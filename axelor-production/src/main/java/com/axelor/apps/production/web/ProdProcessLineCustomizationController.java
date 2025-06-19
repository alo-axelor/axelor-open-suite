package com.axelor.apps.production.web;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.repo.ProdProcessLineRepository;
import com.axelor.apps.production.service.SolDetailsProdProcessSyncService;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.db.mapper.Mapper;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProdProcessLineCustomizationController {

  public void copyProdProcess(ActionRequest request, ActionResponse response) {
    Context context = request.getContext();
    SaleOrderLine saleOrderLine;
    ProdProcessLineRepository prodProcessLineRepository =
        Beans.get(ProdProcessLineRepository.class);
    if (context.get("_saleOrderLineId") != null) {
      SaleOrderLineRepository saleOrderLineRepository = Beans.get(SaleOrderLineRepository.class);
      saleOrderLine =
          saleOrderLineRepository.find(((Integer) context.get("_saleOrderLineId")).longValue());
      List<Map<String, Object>> prodProcessLineMapList = new ArrayList<>();
      for (ProdProcessLine prodProcessLine :
          saleOrderLine.getProdProcess().getProdProcessLineList()) {
        prodProcessLineMapList.add(
            Mapper.toMap(prodProcessLineRepository.copy(prodProcessLine, true)));
      }

      response.setValue("$prodProcessLineList", prodProcessLineMapList);
      response.setValue("$isPersonalized", saleOrderLine.getProdProcess().getIsPersonalized());
    }
  }

  public void customize(ActionRequest request, ActionResponse response) throws AxelorException {
    Context context = request.getContext();
    SaleOrderLine saleOrderLine;
    List<Map<String, Object>> prodProcessLineMapList;
    if (context.get("prodProcessLineList") != null && context.get("_saleOrderLineId") != null) {
      prodProcessLineMapList = (List<Map<String, Object>>) context.get("prodProcessLineList");
      SaleOrderLineRepository saleOrderLineRepository = Beans.get(SaleOrderLineRepository.class);
      saleOrderLine =
          saleOrderLineRepository.find(((Integer) context.get("_saleOrderLineId")).longValue());
    } else {
      return;
    }
    Beans.get(SolDetailsProdProcessSyncService.class)
        .customizeProdProcessAndUpdateSol(saleOrderLine, prodProcessLineMapList);
    response.setCanClose(true);
  }

  public void update(ActionRequest request, ActionResponse response) throws AxelorException {
    Context context = request.getContext();
    SaleOrderLine saleOrderLine;
    List<Map<String, Object>> prodProcessLineMapList;
    if (context.get("prodProcessLineList") != null && context.get("_saleOrderLineId") != null) {
      prodProcessLineMapList = (List<Map<String, Object>>) context.get("prodProcessLineList");
      SaleOrderLineRepository saleOrderLineRepository = Beans.get(SaleOrderLineRepository.class);
      saleOrderLine =
          saleOrderLineRepository.find(((Integer) context.get("_saleOrderLineId")).longValue());
    } else {
      return;
    }
        Beans.get(SolDetailsProdProcessSyncService.class)
        .updateSolDetailsProdProcessLine(saleOrderLine, prodProcessLineMapList);
    response.setCanClose(true);
  }
}
