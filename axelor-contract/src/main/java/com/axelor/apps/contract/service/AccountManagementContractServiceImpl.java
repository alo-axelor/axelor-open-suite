package com.axelor.apps.contract.service;

import com.axelor.apps.account.db.Account;
import com.axelor.apps.account.db.AccountManagement;
import com.axelor.apps.account.db.repo.AccountRepository;
import com.axelor.apps.account.service.AccountManagementServiceAccountImpl;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.tax.FiscalPositionService;
import com.axelor.apps.base.service.tax.TaxService;
import com.google.inject.Inject;

public class AccountManagementContractServiceImpl extends AccountManagementServiceAccountImpl
    implements AccountManagementContractService {
  @Inject
  public AccountManagementContractServiceImpl(
      FiscalPositionService fiscalPositionService,
      TaxService taxService,
      AccountConfigService accountConfigService,
      AccountRepository accountRepository) {
    super(fiscalPositionService, taxService, accountConfigService, accountRepository);
  }

  @Override
  public Account getProductYerAccount(Product product, Company company, boolean isPurchase) {
    return this.getProductYerAccount(product, company, isPurchase, CONFIG_OBJECT_PRODUCT);
  }

  protected Account getProductYerAccount(
      Product product, Company company, boolean isPurchase, int configObject) {

    AccountManagement accountManagement = this.getAccountManagement(product, company, configObject);

    Account account = null;

    if (accountManagement != null) {
      if (isPurchase) {
        account = accountManagement.getYearEndBonusPurchaseAccount();
      } else {
        account = accountManagement.getYearEndBonusSaleAccount();
      }
    }

    if (account == null && configObject == CONFIG_OBJECT_PRODUCT) {
      return getProductYerAccount(product, company, isPurchase, CONFIG_OBJECT_PRODUCT_FAMILY);
    }

    return account;
  }
}
