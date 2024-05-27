package com.axelor.apps.budget.service.observer;

import com.axelor.apps.budget.service.saleorder.SaleOrderLineGroupBudgetService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.event.SaleOrderLineCompute;
import com.axelor.event.Observes;
import com.axelor.inject.Beans;
import java.util.Map;

public class SaleOrderLineBudgetObserver {

/*  void onSaleOrderLineComputation(@Observes SaleOrderLineCompute event) {
    SaleOrderLine saleOrderLine = event.getSaleOrderLine();
    Map<String, Object> saleOrderLineMap = event.getSaleOrderLineMap();
    saleOrderLineMap.putAll(
        Beans.get(SaleOrderLineGroupBudgetService.class).computeValues(saleOrderLine));
  }*/
}
