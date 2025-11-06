package com.digi.account.domain.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestInfo {

    @NotBlank(message = "Customer number is required")
    private String customerNumber;

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    private String segmentName; // Will default to "Retail" if not provided

    @NotBlank(message = "Field type is required")
    private String fieldType; // pdf or xls

    // Helper method to get segment with default
    public String getSegmentNameOrDefault() {
        return (segmentName == null || segmentName.isBlank()) ? "Retail" : segmentName;
    }
}