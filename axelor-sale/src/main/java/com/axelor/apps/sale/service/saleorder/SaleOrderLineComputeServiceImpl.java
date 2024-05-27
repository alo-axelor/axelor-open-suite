package com.axelor.apps.sale.service.saleorder;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.event.SaleOrderLineCompute;
import com.axelor.event.Event;
import com.google.inject.Inject;
import java.util.Map;

public class SaleOrderLineComputeServiceImpl implements SaleOrderLineComputeService {

/*  protected Event<SaleOrderLineCompute> saleOrderLineComputeEvent;

  @Inject
  public SaleOrderLineComputeServiceImpl(Event<SaleOrderLineCompute> saleOrderLineComputeEvent) {
    this.saleOrderLineComputeEvent = saleOrderLineComputeEvent;
  }*/

  @Override
  public Map<String, Object> computeValues(SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    SaleOrderLineCompute saleOrderLineCompute = new SaleOrderLineCompute(saleOrderLine, saleOrder);
    /*saleOrderLineComputeEvent.fire(saleOrderLineCompute);*/
    return saleOrderLineCompute.getSaleOrderLineMap();
  }
}
