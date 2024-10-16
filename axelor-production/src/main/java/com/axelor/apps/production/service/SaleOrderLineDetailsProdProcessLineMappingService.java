package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.sale.db.SaleOrder;

public interface SaleOrderLineDetailsProdProcessLineMappingService {
  SaleOrderLineDetails mapToSaleOrderLineDetails(
      ProdProcessLine prodProcessLine, SaleOrder saleOrder) throws AxelorException;
}
