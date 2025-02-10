package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Unit;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.SaleOrderLineDetailsRepository;
import com.axelor.apps.production.db.repo.WorkCenterRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.studio.db.AppBase;
import com.axelor.studio.db.AppProduction;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Objects;

public class SolDetailsProdProcessLineMappingServiceImpl
    implements SolDetailsProdProcessLineMappingService {

  protected final AppBaseService appBaseService;
  protected final AppProductionService appProductionService;
  protected final SaleOrderLineDetailsPriceService saleOrderLineDetailsPriceService;

  @Inject
  public SolDetailsProdProcessLineMappingServiceImpl(
      AppBaseService appBaseService,
      AppProductionService appProductionService,
      SaleOrderLineDetailsPriceService saleOrderLineDetailsPriceService) {
    this.appBaseService = appBaseService;
    this.appProductionService = appProductionService;
    this.saleOrderLineDetailsPriceService = saleOrderLineDetailsPriceService;
  }

  @Override
  public SaleOrderLineDetails mapToSaleOrderLineDetails(
      SaleOrder saleOrder, SaleOrderLine saleOrderLine, ProdProcessLine prodProcessLine)
      throws AxelorException {
    Objects.requireNonNull(prodProcessLine);

    SaleOrderLineDetails saleOrderLineDetails = getDefaultOperationSolDetails(prodProcessLine);
    saleOrderLineDetailsPriceService.computePrices(saleOrderLineDetails, saleOrder, saleOrderLine);
    setUnit(prodProcessLine, saleOrderLineDetails);

    return saleOrderLineDetails;
  }

  protected SaleOrderLineDetails getDefaultOperationSolDetails(ProdProcessLine prodProcessLine) {
    SaleOrderLineDetails saleOrderLineDetails = new SaleOrderLineDetails();
    saleOrderLineDetails.setProdProcessLine(prodProcessLine);
    saleOrderLineDetails.setTypeSelect(SaleOrderLineDetailsRepository.TYPE_OPERATION);
    saleOrderLineDetails.setQty(BigDecimal.ONE);
    saleOrderLineDetails.setTitle(prodProcessLine.getName());
    return saleOrderLineDetails;
  }

  protected void setUnit(
      ProdProcessLine prodProcessLine, SaleOrderLineDetails saleOrderLineDetails) {
    WorkCenter workCenter = prodProcessLine.getWorkCenter();
    Unit productUnit = prodProcessLine.getProdProcess().getProduct().getUnit();
    int workCenterTypeSelect = workCenter.getWorkCenterTypeSelect();
    switch (workCenterTypeSelect) {
      case WorkCenterRepository.WORK_CENTER_TYPE_HUMAN:
        setHumanUnit(saleOrderLineDetails, workCenter, productUnit);
        break;
      case WorkCenterRepository.WORK_CENTER_TYPE_MACHINE:
        setMachineUnit(saleOrderLineDetails, workCenter, productUnit);
        break;
      default:
        saleOrderLineDetails.setUnit(productUnit);
    }
  }

  protected void setMachineUnit(
      SaleOrderLineDetails saleOrderLineDetails, WorkCenter workCenter, Unit productUnit) {
    AppProduction appProduction = appProductionService.getAppProduction();
    AppBase appBase = appBaseService.getAppBase();
    switch (workCenter.getCostTypeSelect()) {
      case WorkCenterRepository.COST_TYPE_PER_HOUR:
        saleOrderLineDetails.setUnit(appBase.getUnitHours());
        break;
      case WorkCenterRepository.COST_TYPE_PER_CYCLE:
        saleOrderLineDetails.setUnit(appProduction.getCycleUnit());
        break;
      case WorkCenterRepository.COST_TYPE_PER_PIECE:
      default:
        saleOrderLineDetails.setUnit(productUnit);
        break;
    }
  }

  protected void setHumanUnit(
      SaleOrderLineDetails saleOrderLineDetails, WorkCenter workCenter, Unit productUnit) {
    AppBase appBase = appBaseService.getAppBase();
    switch (workCenter.getHrCostTypeSelect()) {
      case WorkCenterRepository.COST_TYPE_PER_HOUR:
        saleOrderLineDetails.setUnit(appBase.getUnitHours());
        break;
      case WorkCenterRepository.COST_TYPE_PER_PIECE:
      default:
        saleOrderLineDetails.setUnit(productUnit);
        break;
    }
  }
}
