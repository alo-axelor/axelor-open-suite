package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.base.db.Company;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.service.ProductCompanyService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.db.repo.SaleOrderLineDetailsRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.MarginComputeService;
import com.axelor.apps.sale.service.app.AppSaleService;
import com.axelor.studio.db.AppBase;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class SaleOrderLineDetailsPriceServiceImpl implements SaleOrderLineDetailsPriceService {

  protected final ProdProcessLineComputeService prodProcessLineComputeService;
  protected final AppBaseService appBaseService;
  protected final MarginComputeService marginComputeService;
  protected final ProductCompanyService productCompanyService;
  protected final AppSaleService appSaleService;

  @Inject
  public SaleOrderLineDetailsPriceServiceImpl(
      ProdProcessLineComputeService prodProcessLineComputeService,
      AppBaseService appBaseService,
      MarginComputeService marginComputeService,
      ProductCompanyService productCompanyService,
      AppSaleService appSaleService) {
    this.prodProcessLineComputeService = prodProcessLineComputeService;
    this.appBaseService = appBaseService;
    this.marginComputeService = marginComputeService;
    this.productCompanyService = productCompanyService;
    this.appSaleService = appSaleService;
  }

  @Override
  public Map<String, Object> computePrices(
      SaleOrderLineDetails saleOrderLineDetails, SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {
    Map<String, Object> lineMap = new HashMap<>();

    BigDecimal qty = saleOrderLineDetails.getQty();

    computeSubTotalCostPrice(saleOrderLineDetails, saleOrder, saleOrderLine, qty);
    computePrice(saleOrderLineDetails);
    computeTotalPrice(saleOrderLineDetails, qty);

    lineMap.putAll(
        marginComputeService.getComputedMarginInfo(
            saleOrder, saleOrderLineDetails, saleOrderLineDetails.getTotalPrice()));

    lineMap.put("subTotalCostPrice", saleOrderLineDetails.getSubTotalCostPrice());
    lineMap.put("costPrice", saleOrderLineDetails.getCostPrice());
    lineMap.put("price", saleOrderLineDetails.getPrice());
    lineMap.put("totalPrice", saleOrderLineDetails.getTotalPrice());
    return lineMap;
  }

  private void computePrice(SaleOrderLineDetails saleOrderLineDetails) {
    int saleOrderLineDetailsTypeSelect = saleOrderLineDetails.getTypeSelect();
    BigDecimal marginCoefficient = saleOrderLineDetails.getMarginCoefficient();
    BigDecimal price;
    BigDecimal costPrice = saleOrderLineDetails.getCostPrice();
    if (saleOrderLineDetailsTypeSelect == SaleOrderLineDetailsRepository.TYPE_OPERATION) {
      price =
          marginCoefficient
              .multiply(costPrice)
              .setScale(appSaleService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
    } else {
      price = saleOrderLineDetails.getPrice();
    }

    saleOrderLineDetails.setPrice(price);
  }

  protected void computeTotalPrice(SaleOrderLineDetails saleOrderLineDetails, BigDecimal qty) {
    BigDecimal totalPrice =
        saleOrderLineDetails
            .getPrice()
            .multiply(qty)
            .setScale(appSaleService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
    saleOrderLineDetails.setTotalPrice(totalPrice);
  }

  protected void computeSubTotalCostPrice(
      SaleOrderLineDetails saleOrderLineDetails,
      SaleOrder saleOrder,
      SaleOrderLine saleOrderLine,
      BigDecimal qty)
      throws AxelorException {
    int saleOrderLineDetailsTypeSelect = saleOrderLineDetails.getTypeSelect();
    if (saleOrderLineDetailsTypeSelect == SaleOrderLineDetailsRepository.TYPE_OPERATION) {
      computeOperationLineCostPrice(saleOrderLine, saleOrderLineDetails);
    } else {
      computeTotalCostPrice(saleOrderLineDetails, saleOrder, qty);
    }
  }

  protected void computeTotalCostPrice(
      SaleOrderLineDetails saleOrderLineDetails, SaleOrder saleOrder, BigDecimal qty)
      throws AxelorException {
    Company company = saleOrder.getCompany();
    Product product = saleOrderLineDetails.getProduct();
    if (product != null && company != null) {
      BigDecimal costPrice = (BigDecimal) productCompanyService.get(product, "costPrice", company);
      BigDecimal totalCostPrice =
          costPrice
              .multiply(qty)
              .setScale(appSaleService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
      saleOrderLineDetails.setCostPrice(costPrice);
      saleOrderLineDetails.setSubTotalCostPrice(totalCostPrice);
    }
  }

  protected void computeOperationLineCostPrice(
      SaleOrderLine saleOrderLine, SaleOrderLineDetails saleOrderLineDetails) {
    ProdProcessLine prodProcessLine = saleOrderLineDetails.getProdProcessLine();
    if (prodProcessLine == null) {
      return;
    }

    AppBase appBase = appBaseService.getAppBase();
    int digitForPrice = appBase.getNbDecimalDigitForUnitPrice();
    BigDecimal qtyToProduce = saleOrderLine.getQtyToProduce();
    BigDecimal totalCost =
        prodProcessLineComputeService.computeLineCost(prodProcessLine, qtyToProduce);

    BigDecimal costPrice = totalCost.divide(qtyToProduce, digitForPrice, RoundingMode.HALF_UP);
    saleOrderLineDetails.setSubTotalCostPrice(costPrice);
    saleOrderLineDetails.setCostPrice(costPrice);
  }
}
