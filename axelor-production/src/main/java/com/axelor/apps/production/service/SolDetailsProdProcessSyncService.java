package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.SaleOrderLineDetails;

public interface SolDetailsProdProcessSyncService {

  void customizeProdProcessAndUpdateSol(
      ProdProcessLine prodProcessLine, SaleOrderLineDetails saleOrderLineDetails) throws AxelorException;

  void updateSolDetailsProdProcessLine(ProdProcessLine prodProcessLine, SaleOrderLineDetails saleOrderLineDetails);
}
