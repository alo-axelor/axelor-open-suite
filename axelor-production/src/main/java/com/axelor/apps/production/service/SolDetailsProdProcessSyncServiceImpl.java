package com.axelor.apps.production.service;

import com.axelor.apps.base.AxelorException;
import com.axelor.apps.production.db.ObjectDescription;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.ProdProduct;
import com.axelor.apps.production.db.SaleOrderLineDetails;
import com.axelor.apps.production.db.repo.ObjectDescriptionRepository;
import com.axelor.apps.production.db.repo.ProdProcessLineRepository;
import com.axelor.apps.production.db.repo.ProdProductRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.db.mapper.Mapper;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import org.apache.commons.collections.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SolDetailsProdProcessSyncServiceImpl implements SolDetailsProdProcessSyncService {

  protected final ProdProcessService prodProcessService;
  protected final ProdProcessLineRepository prodProcessLineRepository;
  protected final ObjectDescriptionRepository objectDescriptionRepository;
  protected final ProdProductRepository prodProductRepository;
  protected final SaleOrderLineDetailsService saleOrderLineDetailsService;
  protected final SaleOrderLineDetailsPriceService saleOrderLineDetailsPriceService;
  protected final SaleOrderComputeService saleOrderComputeService;

  @Inject
  public SolDetailsProdProcessSyncServiceImpl(ProdProcessService prodProcessService, ProdProcessLineRepository prodProcessLineRepository, ObjectDescriptionRepository objectDescriptionRepository, ProdProductRepository prodProductRepository, SaleOrderLineDetailsService saleOrderLineDetailsService, SaleOrderLineDetailsPriceService saleOrderLineDetailsPriceService, SaleOrderComputeService saleOrderComputeService) {
    this.prodProcessService = prodProcessService;
      this.prodProcessLineRepository = prodProcessLineRepository;
      this.objectDescriptionRepository = objectDescriptionRepository;
      this.prodProductRepository = prodProductRepository;
      this.saleOrderLineDetailsService = saleOrderLineDetailsService;
      this.saleOrderLineDetailsPriceService = saleOrderLineDetailsPriceService;
      this.saleOrderComputeService = saleOrderComputeService;
  }

  @Transactional
  @Override
  public void customizeProdProcessAndUpdateSol(
      ProdProcessLine prodProcessLine, SaleOrderLineDetails saleOrderLineDetails) throws AxelorException {

    ProdProcess newProdProcess =
        prodProcessService.createCustomizedProdProcess(prodProcessLine.getProdProcess(), false);

    SaleOrderLine saleOrderLine = saleOrderLineDetails.getSaleOrderLine();
    List<SaleOrderLineDetails> saleOrderLineDetailsList = saleOrderLine.getSaleOrderLineDetailsList();

    for(SaleOrderLineDetails saleOrderLineDetail : saleOrderLineDetailsList) {
      if(saleOrderLineDetail.getProdProcessLine() != null){
        ProdProcessLine newProdProcessLine = prodProcessLineRepository.copy(saleOrderLineDetail.getProdProcessLine(), true);
        if(prodProcessLineEquals(saleOrderLineDetails.getProdProcessLine(), newProdProcessLine)){
          updateProdProcessLine(prodProcessLine, newProdProcessLine);
        }
        prodProcessLineRepository.save(newProdProcessLine);
        saleOrderLineDetail.setProdProcessLine(newProdProcessLine);
        newProdProcess.addProdProcessLineListItem(newProdProcessLine);
      }
    }

    SaleOrder saleOrder = saleOrderLineDetailsService.getParentSaleOrder(saleOrderLineDetails);
    saleOrderLine.setProdProcess(newProdProcess);
    saleOrderLineDetailsPriceService.computePrices(saleOrderLineDetails, saleOrder, saleOrderLine);
    saleOrderComputeService.computeSaleOrder(saleOrder);
  }

  @Override
  public void updateSolDetailsProdProcessLine(ProdProcessLine prodProcessLine, SaleOrderLineDetails saleOrderLineDetails) {
    ProdProcessLine prodProcessLineToUpdate = saleOrderLineDetails.getProdProcessLine();
    updateProdProcessLine(prodProcessLine, prodProcessLineToUpdate);
  }

  protected void updateProdProcessLine(ProdProcessLine updatedLine, ProdProcessLine lineToUpdate){
    Map<String, Object> updatedLineMap = Mapper.toMap(updatedLine);
    Map<String, Object> nonNullUpdatedLineMap = getNonNullUpdatedLineMap(updatedLineMap);
    for(Map.Entry<String, Object> entry : nonNullUpdatedLineMap.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      Mapper.of(ProdProcessLine.class).set(lineToUpdate, key, value);
    }
    updateObjectDescriptionList(updatedLine, lineToUpdate);
    prodProcessLineRepository.save(lineToUpdate);
  }

  protected Map<String, Object> getNonNullUpdatedLineMap(Map<String, Object> updatedLineMap) {
    Map<String, Object> nonNullUpdatedLineMap = new HashMap<>();
    for(Map.Entry<String, Object> entry : updatedLineMap.entrySet()) {
      if(entry.getValue() != null){
        nonNullUpdatedLineMap.put(entry.getKey(), entry.getValue());
      }
    }
    nonNullUpdatedLineMap.remove("id");
    nonNullUpdatedLineMap.remove("empty");
    nonNullUpdatedLineMap.remove("selected");
    nonNullUpdatedLineMap.remove("prodProcess");
    nonNullUpdatedLineMap.remove("objectDescriptionList");
    nonNullUpdatedLineMap.remove("toConsumeProdProductList");
    return nonNullUpdatedLineMap;
  }

  protected void updateObjectDescriptionList(ProdProcessLine updatedLine, ProdProcessLine lineToUpdate) {
    List<ObjectDescription> objectDescriptionList = updatedLine.getObjectDescriptionList();
    if(CollectionUtils.isNotEmpty(objectDescriptionList)){
      for(ObjectDescription objectDescription : objectDescriptionList){
        ObjectDescription newObjectDescription = objectDescriptionRepository.copy(objectDescription, true);
        newObjectDescription.setProdProcessLine(lineToUpdate);
        objectDescriptionRepository.save(newObjectDescription);
      }
    }
  }

  protected void updateToConsumeProdProductList(ProdProcessLine updatedLine, ProdProcessLine lineToUpdate) {
    List<ProdProduct> toConsumeProdProductList = updatedLine.getToConsumeProdProductList();
    if(CollectionUtils.isNotEmpty(toConsumeProdProductList)){
      for(ProdProduct prodProduct : toConsumeProdProductList){
        ProdProduct newProdProduct = prodProductRepository.copy(prodProduct, true);
        prodProductRepository.save(newProdProduct);
        lineToUpdate.addToConsumeProdProductListItem(newProdProduct);
      }
    }
  }

  protected ProdProcessLine getLineToUpdate(SaleOrderLineDetails saleOrderLineDetails, ProdProcess prodProcess) {
    for(ProdProcessLine newProdProcessLine : prodProcess.getProdProcessLineList()){
      if(prodProcessLineEquals(saleOrderLineDetails.getProdProcessLine(), newProdProcessLine)){
        return newProdProcessLine;
      }
    }

    return null;
  }

  protected boolean prodProcessLineEquals(ProdProcessLine prodProcessLine1, ProdProcessLine prodProcessLine2) {
    return prodProcessLine1.getName().equals(prodProcessLine2.getName());
  }
}
