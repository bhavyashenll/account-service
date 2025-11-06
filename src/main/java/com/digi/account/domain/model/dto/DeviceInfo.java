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
public class DeviceInfo {

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    @NotBlank(message = "IP address is required")
    private String ipAddress;

    private String vendorId;
    private String osVersion;
    private String osType;
    private String appVersion;
    private String endToEndId;
}