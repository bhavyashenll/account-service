package com.digi.account.domain.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountStatementRequest {

    @NotNull(message = "Request info is required")
    @Valid
    private RequestInfo requestInfo;

    @NotNull(message = "Device info is required")
    @Valid
    private DeviceInfo deviceInfo;
}
