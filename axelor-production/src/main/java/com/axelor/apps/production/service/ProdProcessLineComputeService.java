package com.axelor.apps.production.service;

import com.axelor.apps.production.db.ProdProcessLine;
import java.math.BigDecimal;

public interface ProdProcessLineComputeService {

  BigDecimal computeLineCost(ProdProcessLine prodProcessLine, BigDecimal qtyToProduce);
}
