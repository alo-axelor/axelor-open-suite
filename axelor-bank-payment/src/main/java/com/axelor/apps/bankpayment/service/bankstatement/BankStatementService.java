/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2023 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.axelor.apps.bankpayment.service.bankstatement;

import com.axelor.apps.bankpayment.db.BankStatement;
import com.axelor.apps.bankpayment.db.BankStatementFileFormat;
import com.axelor.apps.bankpayment.db.BankStatementLine;
import com.axelor.apps.bankpayment.db.BankStatementLineAFB120;
import com.axelor.apps.bankpayment.db.repo.BankStatementFileFormatRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementLineRepository;
import com.axelor.apps.bankpayment.db.repo.BankStatementRepository;
import com.axelor.apps.bankpayment.exception.BankPaymentExceptionMessage;
import com.axelor.apps.bankpayment.service.bankstatement.afb120.BankStatementImportAFB120Service;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.BankDetails;
import com.axelor.apps.base.db.repo.BankDetailsRepository;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

public class BankStatementService {

  protected BankStatementRepository bankStatementRepository;
  protected BankStatementLineRepository bankStatementLineRepository;

  @Inject
  public BankStatementService(
      BankStatementRepository bankStatementRepository,
      BankStatementLineRepository bankStatementLineRepository) {
    this.bankStatementRepository = bankStatementRepository;
    this.bankStatementLineRepository = bankStatementLineRepository;
  }

  public void runImport(BankStatement bankStatement, boolean alertIfFormatNotSupported)
      throws IOException, AxelorException {

    if (bankStatement.getBankStatementFile() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_MISSING_FILE));
    }

    if (bankStatement.getBankStatementFileFormat() == null) {
      throw new AxelorException(
          TraceBackRepository.CATEGORY_MISSING_FIELD,
          I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_MISSING_FILE_FORMAT));
    }

    BankStatementFileFormat bankStatementFileFormat = bankStatement.getBankStatementFileFormat();

    switch (bankStatementFileFormat.getStatementFileFormatSelect()) {
      case BankStatementFileFormatRepository.FILE_FORMAT_CAMT_XXX_CFONB120_REP:
      case BankStatementFileFormatRepository.FILE_FORMAT_CAMT_XXX_CFONB120_STM:
        Beans.get(BankStatementImportAFB120Service.class).runImport(bankStatement);
        break;

      default:
        if (alertIfFormatNotSupported) {
          throw new AxelorException(
              TraceBackRepository.CATEGORY_INCONSISTENCY,
              I18n.get(BankPaymentExceptionMessage.BANK_STATEMENT_FILE_UNKNOWN_FORMAT));
        }
    }
  }

  @Transactional
  public BankStatement setIsFullyReconciled(BankStatement bankStatement) {
    List<BankStatementLine> bankStatementLines =
        bankStatementLineRepository.findByBankStatement(bankStatement).fetch();
    BigDecimal amountToReconcile = BigDecimal.ZERO;
    for (BankStatementLine bankStatementLine : bankStatementLines) {
      amountToReconcile = amountToReconcile.add(bankStatementLine.getAmountRemainToReconcile());
    }
    if (amountToReconcile.compareTo(BigDecimal.ZERO) == 0) {
      bankStatement.setIsFullyReconciled(true);
    }

    return bankStatementRepository.save(bankStatement);
  }

  public List<BankDetails> fetchBankDetailsList(BankStatement bankStatement){
    List<BankDetails> bankDetailsList = new ArrayList<>();
    bankDetailsList.addAll(Beans.get(BankStatementImportAFB120Service.class).fetchBankDetailsList(bankStatement));
    return bankDetailsList.stream().distinct().collect(Collectors.toList());
  }
}
