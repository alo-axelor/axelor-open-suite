package com.axelor.apps.bankpayment.service.bankstatement;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.service.bankstatement.afb120.BankStatementImportAFB120Service;
import com.axelor.inject.Beans;

public class BankStatementLineDeleteService {
    public void deleteBankStatementLines(BankStatement bankStatement) {
        Beans.get(BankStatementImportAFB120Service.class).deleteBankStatementLines(bankStatement);
        bankStatement.setStatusSelect(BankStatementRepository.STATUS_RECEIVED);
    }
}
