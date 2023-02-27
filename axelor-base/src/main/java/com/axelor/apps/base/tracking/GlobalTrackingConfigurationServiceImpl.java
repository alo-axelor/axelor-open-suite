package com.axelor.apps.base.tracking;

import com.axelor.apps.base.db.GlobalTrackingConfigurationLine;
import com.axelor.apps.base.db.repo.GlobalTrackingConfigurationLineRepository;
import com.axelor.meta.db.MetaModel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class GlobalTrackingConfigurationServiceImpl implements GlobalTrackingConfigurationService {

  protected GlobalTrackingRepositoryService globalTrackingRepositoryService;


  @Inject
  public GlobalTrackingConfigurationServiceImpl(
          GlobalTrackingRepositoryService globalTrackingRepositoryService) {
      this.globalTrackingRepositoryService = globalTrackingRepositoryService;
  }

/*  @Override
  public void updateGlobalTrackingConfigLine() {
    List<GlobalTrackingConfigurationLine> globalTrackingConfigurationLineList =
        return globalTrackingConfigurationLineRepository.all().fetch();
    modelNameList =
        globalTrackingConfigurationLineList.stream()
            .map(GlobalTrackingConfigurationLine::getMetaModel)
            .map(MetaModel::getName)
            .collect(Collectors.toList());
  }*/

/*  @Override
  public boolean isTracked(Object entity) {
    *//*if(modelNameList != null && !modelNameList.isEmpty()){
      return modelNameList.contains(entity.getClass().getSimpleName());
    }*//*
    return false;
  }*/

  @Override
  public List<String> getModelNameList() {
    return globalTrackingRepositoryService.getGlobalTrackingConfigList().stream()
            .map(GlobalTrackingConfigurationLine::getMetaModel)
            .map(MetaModel::getName)
            .collect(Collectors.toList());
  }
}
