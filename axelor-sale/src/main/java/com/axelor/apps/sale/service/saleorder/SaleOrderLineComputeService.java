package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.sale.db.SaleOrderLine;

public interface SaleOrderLineComputeService {

  void compute(SaleOrderLine saleOrderLine);
}
