package com.axelor.apps.base.tracking;

import com.axelor.event.Observes;
import com.axelor.events.StartupEvent;
import com.google.inject.Inject;

public class GlobalConfigurationStartup {

    protected GlobalTrackingConfigurationService globalTrackingConfigurationService;

    @Inject
    public GlobalConfigurationStartup(GlobalTrackingConfigurationService globalTrackingConfigurationService){
        this.globalTrackingConfigurationService = globalTrackingConfigurationService;
    }

    /*public void initConfigLines(@Observes StartupEvent event){
        globalTrackingConfigurationService.updateGlobalTrackingConfigLine();
    }*/
}
