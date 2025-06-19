package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.sale.db.SaleOrderLine;
import java.util.List;
import java.util.Map;

public interface SolDetailsProdProcessSyncService {

  void customizeProdProcessAndUpdateSol(
      SaleOrderLine saleOrderLine, List<Map<String, Object>> prodProcessLineMapList)
      throws AxelorException;

  void updateSolDetailsProdProcessLine(SaleOrderLine saleOrderLine, List<Map<String, Object>> prodProcessLineMapList) throws AxelorException;
}
