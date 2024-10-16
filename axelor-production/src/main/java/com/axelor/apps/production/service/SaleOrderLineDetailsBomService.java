package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface SaleOrderLineDetailsBomService {
  Map<String, Object> createSaleOrderLineDetailsFromBom(
      BillOfMaterial billOfMaterial, SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException;
}
