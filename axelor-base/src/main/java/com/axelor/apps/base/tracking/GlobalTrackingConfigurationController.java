package com.axelor.apps.base.tracking;

import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public class GlobalTrackingConfigurationController {
  public void updateConfigurationLines(ActionRequest request, ActionResponse response) {
    try {
      /*Beans.get(GlobalTrackingConfigurationService.class).updateGlobalTrackingConfigLine();*/
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }
}
