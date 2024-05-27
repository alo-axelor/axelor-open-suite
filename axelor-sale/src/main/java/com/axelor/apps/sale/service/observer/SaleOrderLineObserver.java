package com.axelor.apps.sale.service.observer;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.event.SaleOrderLineCompute;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.event.Observes;
import com.axelor.inject.Beans;
import java.util.Map;

public class SaleOrderLineObserver {

/*  void onSaleOrderLineComputation(@Observes SaleOrderLineCompute event) throws AxelorException {
    SaleOrderLine saleOrderLine = event.getSaleOrderLine();
    SaleOrder saleOrder = event.getSaleOrder();
    Map<String, Object> saleOrderLineMap = event.getSaleOrderLineMap();

    saleOrderLineMap.putAll(
        Beans.get(SaleOrderLineService.class).computeValues(saleOrder, saleOrderLine));
  }*/
}
