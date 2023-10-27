package com.axelor.apps.bankpayment.service.bankstatement;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.base.AxelorException;

import java.io.IOException;

public interface BankStatementImportFactoryService {
    void runImport(BankStatement bankStatement) throws AxelorException, IOException;
    void checkImport(BankStatement bankStatement) throws AxelorException;
    void updateStatus(BankStatement bankStatement);
}
