package com.digi.account.adapter.api.controller;

import com.digi.account.adapter.api.service.impl.AccountStatementServiceImpl;
import com.digi.account.domain.model.dto.AccountStatementRequest;
import com.digi.account.infrastructure.annotation.RequireDeviceInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/api/v1/account-statement")
@RequiredArgsConstructor
@Slf4j
@RequireDeviceInfo
public class AccountStatementController {

    private final AccountStatementServiceImpl accountStatementService;

    @PostMapping("/download-account-statement")
    public ResponseEntity<InputStreamResource> downloadAccountStatement(
            @Valid @RequestBody AccountStatementRequest request,
            @RequestHeader(value = "serviceId", required = true) String serviceId,
            @RequestHeader(value = "moduleId", required = true) String moduleId,
            @RequestHeader(value = "subModuleId", required = true) String subModuleId,
            @RequestHeader(value = "screenId", required = true) String screenId,
            @RequestHeader(value = "channel", required = true) String channel,
            @RequestHeader(value = "accept-language", required = false, defaultValue = "en") String acceptLanguage
    ) {
        log.info("Received account statement request for customer: {}, account: {}, segment: {}, fieldType: {}",
                request.getRequestInfo().getCustomerNumber(),
                request.getRequestInfo().getAccountNumber(),
                request.getRequestInfo().getSegmentName(),
                request.getRequestInfo().getFieldType());

        try {
            // Generate PDF
            byte[] pdfBytes = accountStatementService.generateAccountStatement(request);

            // Prepare filename based on segment
            String segmentName = request.getRequestInfo().getSegmentName() != null
                    ? request.getRequestInfo().getSegmentName()
                    : "Retail";
            String filename = String.format("AccountStatement%s.pdf",
                    segmentName);

            // Set response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(pdfBytes.length);

            log.info("Successfully generated PDF for account: {}", request.getRequestInfo().getAccountNumber());

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(new ByteArrayInputStream(pdfBytes)));

        } catch (Exception e) {
            log.error("Error generating account statement: {}", e.getMessage(), e);
            throw e;
        }
    }
}