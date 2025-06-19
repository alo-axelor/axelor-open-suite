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
import com.axelor.apps.production.db.repo.SaleOrderLineDetailsRepository;
import com.axelor.apps.production.db.repo.WorkCenterRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderComputeService;
import com.axelor.apps.sale.service.saleorderline.SaleOrderLineUtils;
import com.axelor.db.mapper.Mapper;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SolDetailsProdProcessSyncServiceImpl implements SolDetailsProdProcessSyncService {

  protected final ProdProcessService prodProcessService;
  protected final ProdProcessLineRepository prodProcessLineRepository;
  protected final ObjectDescriptionRepository objectDescriptionRepository;
  protected final ProdProductRepository prodProductRepository;
  protected final SaleOrderLineDetailsService saleOrderLineDetailsService;
  protected final SaleOrderLineDetailsPriceService saleOrderLineDetailsPriceService;
  protected final SaleOrderComputeService saleOrderComputeService;
  protected final SolDetailsProdProcessLineMappingService solDetailsProdProcessLineMappingService;
  protected final SaleOrderLineDetailsRepository saleOrderLineDetailsRepository;
  protected final SaleOrderLineRepository saleOrderLineRepository;
  protected final SolDetailsProdProcessLineMappingService prodProcessLineMappingService;
  protected final SaleOrderRepository saleOrderRepository;
  protected final WorkCenterRepository workCenterRepository;
  protected final MetaFileRepository metaFileRepository;

  @Inject
  public SolDetailsProdProcessSyncServiceImpl(
          ProdProcessService prodProcessService,
          ProdProcessLineRepository prodProcessLineRepository,
          ObjectDescriptionRepository objectDescriptionRepository,
          ProdProductRepository prodProductRepository,
          SaleOrderLineDetailsService saleOrderLineDetailsService,
          SaleOrderLineDetailsPriceService saleOrderLineDetailsPriceService,
          SaleOrderComputeService saleOrderComputeService,
          SolDetailsProdProcessLineMappingService solDetailsProdProcessLineMappingService,
          SaleOrderLineDetailsRepository saleOrderLineDetailsRepository,
          SaleOrderLineRepository saleOrderLineRepository,
          SolDetailsProdProcessLineMappingService prodProcessLineMappingService,
          SaleOrderRepository saleOrderRepository,
          WorkCenterRepository workCenterRepository, MetaFileRepository metaFileRepository) {
    this.prodProcessService = prodProcessService;
    this.prodProcessLineRepository = prodProcessLineRepository;
    this.objectDescriptionRepository = objectDescriptionRepository;
    this.prodProductRepository = prodProductRepository;
    this.saleOrderLineDetailsService = saleOrderLineDetailsService;
    this.saleOrderLineDetailsPriceService = saleOrderLineDetailsPriceService;
    this.saleOrderComputeService = saleOrderComputeService;
    this.solDetailsProdProcessLineMappingService = solDetailsProdProcessLineMappingService;
    this.saleOrderLineDetailsRepository = saleOrderLineDetailsRepository;
    this.saleOrderLineRepository = saleOrderLineRepository;
    this.prodProcessLineMappingService = prodProcessLineMappingService;
    this.saleOrderRepository = saleOrderRepository;
    this.workCenterRepository = workCenterRepository;
      this.metaFileRepository = metaFileRepository;
  }

  @Transactional(rollbackOn = {Exception.class})
  public void customizeProdProcessAndUpdateSol(
      SaleOrderLine saleOrderLine, List<Map<String, Object>> prodProcessLineMapList)
      throws AxelorException {
    ProdProcess newProdProcess =
        prodProcessService.createCustomizedProdProcess(saleOrderLine.getProdProcess(), false);
    saleOrderLine.setProdProcess(newProdProcess);
    copyAndCustomProcessProcessLine(prodProcessLineMapList, saleOrderLine, newProdProcess);
    saleOrderLineRepository.save(saleOrderLine);
    SaleOrder saleOrder = SaleOrderLineUtils.getParentSol(saleOrderLine).getSaleOrder();
    saleOrderComputeService.computeSaleOrder(saleOrder);
    saleOrderRepository.save(saleOrder);
  }

  @Transactional(rollbackOn = {Exception.class})
  @Override
  public void updateSolDetailsProdProcessLine(SaleOrderLine saleOrderLine, List<Map<String, Object>> prodProcessLineMapList) throws AxelorException {
    for (SaleOrderLineDetails saleOrderLineDetails : saleOrderLine.getSaleOrderLineDetailsList()) {
      ProdProcessLine prodProcessLine = saleOrderLineDetails.getProdProcessLine();
      if (prodProcessLine != null) {
        updateProdProcessLine(prodProcessLineMapList, prodProcessLine);
        prodProcessLineRepository.save(prodProcessLine);
        computeAndSaveSolDetails(saleOrderLine, saleOrderLineDetails, prodProcessLine);
      }
    }
    saleOrderLineRepository.save(saleOrderLine);
    SaleOrder saleOrder = SaleOrderLineUtils.getParentSol(saleOrderLine).getSaleOrder();
    saleOrderComputeService.computeSaleOrder(saleOrder);
    saleOrderRepository.save(saleOrder);
  }

  protected void copyAndCustomProcessProcessLine(
          List<Map<String, Object>> prodProcessLineMapList,
          SaleOrderLine saleOrderLine,
          ProdProcess newProdProcess)
          throws AxelorException {
    for (SaleOrderLineDetails saleOrderLineDetails : saleOrderLine.getSaleOrderLineDetailsList()) {
      if (saleOrderLineDetails.getProdProcessLine() != null) {
        ProdProcessLine newProdProcessLine =
                prodProcessLineRepository.copy(
                        prodProcessLineRepository.find(saleOrderLineDetails.getProdProcessLine().getId()),
                        false);
        updateProdProcessLine(prodProcessLineMapList, newProdProcessLine);
        prodProcessLineRepository.save(newProdProcessLine);
        saleOrderLineDetails.setProdProcessLine(newProdProcessLine);
        newProdProcess.addProdProcessLineListItem(newProdProcessLine);
        computeAndSaveSolDetails(saleOrderLine, saleOrderLineDetails, newProdProcessLine);
      }
    }
  }

  protected void updateProdProcessLine(List<Map<String, Object>> prodProcessLineMapList, ProdProcessLine lineToUpdate) {
    for (Map<String, Object> map : prodProcessLineMapList) {
      if (map.get("name").equals(lineToUpdate.getName())) {
        updateProdProcessLine(map, lineToUpdate);
        break;
      }
    }
  }

  protected void updateProdProcessLine(
      Map<String, Object> prodProcessLineMap, ProdProcessLine lineToUpdate) {
    Map<String, Object> nonNullUpdatedLineMap = getNonNullUpdatedLineMap(prodProcessLineMap);
    for (Map.Entry<String, Object> entry : nonNullUpdatedLineMap.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      Mapper.of(ProdProcessLine.class).set(lineToUpdate, key, value);
    }
    lineToUpdate.setWorkCenter(workCenterRepository.find(lineToUpdate.getWorkCenter().getId()));
    updateObjectDescriptionList(prodProcessLineMap, lineToUpdate);
    prodProcessLineRepository.save(lineToUpdate);
  }

  protected void computeAndSaveSolDetails(SaleOrderLine saleOrderLine, SaleOrderLineDetails saleOrderLineDetails, ProdProcessLine newProdProcessLine) throws AxelorException {
    SaleOrder saleOrder = saleOrderLineDetailsService.getParentSaleOrder(saleOrderLineDetails);
    solDetailsProdProcessLineMappingService.setQty(
            saleOrderLine, newProdProcessLine, saleOrderLineDetails);
    saleOrderLineDetailsPriceService.computePrices(
            saleOrderLineDetails, saleOrder, saleOrderLine);
    saleOrderLineDetailsRepository.save(saleOrderLineDetails);
  }

  protected Map<String, Object> getNonNullUpdatedLineMap(Map<String, Object> updatedLineMap) {
    Map<String, Object> nonNullUpdatedLineMap = new HashMap<>();
    for (Map.Entry<String, Object> entry : updatedLineMap.entrySet()) {
      if (entry.getValue() != null) {
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

  protected void updateObjectDescriptionList(
      Map<String, Object> prodProcessLineMap, ProdProcessLine lineToUpdate) {

    List<Map<String, Object>> objectDescriptionMapList =
        (List<Map<String, Object>>) prodProcessLineMap.get("objectDescriptionList");
    if (CollectionUtils.isEmpty(objectDescriptionMapList)) {
      return;
    }
    for (Map<String, Object> objectDescriptionMap : objectDescriptionMapList) {
      ObjectDescription newObjectDescription =
          Mapper.toBean(ObjectDescription.class, objectDescriptionMap);
      newObjectDescription.setProdProcessLine(lineToUpdate);
      newObjectDescription.setImage(metaFileRepository.find(newObjectDescription.getImage().getId()));
      objectDescriptionRepository.save(newObjectDescription);
    }
  }
}
