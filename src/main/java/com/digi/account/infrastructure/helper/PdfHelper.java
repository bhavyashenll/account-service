package com.digi.account.infrastructure.helper;

import com.digi.account.domain.model.dto.StatementData;
import com.digi.account.domain.model.dto.StatementFooter;
import com.digi.account.domain.model.dto.StatementHeader;
import com.digi.account.domain.model.dto.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.List;
@Slf4j
@Component
public class PdfHelper {



    public void replacePlaceholders(XWPFDocument document, StatementData data) {
        log.info("Starting placeholder replacement...");

        // STEP 1: Replace in ALL BODY TABLES first (this includes your header data table!)
        log.info("Processing {} body tables", document.getTables().size());
        for (int tableIdx = 0; tableIdx < document.getTables().size(); tableIdx++) {
            XWPFTable table = document.getTables().get(tableIdx);
            log.info("Processing body table {}: {} rows, {} columns",
                    tableIdx, table.getRows().size(),
                    table.getRows().isEmpty() ? 0 : table.getRows().get(0).getTableCells().size());

            // Check if this is a transaction table
            boolean isTransactionTable = isTransactionTable(table);

            if (!isTransactionTable) {
                // This is a regular table (likely your header data table)
                log.info("Table {} is a DATA table - replacing placeholders", tableIdx);
                replaceInDataTable(table, data);
            }
        }

        // STEP 2: Replace in body paragraphs
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            replaceInParagraph(paragraph, data);
        }

        // STEP 3: Replace in actual Word headers
        for (XWPFHeader header : document.getHeaderList()) {
            for (XWPFTable table : header.getTables()) {
                replaceInDataTable(table, data);
            }
            for (XWPFParagraph paragraph : header.getParagraphs()) {
                replaceInParagraph(paragraph, data);
            }
        }

        // STEP 4: Replace in footers
        for (XWPFFooter footer : document.getFooterList()) {
            for (XWPFTable table : footer.getTables()) {
                replaceInDataTable(table, data);
            }
            for (XWPFParagraph paragraph : footer.getParagraphs()) {
                replaceInParagraph(paragraph, data);
            }
        }

        // STEP 5: NOW handle transaction tables
        for (XWPFTable table : document.getTables()) {
            if (isTransactionTable(table)) {
                log.info("Found transaction table - populating transactions");
                replaceTransactionTable(table, data);
            }
        }

