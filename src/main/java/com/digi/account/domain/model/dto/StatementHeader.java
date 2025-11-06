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
public class StatementHeader {

    @JsonProperty("accountName")
    private String accountName; // «Product.AccountName»

    @JsonProperty("statementDate")
    private String statementDate; // «Product.Transactions.Summary.StatementDa»

    @JsonProperty("statementPeriod")
    private String statementPeriod; // «Product.Transactions.Summary.StatementPe»

    @JsonProperty("currency")
    private String currency; // «Product.ProductCCY»

    @JsonProperty("customerCIF")
    private String customerCIF; // «Product.Customer.CIF»

    @JsonProperty("productCode")
    private String productCode; // «Product.ProductCode»

    @JsonProperty("productNumber")
    private String productNumber; // «Product.ProductNum»

    @JsonProperty("iban")
    private String iban; // «Product.IBAN»

    @JsonProperty("cashLimit")
    private String cashLimit; // «Product.CashLimit»

    @JsonProperty("utilizedLimit")
    private String utilizedLimit; // «Product.UtilizedLimit»

    @JsonProperty("address1")
    private String address1; // «Product.Customer.Address1»

    @JsonProperty("address2")
    private String address2; // «Product.Customer.Address2»

    @JsonProperty("address3")
    private String address3; // «Product.Customer.Address3»
}