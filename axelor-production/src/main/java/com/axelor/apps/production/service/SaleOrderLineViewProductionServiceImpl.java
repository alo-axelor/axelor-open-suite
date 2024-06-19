package com.axelor.apps.production.service;

import com.axelor.apps.account.db.repo.AccountConfigRepository;
import com.axelor.apps.account.service.analytic.AnalyticAttrsService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.apps.supplychain.service.SaleOrderLineViewSupplychainServiceImpl;
import com.axelor.apps.supplychain.service.analytic.AnalyticAttrsSupplychainService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SaleOrderLineViewProductionServiceImpl extends SaleOrderLineViewSupplychainServiceImpl
    implements SaleOrderLineViewProductionService {

  @Inject
  public SaleOrderLineViewProductionServiceImpl(
      AppBaseService appBaseService,
      AppSaleService appSaleService,
      AnalyticAttrsService analyticAttrsService,
      AnalyticAttrsSupplychainService analyticAttrsSupplychainService,
      AppSupplychainService appSupplychainService,
      AccountConfigRepository accountConfigRepository) {
    super(
        appBaseService,
        appSaleService,
        analyticAttrsService,
        analyticAttrsSupplychainService,
        appSupplychainService,
        accountConfigRepository);
  }

  @Override
  public Map<String, Map<String, Object>> getProductOnChangeAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) throws AxelorException {
    Map<String, Map<String, Object>> attrs =
        super.getProductOnChangeAttrs(saleOrderLine, saleOrder);
    attrs.putAll(hideBomAndProdProcess(saleOrderLine));
    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> getSaleSupplySelectOnChangeAttrs(
      SaleOrderLine saleOrderLine, SaleOrder saleOrder) {
    Map<String, Map<String, Object>> attrs =
        super.getSaleSupplySelectOnChangeAttrs(saleOrderLine, saleOrder);
    attrs.putAll(hideBomAndProdProcess(saleOrderLine));
    return attrs;
  }

  @Override
  public Map<String, Map<String, Object>> hideBomAndProdProcess(SaleOrderLine saleOrderLine) {
    Map<String, Map<String, Object>> attrs = new HashMap<>();
    int saleSupplySelect = saleOrderLine.getSaleSupplySelect();
    String productTypeSelect =
        Optional.ofNullable(saleOrderLine.getProduct())
            .map(Product::getProductTypeSelect)
            .orElse("");
    boolean hideBom =
        (saleSupplySelect != 3 && saleSupplySelect != 4) || productTypeSelect.equals("service");
    boolean hideProdProcess = saleSupplySelect != 3 || productTypeSelect.equals("service");
    attrs.put("billOfMaterial", Map.of(HIDDEN_ATTR, hideBom));
    attrs.put("customizeBOMBtn", Map.of(HIDDEN_ATTR, hideBom));
    attrs.put("prodProcess", Map.of(HIDDEN_ATTR, hideProdProcess));
    attrs.put("customizeProdProcessBtn", Map.of(HIDDEN_ATTR, hideProdProcess));
    return attrs;
  }
}
