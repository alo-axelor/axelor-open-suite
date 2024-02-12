package com.axelor.apps.contract.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountConfig;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.InvoiceLine;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.repo.TraceBackRepository;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.contract.db.repo.ContractRepository;
import com.axelor.i18n.I18n;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class ContractYerServiceImpl implements ContractYerService {

  protected InvoiceLinePricingService invoiceLinePricingService;
  protected AccountConfigService accountConfigService;
  protected AccountManagementContractService accountManagementContractService;

  @Inject
  public ContractYerServiceImpl(
      InvoiceLinePricingService invoiceLinePricingService,
      AccountConfigService accountConfigService,
      AccountManagementContractService accountManagementContractService) {
    this.invoiceLinePricingService = invoiceLinePricingService;
    this.accountConfigService = accountConfigService;
    this.accountManagementContractService = accountManagementContractService;
  }

  @Override
  public void invoiceYerContract(Contract contract, Invoice invoice) throws AxelorException {
    if (!isYerContract(contract)) {
      return;
    }

    invoiceLinePricingService.computePricing(invoice);
    replaceAccount(invoice);
  }

  protected void replaceAccount(Invoice invoice) throws AxelorException {
    AccountConfig accountConfig = accountConfigService.getAccountConfig(invoice.getCompany());
    Company company = invoice.getCompany();
    Product yerProduct = accountConfig.getYearEndBonusProduct();
    boolean isPurchase = InvoiceToolService.isPurchase(invoice);
    for (InvoiceLine invoiceLine : invoice.getInvoiceLineList()) {
      replaceAccount(invoiceLine, accountConfig, yerProduct, company, isPurchase);
    }
  }

  protected void replaceAccount(
      InvoiceLine invoiceLine,
      AccountConfig accountConfig,
      Product yerProduct,
      Company company,
      boolean isPurchase)
      throws AxelorException {
    Account accountReplace =
        getReplacementAccount(invoiceLine, accountConfig, yerProduct, company, isPurchase);
    if (accountReplace != null) {
      invoiceLine.setAccount(accountReplace);
    }
  }

  protected Account getReplacementAccount(
      InvoiceLine invoiceLine,
      AccountConfig accountConfig,
      Product yerProduct,
      Company company,
      boolean isPurchase)
      throws AxelorException {
    boolean IsYerAccountConfigByProductFamilyEnabled =
        accountConfig.getIsYerAccountConfigByProductFamilyEnabled();
    Account accountReplace;

    if (yerProduct != null && yerProduct.equals(invoiceLine.getProduct())) {
      return getYerProductAccount(
          invoiceLine, yerProduct, company, isPurchase, IsYerAccountConfigByProductFamilyEnabled);
    }

    accountReplace =
        getYerAccount(
            invoiceLine, yerProduct, company, isPurchase, IsYerAccountConfigByProductFamilyEnabled);
    return accountReplace;
  }

  protected Account getYerAccount(
      InvoiceLine invoiceLine,
      Product yerProduct,
      Company company,
      boolean isPurchase,
      boolean IsYerAccountConfigByProductFamilyEnabled)
      throws AxelorException {
    Account accountReplace;
    if (!IsYerAccountConfigByProductFamilyEnabled) {
      if (yerProduct != null) {
        accountReplace =
            accountManagementContractService.getProductAccountOnly(
                yerProduct,
                company,
                invoiceLine.getInvoice().getFiscalPosition(),
                isPurchase,
                invoiceLine.getFixedAssets());
      } else {
        throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            I18n.get("Please fill a year end bonus product in account config."));
      }
    } else {
      accountReplace =
          accountManagementContractService.getProductYerAccount(
              invoiceLine.getProduct(), company, isPurchase);
    }
    return accountReplace;
  }

  protected Account getYerProductAccount(
      InvoiceLine invoiceLine,
      Product yerProduct,
      Company company,
      boolean isPurchase,
      boolean IsYerAccountConfigByProductFamilyEnabled)
      throws AxelorException {
    Account accountReplace =
        accountManagementContractService.getProductAccountOnly(
            yerProduct,
            company,
            invoiceLine.getInvoice().getFiscalPosition(),
            isPurchase,
            invoiceLine.getFixedAssets());
    if (accountReplace != null) {
      return accountReplace;
    } else if (IsYerAccountConfigByProductFamilyEnabled) {
      return accountManagementContractService.getProductYerAccount(
          invoiceLine.getProduct(), company, isPurchase);
    }

    return accountReplace;
  }

  protected boolean isYerContract(Contract contract) {
    int targetTypeSelect = contract.getTargetTypeSelect();
    List<Integer> yerTypes = new ArrayList<>();
    yerTypes.add(ContractRepository.YER_CUSTOMER_CONTRACT);
    yerTypes.add(ContractRepository.YER_SUPPLIER_CONTRACT);
    return yerTypes.contains(targetTypeSelect);
  }
}
