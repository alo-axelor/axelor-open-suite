package com.axelor.apps.bankpayment.service.bankstatement;

import com.axelor.apps.bankpayment.db.BankPaymentBatch;
import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.repo.BankPaymentBankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineRepository;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.List;

public class BankStatementLineFetchServiceImpl implements BankStatementLineFetchService{

    protected BankPaymentBankStatementLineAFB120Repository bankPaymentBankStatementLineAFB120Repository;
    protected BankStatementLineRepository bankStatementLineRepository;

    @Inject
    public BankStatementLineFetchServiceImpl(BankPaymentBankStatementLineAFB120Repository bankPaymentBankStatementLineAFB120Repository, BankStatementLineRepository bankStatementLineRepository) {
        this.bankPaymentBankStatementLineAFB120Repository = bankPaymentBankStatementLineAFB120Repository;
        this.bankStatementLineRepository = bankStatementLineRepository;
    }

    @Override
    public List<BankStatementLine> getBankStatementLines(BankStatement bankStatement) {
        List<BankStatementLine> bankStatementLines = new ArrayList<>();
        bankStatementLines.addAll(bankPaymentBankStatementLineAFB120Repository
                .all()
                .filter("self.bankStatement = :bankStatement")
                .bind("bankStatement", bankStatement)
                .fetch());
        bankStatementLines.addAll(bankStatementLineRepository.findByBankStatement(bankStatement).fetch());
        return bankStatementLines;
    }
}
