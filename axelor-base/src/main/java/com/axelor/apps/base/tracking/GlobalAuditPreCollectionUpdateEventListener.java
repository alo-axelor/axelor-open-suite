package com.axelor.apps.base.tracking;

import com.axelor.apps.base.db.GlobalTrackingLog;
import com.axelor.apps.base.db.GlobalTrackingLogLine;
import com.axelor.apps.base.db.repo.GlobalTrackingLogRepository;
import com.axelor.auth.db.AuditableModel;
import com.axelor.auth.db.User;
import com.axelor.inject.Beans;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.collection.spi.AbstractPersistentCollection;
import org.hibernate.collection.spi.PersistentBag;
import org.hibernate.collection.spi.PersistentSet;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PreCollectionUpdateEvent;
import org.hibernate.event.spi.PreCollectionUpdateEventListener;

public class GlobalAuditPreCollectionUpdateEventListener
    implements PreCollectionUpdateEventListener {

  @Override
  public void onPreUpdateCollection(PreCollectionUpdateEvent event) {
    Object collection = event.getCollection();
    Long key = (Long) event.();
    /*addCollectionModification(collection, key, GlobalAuditUtils.currentUser(event.getSession()), event);*/
  }

  protected void addCollectionModification(
      Object collection, Long id, User user, PostInsertEvent event) {
    event.
    if (collection instanceof AbstractPersistentCollection) {

      AbstractPersistentCollection newValues = null;
      Collection<AuditableModel> oldValues = null;
      if (collection instanceof PersistentSet) {
        // MANY-TO-MANY
        newValues = (PersistentSet) collection;
        oldValues =
            (Collection<AuditableModel>) ((Map<?, ?>) newValues.getStoredSnapshot()).keySet();
      } else if (collection instanceof PersistentBag) {
        // ONE-TO-MANY
        newValues = (PersistentBag) collection;
        oldValues = (Collection<AuditableModel>) newValues.getStoredSnapshot();
      }

      if (newValues == null) {
        return;
      }

      Object owner = newValues.getOwner();

      if (owner == null
          || Arrays.asList(GlobalAuditListener.BLACKLISTED_CLASSES).contains(owner.getClass())
          || !(owner instanceof AuditableModel)) {
        return;
      }

      String fieldName = newValues.getRole().replace(owner.getClass().getCanonicalName() + ".", "");
      GlobalAuditService globalAuditService = Beans.get(GlobalAuditService.class);
      GlobalTrackingLog log =
          globalAuditService.addLog(
              (AuditableModel) owner, GlobalTrackingLogRepository.TYPE_UPDATE);

      List<Long> previousIdList = new ArrayList<>();
      List<Long> newIdList = new ArrayList<>();

      if (CollectionUtils.isNotEmpty(oldValues)) {
        for (AuditableModel oldValue : oldValues) {
          if (oldValue != null) {
            previousIdList.add(oldValue.getId());
          }
        }
      }

      for (AuditableModel newValue : (Collection<AuditableModel>) newValues) {
        if (newValue != null) {
          newIdList.add(newValue.getId());
        }
      }

      createLogLine(log, fieldName, previousIdList, newIdList);

      globalAuditService.completeLog(log, user);
      event.getSession().persist(log);
    }
  }

  protected void createLogLine(
      GlobalTrackingLog log, String fieldName, List<Long> previousIdList, List<Long> newIdList) {
    GlobalTrackingLogLine line =
        log.getGlobalTrackingLogLineList().stream()
            .filter(l -> l.getMetaFieldName().equals(fieldName))
            .findFirst()
            .orElse(null);

    if (line == null) {
      line = new GlobalTrackingLogLine();
      line.setMetaFieldName(fieldName);
      line.setGlobalTrackingLog(log);
      line.setPreviousValue(
          String.format(
              "[%s]",
              previousIdList.stream().map(String::valueOf).collect(Collectors.joining(", "))));
      log.addGlobalTrackingLogLineListItem(line);
    }
    line.setNewValue(
        String.format(
            "[%s]", newIdList.stream().map(String::valueOf).collect(Collectors.joining(", "))));
  }
}
