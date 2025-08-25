package com.axelor.apps.base.tracking;

import com.axelor.apps.base.db.GlobalTrackingLog;
import com.axelor.auth.db.AuditableModel;
import com.axelor.auth.db.User;

public interface GlobalAuditService {
  GlobalTrackingLog completeLog(GlobalTrackingLog log, User user);

  GlobalTrackingLog addLog(AuditableModel entity, int type);
}
