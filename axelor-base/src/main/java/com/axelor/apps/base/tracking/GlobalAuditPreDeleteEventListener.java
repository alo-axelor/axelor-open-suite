package com.axelor.apps.base.tracking;

import com.axelor.apps.base.db.GlobalTrackingLog;
import com.axelor.apps.base.db.repo.GlobalTrackingLogRepository;
import com.axelor.auth.db.AuditableModel;
import com.axelor.inject.Beans;
import java.util.Arrays;
import org.hibernate.event.spi.PreDeleteEvent;
import org.hibernate.event.spi.PreDeleteEventListener;

public class GlobalAuditPreDeleteEventListener implements PreDeleteEventListener {

  @Override
  public boolean onPreDelete(PreDeleteEvent event) {
    final Object entity = event.getEntity();
    if (entity instanceof AuditableModel
        && !Arrays.asList(GlobalAuditListener.BLACKLISTED_CLASSES).contains(entity.getClass())) {
      GlobalAuditService globalAuditService = Beans.get(GlobalAuditService.class);
      GlobalTrackingLog log =
          globalAuditService.addLog(
              (AuditableModel) entity, GlobalTrackingLogRepository.TYPE_DELETE);
      /*globalAuditService.completeLog(log, GlobalAuditUtils.currentUser(event.getSession()));*/
    }
    return false;
  }
}
