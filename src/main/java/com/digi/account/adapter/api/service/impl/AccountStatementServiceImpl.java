package com.digi.account.adapter.api.service.impl;

import com.digi.account.adapter.api.service.AccountStatementService;
import com.digi.account.domain.model.dto.*;
import com.digi.account.exception.PdfGenerationException;
import com.digi.account.exception.TemplateNotFoundException;
import com.digi.account.infrastructure.helper.PdfHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountStatementServiceImpl implements AccountStatementService {

    public final ObjectMapper objectMapper;
    public final PdfHelper pdfHelper;
    public byte[] generateAccountStatement(AccountStatementRequest request) {
        try {
            String segmentName = request.getRequestInfo().getSegmentNameOrDefault();
            log.info("Processing request for segment: {}", segmentName);

            String fieldType = request.getRequestInfo().getFieldType();
            if (!"pdf".equalsIgnoreCase(fieldType)) {
                throw new PdfGenerationException("000400", "Only PDF generation is supported currently. Provided: " + fieldType);
            }

            String templatePath = getTemplatePath(segmentName);
            log.info("Loading template: {}", templatePath);

            StatementData statementData = loadMockDataFromJson(segmentName);
            log.info("Loaded mock data for {} segment with {} transactions",
                    segmentName, statementData.getTransactions().size());

            byte[] pdfBytes = generatePdfFromTemplate(templatePath, statementData);

            log.info("PDF generated successfully for segment: {}, size: {} bytes", segmentName, pdfBytes.length);
            return pdfBytes;

        } catch (TemplateNotFoundException | PdfGenerationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during PDF generation", e);
            throw new PdfGenerationException("000500", "PDF generation failed: " + e.getMessage());
        }
    }
    public String getTemplatePath(String segmentName) {
        return switch (segmentName.toLowerCase()) {
            case "retail" -> "templates/AccountStatementRetail.docx";
            case "private" -> "templates/AccountStatementPrivate.docx";
            default -> throw new TemplateNotFoundException("000404", "Template not found for segment: " + segmentName);
        };
    }
    public StatementData loadMockDataFromJson(String segmentName) {
        try {
            String jsonPath = "mock/" + segmentName.toLowerCase() + "-mock-data.json";
            log.info("Loading mock data from: {}", jsonPath);

            InputStream jsonStream = new ClassPathResource(jsonPath).getInputStream();
            StatementData data = objectMapper.readValue(jsonStream, StatementData.class);

            if (data.getTransactions() == null || data.getTransactions().isEmpty()) {
                log.warn("No transactions found in mock data for segment: {}", segmentName);
            }

            return data;

        } catch (Exception e) {
            log.error("Failed to load mock data for segment: {}", segmentName, e);
            throw new PdfGenerationException("000500", "Failed to load mock data: " + e.getMessage());
        }
    }
    public byte[] generatePdfFromTemplate(String templatePath, StatementData data) {
        try (InputStream templateStream = new ClassPathResource(templatePath).getInputStream();
             XWPFDocument document = new XWPFDocument(templateStream);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            pdfHelper.replacePlaceholders(document, data);
            PdfOptions options = PdfOptions.create();
            PdfConverter.getInstance().convert(document, out, options);
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Error generating PDF from template: {}", templatePath, e);
            throw new PdfGenerationException("000500", "Failed to generate PDF: " + e.getMessage());
        }
    }




}