package com.axelor.apps.budget.service.observer;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.budget.service.saleorder.SaleOrderLineBudgetService;
import com.axelor.apps.budget.service.saleorder.SaleOrderLineViewBudgetService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.event.SaleOrderLineProductOnChange;
import com.axelor.apps.sale.service.event.SaleOrderLineViewOnLoad;
import com.axelor.apps.sale.service.event.SaleOrderLineViewOnNew;
import com.axelor.event.Observes;
import com.axelor.inject.Beans;
import java.util.Map;

public class SaleOrderLineBudgetObserver {

  void onSaleOrderLineOnNew(@Observes SaleOrderLineViewOnNew event) throws AxelorException {
    SaleOrder saleOrder = event.getSaleOrder();
    Map<String, Map<String, Object>> saleOrderLineMap = event.getSaleOrderLineMap();
    saleOrderLineMap.putAll(Beans.get(SaleOrderLineViewBudgetService.class).checkBudget(saleOrder));
  }

  void onSaleOrderLineOnLoad(@Observes SaleOrderLineViewOnLoad event) throws AxelorException {
    SaleOrder saleOrder = event.getSaleOrder();
    Map<String, Map<String, Object>> saleOrderLineMap = event.getSaleOrderLineMap();
    saleOrderLineMap.putAll(Beans.get(SaleOrderLineViewBudgetService.class).checkBudget(saleOrder));
  }

  void onSaleOrderLineProductChange(@Observes SaleOrderLineProductOnChange event)
      throws AxelorException {
    SaleOrderLine saleOrderLine = event.getSaleOrderLine();
    SaleOrder saleOrder = event.getSaleOrder();
    Map<String, Object> saleOrderLineMap = event.getSaleOrderLineMap();
    SaleOrderLineBudgetService saleOrderLineBudgetService =
        Beans.get(SaleOrderLineBudgetService.class);
    saleOrderLineMap.putAll(saleOrderLineBudgetService.setProductAccount(saleOrder, saleOrderLine));
    saleOrderLineMap.putAll(saleOrderLineBudgetService.resetBudget(saleOrderLine));
  }
}
