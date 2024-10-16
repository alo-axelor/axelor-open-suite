package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.repo.SaleOrderLineDetailsRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.google.inject.Inject;
import java.util.Objects;

public class SaleOrderLineDetailsProdProcessLineMappingServiceImpl
    implements SaleOrderLineDetailsProdProcessLineMappingService {

  protected final SaleOrderLineDetailsService saleOrderLineDetailsService;

  @Inject
  public SaleOrderLineDetailsProdProcessLineMappingServiceImpl(
      SaleOrderLineDetailsService saleOrderLineDetailsService) {
    this.saleOrderLineDetailsService = saleOrderLineDetailsService;
  }

  @Override
  public SaleOrderLineDetails mapToSaleOrderLineDetails(
      ProdProcessLine prodProcessLine, SaleOrder saleOrder) throws AxelorException {
    Objects.requireNonNull(prodProcessLine);

    WorkCenter workCenter = prodProcessLine.getWorkCenter();

    if (workCenter != null) {
      SaleOrderLineDetails saleOrderLineDetails = new SaleOrderLineDetails();
      saleOrderLineDetails.setTypeSelect(SaleOrderLineDetailsRepository.TYPE_OPERATION);
      saleOrderLineDetails.setWorkCenter(workCenter);
      saleOrderLineDetails.setTitle(workCenter.getName());

      return saleOrderLineDetails;
    }
    return null;
  }
}
