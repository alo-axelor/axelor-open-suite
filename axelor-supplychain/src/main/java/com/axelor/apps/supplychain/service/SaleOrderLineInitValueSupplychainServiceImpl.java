package com.axelor.apps.supplychain.service;

import com.axelor.apps.account.service.analytic.AnalyticGroupService;
import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineInitValueServiceImpl;
import com.axelor.apps.supplychain.db.SupplyChainConfig;
import com.axelor.apps.supplychain.model.AnalyticLineModel;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.studio.db.AppSupplychain;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineInitValueSupplychainServiceImpl extends SaleOrderLineInitValueServiceImpl
    implements SaleOrderLineInitValueSupplychainService {

  protected SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain;
  protected AppSupplychainService appSupplychainService;
  protected AnalyticGroupService analyticGroupService;

  @Inject
  public SaleOrderLineInitValueSupplychainServiceImpl(
      SaleOrderLineServiceSupplyChain saleOrderLineServiceSupplyChain,
      AppSupplychainService appSupplychainService,
      AnalyticGroupService analyticGroupService) {
    this.saleOrderLineServiceSupplyChain = saleOrderLineServiceSupplyChain;
    this.appSupplychainService = appSupplychainService;
    this.analyticGroupService = analyticGroupService;
  }

  @Override
  public Map<String, Object> onNewInitValues(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {
    Map<String, Object> values = super.onNewInitValues(saleOrder, saleOrderLine);
    AppSupplychain appSupplychain = appSupplychainService.getAppSupplychain();
    values.putAll(fillEstimatedDate(saleOrder, saleOrderLine));
    values.putAll(initQty(saleOrderLine));
    if (appSupplychain.getManageStockReservation()) {
      values.putAll(saleOrderLineServiceSupplyChain.updateRequestedReservedQty(saleOrderLine));
    }
    values.putAll(fillRequestQty(saleOrder, saleOrderLine));
    values.putAll(printAnalyticAccounts(saleOrder, saleOrderLine));
    return values;
  }

  @Override
  public Map<String, Object> onLoadInitValues(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {
    Map<String, Object> values = super.onLoadInitValues(saleOrder, saleOrderLine);
    values.putAll(printAnalyticAccounts(saleOrder, saleOrderLine));
    return values;
  }

  @Override
  public Map<String, Object> onNewEditableInitValues(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    Map<String, Object> values = new HashMap<>();
    return values;
  }

  @Override
  public Map<String, Object> onChangeProductInitValues(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine) throws AxelorException {
    Map<String, Object> values = new HashMap<>();
    values.putAll(printAnalyticAccounts(saleOrder, saleOrderLine));
    return values;
  }

  protected Map<String, Object> fillRequestQty(SaleOrder saleOrder, SaleOrderLine saleOrderLine) {
    Map<String, Object> values = new HashMap<>();
    SupplyChainConfig supplyChainConfig = saleOrder.getCompany().getSupplyChainConfig();
    if (supplyChainConfig == null) {
      return values;
    }
    boolean autoRequestReservedQty = supplyChainConfig.getAutoRequestReservedQty();
    saleOrderLine.setIsQtyRequested(autoRequestReservedQty);
    if (autoRequestReservedQty) {
      saleOrderLine.setRequestedReservedQty(saleOrderLine.getQty());
    }

    values.put("isQtyRequested", saleOrderLine.getIsQtyRequested());
    values.put("requestedReservedQty", saleOrderLine.getRequestedReservedQty());

    return values;
  }

  @Override
  public Map<String, Object> printAnalyticAccounts(SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {
    AnalyticLineModel analyticLineModel = new AnalyticLineModel(saleOrderLine, saleOrder);
    return analyticGroupService.getAnalyticAccountValueMap(
        analyticLineModel, saleOrder.getCompany());
  }
}
