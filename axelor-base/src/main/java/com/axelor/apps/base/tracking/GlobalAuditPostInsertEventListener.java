package com.axelor.apps.base.tracking;

import com.axelor.apps.base.db.GlobalTrackingConfigurationLine;
import com.axelor.apps.base.db.GlobalTrackingLog;
import com.axelor.apps.base.db.GlobalTrackingLogLine;
import com.axelor.apps.base.db.repo.GlobalTrackingConfigurationLineRepository;
import com.axelor.apps.base.db.repo.GlobalTrackingLogRepository;
import com.axelor.auth.db.AuditableModel;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaField;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.persister.entity.EntityPersister;

public class GlobalAuditPostInsertEventListener implements PostInsertEventListener {
  @Override
  public void onPostInsert(PostInsertEvent event) {
            final Object entity = event.getEntity();
/*
    List<GlobalTrackingConfigurationLine> configLineList;
    configLineList =
            Beans.get(GlobalTrackingConfigurationLineRepository.class)
                    .all()
                    .filter("self.metaModel.name = ?", entity.getClass().getSimpleName())
                    .fetch();

    if (Arrays.asList(GlobalAuditListener.BLACKLISTED_CLASSES).contains(entity.getClass())
            || !(entity instanceof AuditableModel)
            || configLineList.isEmpty()) {
        return;
    }

    final EntityPersister persister = event.getPersister();
    final String[] propertyNames = persister.getPropertyNames();
    final Object[] state = event.getState();

    GlobalAuditService globalAuditService = Beans.get(GlobalAuditService.class);
    GlobalTrackingLog log =
            globalAuditService.addLog((AuditableModel) entity, GlobalTrackingLogRepository.TYPE_CREATE);
    createLogLines(propertyNames, state, log, configLineList);
    globalAuditService.completeLog(log, GlobalAuditUtils.currentUser(event.getSession()));
    event.getSession().persist(log);*/
  }

  @Override
  public boolean requiresPostCommitHandling(EntityPersister persister) {
    return false;
  }

  protected void createLogLines(
      String[] propertyNames,
      Object[] state,
      GlobalTrackingLog log,
      List<GlobalTrackingConfigurationLine> configLineList) {
    List<String> metafieldNameList =
        configLineList.stream()
            .map(GlobalTrackingConfigurationLine::getMetaField)
            .map(MetaField::getName)
            .toList();
    for (int i = 0; i < propertyNames.length; i++) {
      if (state[i] == null
          || GlobalAuditListener.CREATED_ON.equals(propertyNames[i])
          || GlobalAuditListener.CREATED_BY.equals(propertyNames[i])
          || !metafieldNameList.contains(propertyNames[i])) {
        continue;
      }
      log.addGlobalTrackingLogLineListItem(createLogLine(propertyNames, state, i));
    }
  }

  protected GlobalTrackingLogLine createLogLine(String[] propertyNames, Object[] state, int i) {
    GlobalTrackingLogLine logLine = new GlobalTrackingLogLine();
    logLine.setMetaFieldName(propertyNames[i]);

    if (state[i] instanceof AuditableModel) {
      logLine.setNewValue(String.valueOf(((AuditableModel) state[i]).getId()));
    } else if (state[i] instanceof Collection) {

      String newVal = "";
      if (CollectionUtils.isNotEmpty((Collection<Object>) state[i])) {
        newVal =
            String.format(
                "[%s]",
                ((Collection<AuditableModel>) state[i])
                    .stream()
                        .map(AuditableModel::getId)
                        .map(String::valueOf)
                        .collect(Collectors.joining(", ")));
      }
      logLine.setNewValue(newVal);
    } else {
      logLine.setNewValue(String.valueOf(Optional.ofNullable(state[i]).orElse("")));
    }

    return logLine;
  }
}
