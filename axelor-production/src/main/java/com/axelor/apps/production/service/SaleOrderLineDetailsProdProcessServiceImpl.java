package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.db.repo.SaleOrderLineDetailsRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderLineDetailsProdProcessServiceImpl
    implements SaleOrderLineDetailsProdProcessService {

  protected final SaleOrderLineDetailsProdProcessLineMappingService
      saleOrderLineDetailsProdProcessLineMappingService;

  @Inject
  public SaleOrderLineDetailsProdProcessServiceImpl(
      SaleOrderLineDetailsProdProcessLineMappingService
          saleOrderLineDetailsProdProcessLineMappingService) {
    this.saleOrderLineDetailsProdProcessLineMappingService =
        saleOrderLineDetailsProdProcessLineMappingService;
  }

  @Override
  public Map<String, Object> createSaleOrderLineDetailsFromProdProcess(
      ProdProcess prodProcess, SaleOrderLine saleOrderLine, SaleOrder saleOrder)
      throws AxelorException {
    Map<String, Object> map = new HashMap<>();
    Objects.requireNonNull(prodProcess);

    List<SaleOrderLineDetails> saleOrderLineDetailsList;
    if (saleOrderLine != null
        && CollectionUtils.isNotEmpty(saleOrderLine.getSaleOrderLineDetailsList())) {
      saleOrderLineDetailsList =
          saleOrderLine.getSaleOrderLineDetailsList().stream()
              .filter(line -> line.getTypeSelect() != SaleOrderLineDetailsRepository.TYPE_OPERATION)
              .collect(Collectors.toList());
    } else {
      saleOrderLineDetailsList = new ArrayList<>();
    }

    for (ProdProcessLine prodProcessLine : prodProcess.getProdProcessLineList()) {
      var saleOrderLineDetails =
          saleOrderLineDetailsProdProcessLineMappingService.mapToSaleOrderLineDetails(
              prodProcessLine, saleOrder);
      if (saleOrderLineDetails != null) {
        saleOrderLineDetailsList.add(saleOrderLineDetails);
      }
    }
    saleOrderLine.setSaleOrderLineDetailsList(saleOrderLineDetailsList);
    map.put("saleOrderLineDetailsList", saleOrderLineDetailsList);
    return map;
  }
}
