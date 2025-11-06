package com.digi.account.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatementData {

    @JsonProperty("header")
    private StatementHeader header;

    @JsonProperty("transactions")
    private List<Transaction> transactions;

    @JsonProperty("footer")
    private StatementFooter footer;
}