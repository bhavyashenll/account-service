package com.digi.account.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @JsonProperty("date")
    private String date; // «Date»

    @JsonProperty("description")
    private String description; // «Description»

    @JsonProperty("debit")
    private String debit; // «Debit»

    @JsonProperty("credit")
    private String credit; // «Credit»

    @JsonProperty("runningBalance")
    private String runningBalance; // «RunningBalance»
}