package com.axelor.apps.base.tracking;

import com.axelor.apps.base.db.GlobalTrackingConfigurationLine;
import com.axelor.apps.base.db.GlobalTrackingLog;
import com.axelor.apps.base.db.GlobalTrackingLogLine;
import com.axelor.apps.base.db.TraceBack;
import com.axelor.auth.db.Group;
import com.axelor.auth.db.Role;
import com.axelor.mail.db.MailFlags;
import com.axelor.mail.db.MailFollower;
import com.axelor.mail.db.MailMessage;
import com.axelor.meta.db.MetaAction;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaMenu;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.MetaModule;
import com.axelor.meta.db.MetaSelect;
import com.axelor.meta.db.MetaSelectItem;
import com.axelor.meta.db.MetaTranslation;
import com.axelor.meta.db.MetaView;

public class GlobalAuditListener {
  public static final String UPDATED_BY = "updatedBy";
  public static final String UPDATED_ON = "updatedOn";
  public static final String CREATED_BY = "createdBy";
  public static final String CREATED_ON = "createdOn";

  public static final Class[] BLACKLISTED_CLASSES = {
    GlobalTrackingLogLine.class,
    GlobalTrackingLog.class,
    GlobalTrackingConfigurationLine.class,
    MailMessage.class,
    MailFlags.class,
    MailFollower.class,
    MetaModel.class,
    MetaField.class,
    MetaModule.class,
    MetaView.class,
    MetaAction.class,
    MetaTranslation.class,
    MetaMenu.class,
    MetaSelect.class,
    MetaSelectItem.class,
    Group.class,
    Role.class,
    TraceBack.class
  };
}
