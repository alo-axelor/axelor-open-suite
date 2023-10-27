package com.axelor.apps.bankpayment.service.bankstatement;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;

import java.util.List;

public interface BankStatementLineFetchService {
    List<BankStatementLine> getBankStatementLines(BankStatement bankStatement);
}
