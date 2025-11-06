package com.digi.account.adapter.api.service;

import com.digi.account.domain.model.dto.AccountStatementRequest;
import com.digi.account.domain.model.dto.StatementData;

public interface AccountStatementService {

    public byte[] generateAccountStatement(AccountStatementRequest request);
    public String getTemplatePath(String segmentName);
    public  StatementData loadMockDataFromJson(String segmentName);
    public byte[] generatePdfFromTemplate(String templatePath, StatementData data);
}