        log.info("Placeholder replacement completed");
    }
    public boolean isTransactionTable(XWPFTable table) {
        for (XWPFTableRow row : table.getRows()) {
            String rowText = getRowText(row);
            if (rowText.contains("«TableStart:Transaction»") ||
                    (rowText.contains("«Date»") && rowText.contains("«Description»") && rowText.contains("«Debit»"))) {
                return true;
            }
        }
        return false;
    }
    public void replaceInDataTable(XWPFTable table, StatementData data) {
        for (int rowIdx = 0; rowIdx < table.getRows().size(); rowIdx++) {
            XWPFTableRow row = table.getRow(rowIdx);
            for (int cellIdx = 0; cellIdx < row.getTableCells().size(); cellIdx++) {
                XWPFTableCell cell = row.getCell(cellIdx);

                for (XWPFParagraph paragraph : cell.getParagraphs()) {
                    List<XWPFRun> runs = paragraph.getRuns();
                    if (runs.isEmpty()) continue;

                    // Combine all run texts to get full paragraph text
                    StringBuilder fullText = new StringBuilder();
                    for (XWPFRun run : runs) {
                        String text = run.getText(0);
                        if (text != null) {
                            fullText.append(text);
                        }
                    }

                    String originalText = fullText.toString();

                    if (originalText.contains("«") || originalText.contains("{{")) {
                        log.debug("Table[{}] Cell[{},{}] BEFORE: '{}'",
                                table.getRows().indexOf(row), rowIdx, cellIdx, originalText);

                        String replacedText = replacePlaceholderText(originalText, data);

                        if (!originalText.equals(replacedText)) {
                            // Store formatting from first run
                            XWPFRun firstRun = runs.get(0);
                            String fontFamily = firstRun.getFontFamily();
                            int fontSize = firstRun.getFontSize();
                            boolean isBold = firstRun.isBold();
                            boolean isItalic = firstRun.isItalic();

                            // Remove all runs
                            for (int i = runs.size() - 1; i >= 0; i--) {
                                paragraph.removeRun(i);
                            }

                            // Create single new run with replaced text
                            XWPFRun newRun = paragraph.createRun();
                            newRun.setText(replacedText);

                            // Apply formatting
                            if (fontFamily != null) {
                                newRun.setFontFamily(fontFamily);
                            }
                            if (fontSize > 0) {
                                newRun.setFontSize(fontSize);
                            }
                            newRun.setBold(isBold);
                            newRun.setItalic(isItalic);

                            log.debug("Table[{}] Cell[{},{}] AFTER: '{}'",
                                    table.getRows().indexOf(row), rowIdx, cellIdx, replacedText);
                        }
                    }
                }
            }
        }
    }
    public void replaceInParagraph(XWPFParagraph paragraph, StatementData data) {
        List<XWPFRun> runs = paragraph.getRuns();
        if (runs.isEmpty()) return;

        // Combine all run texts
        StringBuilder fullText = new StringBuilder();
        for (XWPFRun run : runs) {
            String text = run.getText(0);
            if (text != null) {
                fullText.append(text);
            }
        }

        String originalText = fullText.toString();

        if (originalText.contains("«") || originalText.contains("{{")) {
            String replacedText = replacePlaceholderText(originalText, data);

            if (!originalText.equals(replacedText)) {
                XWPFRun firstRun = runs.get(0);
                String fontFamily = firstRun.getFontFamily();

                for (int i = runs.size() - 1; i >= 0; i--) {
                    paragraph.removeRun(i);
                }

                XWPFRun newRun = paragraph.createRun();
                newRun.setText(replacedText);

                if (fontFamily != null) {
                    newRun.setFontFamily(fontFamily);
                }
            }
        }
    }
    public void replaceTransactionTable(XWPFTable table, StatementData data) {
        XWPFTableRow templateRow = null;
        int templateRowIndex = -1;

        for (int i = 0; i < table.getRows().size(); i++) {
            XWPFTableRow row = table.getRow(i);
            String rowText = getRowText(row);

            if (rowText.contains("«TableStart:Transaction»") ||
                    (rowText.contains("«Date»") && rowText.contains("«Description»"))) {
                templateRow = row;
                templateRowIndex = i;
                log.info("Found transaction template row at index: {}", i);
                break;
            }
        }

        if (templateRow == null || data.getTransactions() == null || data.getTransactions().isEmpty()) {
            log.info("No transaction template row found or no transactions to populate");
            return;
        }

        log.info("Populating {} transactions into table", data.getTransactions().size());

        for (int i = 0; i < data.getTransactions().size(); i++) {
            Transaction txn = data.getTransactions().get(i);
            XWPFTableRow newRow;

            if (i == 0) {
                newRow = templateRow;
            } else {
                newRow = table.insertNewTableRow(templateRowIndex + i);
                copyRowStructure(templateRow, newRow);
            }

            populateTransactionRow(newRow, txn);
        }

        cleanTemplateMarkers(templateRow);
    }
    public String getRowText(XWPFTableRow row) {
        StringBuilder sb = new StringBuilder();
        for (XWPFTableCell cell : row.getTableCells()) {
            sb.append(cell.getText()).append(" ");
        }
        return sb.toString();
    }
    public void copyRowStructure(XWPFTableRow sourceRow, XWPFTableRow targetRow) {
        for (int i = 0; i < sourceRow.getTableCells().size(); i++) {
            if (targetRow.getTableCells().size() <= i) {
                targetRow.createCell();
            }

            XWPFTableCell sourceCell = sourceRow.getCell(i);
            XWPFTableCell targetCell = targetRow.getCell(i);

            if (sourceCell.getCTTc() != null && targetCell.getCTTc() != null) {
                CTTcPr sourceTcPr = sourceCell.getCTTc().getTcPr();
                if (sourceTcPr != null) {
                    targetCell.getCTTc().setTcPr((CTTcPr) sourceTcPr.copy());
                }
            }
        }
    }
    public void populateTransactionRow(XWPFTableRow row, Transaction txn) {
        // ✅ Ensure uniform row height (in Twips, 1/20th of a point)
        row.setHeight(400); // ~20 points, adjust to your liking

        if (row.getTableCells().size() >= 5) {
            setCellText(row.getCell(0), txn.getDate() != null ? txn.getDate() : "", true);
            setCellText(row.getCell(1), txn.getDescription() != null ? txn.getDescription() : "", false);
            setCellText(row.getCell(2), txn.getDebit() != null ? txn.getDebit() : "", true);
            setCellText(row.getCell(3), txn.getCredit() != null ? txn.getCredit() : "", true);
            setCellText(row.getCell(4), txn.getRunningBalance() != null ? txn.getRunningBalance() : "", true);
        }

        // ✅ Clean template markers (just in case placeholders exist)
        cleanTemplateMarkers(row);
    }
    public void setCellText(XWPFTableCell cell, String text, boolean centerAlign) {
        while (cell.getParagraphs().size() > 0) {
            cell.removeParagraph(0);
        }

        XWPFParagraph paragraph = cell.addParagraph();
        paragraph.setAlignment(centerAlign ? ParagraphAlignment.CENTER : ParagraphAlignment.LEFT);

        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setFontSize(10);
    }
    public void cleanTemplateMarkers(XWPFTableRow row) {
        int desiredHeight = 700; // 700 Twips ≈ 35pt
        ensureRowHeight(row, desiredHeight); // ✅ ensure uniform height

        for (XWPFTableCell cell : row.getTableCells()) {
            for (XWPFParagraph para : cell.getParagraphs()) {

                // ✅ Consistent paragraph formatting
                para.setSpacingBefore(120);
                para.setSpacingAfter(120);
                para.setSpacingBetween(1.3);
                para.setAlignment(ParagraphAlignment.LEFT);

                // ✅ Clean text placeholders
                for (int i = para.getRuns().size() - 1; i >= 0; i--) {
                    XWPFRun run = para.getRuns().get(i);
                    String text = run.getText(0);
                    if (text != null) {
                        text = text.replace("«TableStart:Transaction»", "")
                                .replace("«TableEnd:Transaction»", "")
                                .replace("«Date»", "")
                                .replace("«Description»", "")
                                .replace("«Debit»", "")
                                .replace("«Credit»", "")
                                .replace("«RunningBalance»", "");

                        if (text.trim().isEmpty()) {
                            para.removeRun(i);
                        } else {
                            run.setText(text, 0);
                        }
                    }
                }
            }
        }
    }
    public void ensureRowHeight(XWPFTableRow row, int heightTwips) {
        CTRow ctRow = row.getCtRow();

        // Ensure the <w:trPr> element exists
        CTTrPr trPr = ctRow.isSetTrPr() ? ctRow.getTrPr() : ctRow.addNewTrPr();

        // Add or modify the <w:trHeight> element
        CTHeight ctHeight = trPr.sizeOfTrHeightArray() > 0
                ? trPr.getTrHeightArray(0)
                : trPr.addNewTrHeight();

        ctHeight.setVal(BigInteger.valueOf(heightTwips));
        ctHeight.setHRule(STHeightRule.EXACT); // Force exact height
    }
    public String replacePlaceholderText(String text, StatementData data) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String result = text;

        if (data.getHeader() != null) {
            StatementHeader header = data.getHeader();

            String[][] replacements = {
                    {"«Product.AccountName»", header.getAccountName()},
                    {"«Product.Transactions.Summary.StatementDa»", header.getStatementDate()},
                    {"«Product.Transactions.Summary.StatementPe»", header.getStatementPeriod()},
                    {"«Product.ProductCCY»", header.getCurrency()},
                    {"«Product.Customer.CIF»", header.getCustomerCIF()},
                    {"«Product.ProductCode»", header.getProductCode()},
                    {"«Product.ProductNum»", header.getProductNumber()},
                    {"«Product.IBAN»", header.getIban()},
                    {"«Product.CashLimit»", header.getCashLimit()},
                    {"«Product.UtilizedLimit»", header.getUtilizedLimit()},
                    {"«Product.Customer.Address1»", header.getAddress1()},
                    {"«Product.Customer.Address2»", header.getAddress2()},
                    {"«Product.Customer.Address3»", header.getAddress3()}
            };

            for (String[] replacement : replacements) {
                String placeholder = replacement[0];
                String value = replacement[1] != null ? replacement[1] : "";

                if (result.contains(placeholder)) {
                    result = result.replace(placeholder, value);
                    log.debug("✓ Replaced {} with '{}'", placeholder, value);
                }
            }
        }

        if (data.getFooter() != null) {
            StatementFooter footer = data.getFooter();
            result = result.replace("{{generatedOn}}", footer.getGeneratedOn() != null ? footer.getGeneratedOn() : "")
                    .replace("{{disclaimer}}", footer.getDisclaimer() != null ? footer.getDisclaimer() : "")
                    .replace("{{contactInfo}}", footer.getContactInfo() != null ? footer.getContactInfo() : "");
        }

        return result;
    }
}
