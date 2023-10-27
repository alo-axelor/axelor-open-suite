package com.axelor.apps.bankpayment.service.bankstatement.afb120;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.bankpayment.db.repo.BankPaymentBankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineAFB120Repository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.service.bankstatement.BankStatementImportFactoryService;
import com.axelor.apps.bankpayment.service.bankstatement.file.afb120.BankStatementFileAFB120Service;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JPA;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.persist.Transactional;

import javax.persistence.Query;
import java.io.IOException;
import java.util.List;

public class BankStatementImportAFB120Service implements BankStatementImportFactoryService {

    protected BankStatementRepository bankStatementRepository;
    protected BankPaymentBankStatementLineAFB120Repository bankPaymentBankStatementLineAFB120Repository;

    @Override
    public void runImport(BankStatement bankStatement) throws AxelorException, IOException {
        Beans.get(BankStatementFileAFB120Service.class).process(bankStatement);
        this.checkImport(bankStatement);
        updateStatus(bankStatement);
    }

    @Transactional
    public void updateStatus(BankStatement bankStatement) {
        List<BankDetails> bankDetailsList = fetchBankDetailsList(bankStatement);
        if (!ObjectUtils.isEmpty(bankDetailsList)) {
            for (BankDetails bankDetails : bankDetailsList) {
                BankStatementLineAFB120 finalBankStatementLineAFB120 =
                        bankPaymentBankStatementLineAFB120Repository
                                .findByBankStatementBankDetailsAndLineType(
                                        bankStatement,
                                        bankDetails,
                                        BankStatementLineAFB120Repository.LINE_TYPE_FINAL_BALANCE)
                                .order("-operationDate")
                                .order("-sequence")
                                .fetchOne();
                bankDetails.setBalance(
                        (finalBankStatementLineAFB120
                                .getCredit()
                                .subtract(finalBankStatementLineAFB120.getDebit())));
                bankDetails.setBalanceUpdatedDate(finalBankStatementLineAFB120.getOperationDate());
            }
        }

        bankStatement.setStatusSelect(BankStatementRepository.STATUS_IMPORTED);
        bankStatementRepository.save(bankStatement);
    }

