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

    lineMap.putAll(computeSubTotalCostPrice(saleOrderLineDetails, saleOrder, saleOrderLine));
    lineMap.putAll(computePrice(saleOrderLineDetails));
    lineMap.putAll(computeTotalPrice(saleOrderLineDetails, saleOrder));

    return lineMap;
  }

  @Override
  public Map<String, Object> computePrice(SaleOrderLineDetails saleOrderLineDetails) {
    Map<String, Object> lineMap = new HashMap<>();
    BigDecimal marginCoefficient = saleOrderLineDetails.getMarginCoefficient();
    BigDecimal price;
    BigDecimal costPrice = saleOrderLineDetails.getCostPrice();

    price =
        marginCoefficient
            .multiply(costPrice)
            .setScale(appSaleService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);

    saleOrderLineDetails.setPrice(price);
    lineMap.put("price", saleOrderLineDetails.getPrice());
    return lineMap;
  }

  @Override
  public Map<String, Object> computeTotalPrice(
      SaleOrderLineDetails saleOrderLineDetails, SaleOrder saleOrder) throws AxelorException {
    Map<String, Object> lineMap = new HashMap<>();
    BigDecimal qty = saleOrderLineDetails.getQty();
    BigDecimal totalPrice =
        saleOrderLineDetails
            .getPrice()
            .multiply(qty)
            .setScale(appSaleService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
    saleOrderLineDetails.setTotalPrice(totalPrice);

    lineMap.put("totalPrice", saleOrderLineDetails.getTotalPrice());
    lineMap.putAll(
        marginComputeService.getComputedMarginInfo(
            saleOrder, saleOrderLineDetails, saleOrderLineDetails.getTotalPrice()));
    return lineMap;
  }

  protected Map<String, Object> computeSubTotalCostPrice(
      SaleOrderLineDetails saleOrderLineDetails, SaleOrder saleOrder, SaleOrderLine saleOrderLine)
      throws AxelorException {
    Map<String, Object> lineMap = new HashMap<>();
    int saleOrderLineDetailsTypeSelect = saleOrderLineDetails.getTypeSelect();
    if (saleOrderLineDetailsTypeSelect == SaleOrderLineDetailsRepository.TYPE_OPERATION) {
      computeOperationLineCostPrice(saleOrderLine, saleOrderLineDetails);
    } else {
      computeTotalCostPrice(saleOrderLineDetails, saleOrder);
    }

    lineMap.put("subTotalCostPrice", saleOrderLineDetails.getSubTotalCostPrice());
    lineMap.put("costPrice", saleOrderLineDetails.getCostPrice());
    return lineMap;
  }

  protected void computeTotalCostPrice(
      SaleOrderLineDetails saleOrderLineDetails, SaleOrder saleOrder) throws AxelorException {
    Company company = saleOrder.getCompany();
    Product product = saleOrderLineDetails.getProduct();
    BigDecimal qty = saleOrderLineDetails.getQty();
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

  @Override
  public Map<String, Object> computeMarginCoef(SaleOrderLineDetails saleOrderLineDetails) {
    Map<String, Object> lineMap = new HashMap<>();
    BigDecimal costPrice = saleOrderLineDetails.getCostPrice();
    BigDecimal price = saleOrderLineDetails.getPrice();
    BigDecimal marginCoef =
        price.divide(
            costPrice, appSaleService.getNbDecimalDigitForUnitPrice(), RoundingMode.HALF_UP);
    saleOrderLineDetails.setMarginCoefficient(marginCoef);
    lineMap.put("marginCoefficient", saleOrderLineDetails.getMarginCoefficient());
    return lineMap;
  }
}
