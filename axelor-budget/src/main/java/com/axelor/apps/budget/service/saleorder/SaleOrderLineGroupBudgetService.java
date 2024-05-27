package com.axelor.apps.budget.service.saleorder;

import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.Map;

public interface SaleOrderLineGroupBudgetService {

  Map<String, Object> computeValues(SaleOrderLine saleOrderLine);
}
