package com.axelor.apps.base.tracking;

import com.axelor.apps.base.db.GlobalTrackingConfigurationLine;
import com.axelor.apps.base.db.repo.GlobalTrackingConfigurationLineRepository;
import com.google.inject.Inject;

import java.util.List;

public class GlobalTrackingRepositoryServiceImpl implements GlobalTrackingRepositoryService{

    protected GlobalTrackingConfigurationLineRepository globalTrackingConfigurationLineRepository;

    @Inject
    public GlobalTrackingRepositoryServiceImpl(GlobalTrackingConfigurationLineRepository globalTrackingConfigurationLineRepository){
        this.globalTrackingConfigurationLineRepository = globalTrackingConfigurationLineRepository;
    }
    @Override
    public List<GlobalTrackingConfigurationLine> getGlobalTrackingConfigList() {
        return globalTrackingConfigurationLineRepository.all().fetch();
    }
}
