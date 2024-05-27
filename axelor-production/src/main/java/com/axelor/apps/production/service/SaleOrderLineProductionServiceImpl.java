package com.axelor.apps.production.service;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineProductionServiceImpl implements SaleOrderLineProductionService {

  protected AppProductionService appProductionService;

  @Inject
  public SaleOrderLineProductionServiceImpl(AppProductionService appProductionService) {
    this.appProductionService = appProductionService;
  }

  @Override
  public Map<String, Object> setBillOfMaterial(SaleOrderLine saleOrderLine) {
    Map<String, Object> saleOrderLineMap = new HashMap<>();
    if (appProductionService.isApp("production")) {
      Product product = saleOrderLine.getProduct();

      if (product != null) {
        if (product.getDefaultBillOfMaterial() != null) {
          saleOrderLine.setBillOfMaterial(product.getDefaultBillOfMaterial());
        } else {
          saleOrderLine.setBillOfMaterial(product.getParentProduct().getDefaultBillOfMaterial());
        }
        saleOrderLineMap.put("billOfMaterial", saleOrderLine.getBillOfMaterial());
      }
    }
    return saleOrderLineMap;
  }
}