    @Override
    public void checkImport(BankStatement bankStatement) throws AxelorException {
        try {
            boolean alreadyImported = false;

            List<BankStatementLineAFB120> initialLines;
            List<BankStatementLineAFB120> finalLines;
            List<BankDetails> bankDetails = fetchBankDetailsList(bankStatement);
            // Load lines
            for (BankDetails bd : bankDetails) {
                initialLines =
                        bankPaymentBankStatementLineAFB120Repository
                                .findByBankStatementBankDetailsAndLineType(
                                        bankStatement, bd, BankStatementLineAFB120Repository.LINE_TYPE_INITIAL_BALANCE)
                                .fetch();

                finalLines =
                        bankPaymentBankStatementLineAFB120Repository
                                .findByBankStatementBankDetailsAndLineType(
                                        bankStatement, bd, BankStatementLineAFB120Repository.LINE_TYPE_FINAL_BALANCE)
                                .fetch();

                alreadyImported =
                        bankStatementLineAlreadyExists(initialLines)
                                || bankStatementLineAlreadyExists(finalLines)
                                || alreadyImported;
            }
            if (!alreadyImported) {
                checkAmountWithPreviousBankStatement(bankStatement, bankDetails);
                checkAmountWithinBankStatement(bankStatement, bankDetails);
            } else {
                throw new AxelorException(
                        bankStatement,
                        TraceBackRepository.CATEGORY_INCONSISTENCY,
                        I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_ALREADY_IMPORTED));
            }

        } catch (Exception e) {
            deleteBankStatementLines(bankStatementRepository.find(bankStatement.getId()));
            throw e;
        }
    }

    @Transactional
    public void deleteBankStatementLines(BankStatement bankStatement) {
        List<BankStatementLineAFB120> bankStatementLines;
        bankStatementLines =
                bankPaymentBankStatementLineAFB120Repository
                        .all()
                        .filter("self.bankStatement = :bankStatement")
                        .bind("bankStatement", bankStatement)
                        .fetch();
        for (BankStatementLineAFB120 bsl : bankStatementLines) {
            bankPaymentBankStatementLineAFB120Repository.remove(bsl);
        }
        bankStatement.setStatusSelect(BankStatementRepository.STATUS_RECEIVED);
    }

    public void checkAmountWithPreviousBankStatement(
            BankStatement bankStatement, List<BankDetails> bankDetails) throws AxelorException {
        boolean deleteLines = false;
        for (BankDetails bd : bankDetails) {
            BankStatementLineAFB120 initialBankStatementLineAFB120 =
                    bankPaymentBankStatementLineAFB120Repository
                            .findByBankStatementBankDetailsAndLineType(
                                    bankStatement, bd, BankStatementLineAFB120Repository.LINE_TYPE_INITIAL_BALANCE)
                            .order("operationDate")
                            .order("sequence")
                            .fetchOne();
            BankStatementLineAFB120 finalBankStatementLineAFB120 =
                    bankPaymentBankStatementLineAFB120Repository
                            .findByBankDetailsLineTypeExcludeBankStatement(
                                    bankStatement, bd, BankStatementLineAFB120Repository.LINE_TYPE_FINAL_BALANCE)
                            .order("-operationDate")
                            .order("-sequence")
                            .fetchOne();
            if (ObjectUtils.notEmpty(finalBankStatementLineAFB120)
                    && (initialBankStatementLineAFB120
                    .getDebit()
                    .compareTo(finalBankStatementLineAFB120.getDebit())
                    != 0
                    || initialBankStatementLineAFB120
                    .getCredit()
                    .compareTo(finalBankStatementLineAFB120.getCredit())
                    != 0)) {
                deleteLines = true;
            }
        }
        // delete imported
        if (deleteLines) {
            throw new AxelorException(
                    bankStatement,
                    TraceBackRepository.CATEGORY_INCONSISTENCY,
                    I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_NOT_MATCHING));
        }
    }

    public void checkAmountWithinBankStatement(
            BankStatement bankStatement, List<BankDetails> bankDetails) throws AxelorException {
        boolean deleteLines = false;
        for (BankDetails bd : bankDetails) {
            List<BankStatementLineAFB120> initialBankStatementLineAFB120 =
                    bankPaymentBankStatementLineAFB120Repository
                            .findByBankStatementBankDetailsAndLineType(
                                    bankStatement, bd, BankStatementLineAFB120Repository.LINE_TYPE_INITIAL_BALANCE)
                            .order("sequence")
                            .fetch();
            List<BankStatementLineAFB120> finalBankStatementLineAFB120 =
                    bankPaymentBankStatementLineAFB120Repository
                            .findByBankStatementBankDetailsAndLineType(
                                    bankStatement, bd, BankStatementLineAFB120Repository.LINE_TYPE_FINAL_BALANCE)
                            .order("sequence")
                            .fetch();
            initialBankStatementLineAFB120.remove(0);
            finalBankStatementLineAFB120.remove(finalBankStatementLineAFB120.size() - 1);
            if (initialBankStatementLineAFB120.size() != finalBankStatementLineAFB120.size()) {
                deleteLines = true;
                break;
            }
            if (!deleteLines) {
                for (int i = 0; i < initialBankStatementLineAFB120.size(); i++) {
                    deleteLines =
                            deleteLines
                                    || (initialBankStatementLineAFB120
                                    .get(i)
                                    .getDebit()
                                    .compareTo(finalBankStatementLineAFB120.get(i).getDebit())
                                    != 0
                                    || initialBankStatementLineAFB120
                                    .get(i)
                                    .getCredit()
                                    .compareTo(finalBankStatementLineAFB120.get(i).getCredit())
                                    != 0);
                    if (deleteLines) {
                        break;
                    }
                }
            }
            if (deleteLines) {
                break;
            }
        }
        // delete imported
        if (deleteLines) {
            throw new AxelorException(
                    bankStatement,
                    TraceBackRepository.CATEGORY_INCONSISTENCY,
                    I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_INCOHERENT_BALANCE));
        }
    }

    public boolean bankStatementLineAlreadyExists(List<BankStatementLineAFB120> initialLines) {
        boolean alreadyImported = false;
        BankStatementLineAFB120 tempBankStatementLineAFB120;
        for (BankStatementLineAFB120 bslAFB120 : initialLines) {
            tempBankStatementLineAFB120 =
                    bankPaymentBankStatementLineAFB120Repository
                            .all()
                            .filter(
                                    "self.operationDate = :operationDate"
                                            + " AND self.lineTypeSelect = :lineTypeSelect"
                                            + " AND self.bankStatement != :bankStatement"
                                            + " AND self.bankDetails = :bankDetails")
                            .bind("operationDate", bslAFB120.getOperationDate())
                            .bind("lineTypeSelect", bslAFB120.getLineTypeSelect())
                            .bind("bankStatement", bslAFB120.getBankStatement())
                            .bind("bankDetails", bslAFB120.getBankDetails())
                            .fetchOne();
            if (ObjectUtils.notEmpty(tempBankStatementLineAFB120)) {
                alreadyImported = true;
                break;
            }
        }
        return alreadyImported;
    }

    public List<BankDetails> fetchBankDetailsList(BankStatement bankStatement) {
        List<BankDetails> bankDetails;
        String query =
                "select distinct bankDetails from BankStatementLineAFB120 as self"
                        + " where self.bankStatement = ?1";
        Query q = JPA.em().createQuery(query, BankDetails.class);
        q.setParameter(1, bankStatement);
        bankDetails = (List<BankDetails>) q.getResultList();

        return bankDetails;
    }

}
