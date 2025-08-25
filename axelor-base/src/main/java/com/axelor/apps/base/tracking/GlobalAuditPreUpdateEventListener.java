package com.axelor.apps.base.tracking;

import com.axelor.apps.base.db.GlobalTrackingConfigurationLine;
import com.axelor.apps.base.db.GlobalTrackingLog;
import com.axelor.apps.base.db.GlobalTrackingLogLine;
import com.axelor.apps.base.db.repo.GlobalTrackingConfigurationLineRepository;
import com.axelor.apps.base.db.repo.GlobalTrackingLogRepository;
import com.axelor.auth.db.AuditableModel;
import com.axelor.inject.Beans;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;

public class GlobalAuditPreUpdateEventListener implements PreUpdateEventListener {

  @Override
  public boolean onPreUpdate(PreUpdateEvent event) {
    final Object entity = event.getEntity();

    List<GlobalTrackingConfigurationLine> configLineList;
    configLineList =
        Beans.get(GlobalTrackingConfigurationLineRepository.class)
            .all()
            .filter("self.metaModel.name = ?", entity.getClass().getSimpleName())
            .fetch();

    if (Arrays.asList(GlobalAuditListener.BLACKLISTED_CLASSES).contains(entity.getClass())
        || !(entity instanceof AuditableModel)
        || configLineList.isEmpty()) {
      return false;
    }

    final EntityPersister persister = event.getPersister();
    final String[] propertyNames = persister.getPropertyNames();
    Object[] currentState = event.getState();
    Object[] previousState = event.getOldState();

    GlobalAuditService globalAuditService = Beans.get(GlobalAuditService.class);
    GlobalTrackingLog log =
        globalAuditService.addLog((AuditableModel) entity, GlobalTrackingLogRepository.TYPE_UPDATE);

    createLogLines(propertyNames, currentState, previousState, log);
    globalAuditService.completeLog(log, GlobalAuditUtils.currentUser(event.getSession()));
    return false;
  }

  protected void createLogLines(
      String[] propertyNames,
      Object[] currentState,
      Object[] previousState,
      GlobalTrackingLog log) {
    for (int i = 0; i < propertyNames.length; i++) {

      if (Objects.equals(currentState[i], previousState[i])
          || GlobalAuditListener.UPDATED_ON.equals(propertyNames[i])
          || GlobalAuditListener.UPDATED_BY.equals(propertyNames[i])) {
        continue;
      }

      GlobalTrackingLogLine logLine = createLogLine(propertyNames, i, currentState, previousState);
      log.addGlobalTrackingLogLineListItem(logLine);
    }
  }

  protected GlobalTrackingLogLine createLogLine(
      String[] propertyNames, int i, Object[] currentState, Object[] previousState) {
    GlobalTrackingLogLine logLine = new GlobalTrackingLogLine();
    logLine.setMetaFieldName(propertyNames[i]);

    if (currentState[i] instanceof AuditableModel || previousState[i] instanceof AuditableModel) {

      logLine.setNewValue(
          currentState[i] instanceof AuditableModel
              ? String.valueOf(((AuditableModel) currentState[i]).getId())
              : "");
      logLine.setPreviousValue(
          previousState[i] instanceof AuditableModel
              ? String.valueOf(((AuditableModel) previousState[i]).getId())
              : "");

    } else if (currentState[i] instanceof Collection || previousState[i] instanceof Collection) {

      String prevVal = "";
      String newVal = "";
      if (CollectionUtils.isNotEmpty((Collection<Object>) previousState[i])) {
        prevVal =
            String.format(
                "[%s]",
                ((Collection<AuditableModel>) previousState[i])
                    .stream()
                        .map(AuditableModel::getId)
                        .map(String::valueOf)
                        .collect(Collectors.joining(", ")));
      }
      if (CollectionUtils.isNotEmpty((Collection<Object>) currentState[i])) {
        newVal =
            String.format(
                "[%s]",
                ((Collection<AuditableModel>) currentState[i])
                    .stream()
                        .map(AuditableModel::getId)
                        .map(String::valueOf)
                        .collect(Collectors.joining(", ")));
      }
      logLine.setPreviousValue(prevVal);
      logLine.setNewValue(newVal);

    } else {
      logLine.setNewValue(String.valueOf(Optional.ofNullable(currentState[i]).orElse("")));
      logLine.setPreviousValue(String.valueOf(Optional.ofNullable(previousState[i]).orElse("")));
    }
    return logLine;
  }
}
