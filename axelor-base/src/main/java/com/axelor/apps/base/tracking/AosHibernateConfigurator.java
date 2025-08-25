package com.axelor.apps.base.tracking;

import com.axelor.db.audit.HibernateListenerConfigurator;
import com.google.inject.Inject;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;

public class AosHibernateConfigurator implements HibernateListenerConfigurator {

  private final GlobalAuditPreUpdateEventListener globalAuditPreUpdateEventListener;
  private final GlobalAuditPreInsertEventListener globalAuditPreInsertEventListener;
  private final GlobalAuditPreCollectionUpdateEventListener
      globalAuditPreCollectionUpdateEventListener;
  private final GlobalAuditPreDeleteEventListener globalAuditPreDeleteEventListener;
  private final GlobalAuditPostInsertEventListener globalAuditPostInsertEventListener;

  @Inject
  public AosHibernateConfigurator(
      GlobalAuditPreUpdateEventListener globalAuditPreUpdateEventListener,
      GlobalAuditPreInsertEventListener globalAuditPreInsertEventListener,
      GlobalAuditPreCollectionUpdateEventListener globalAuditPreCollectionUpdateEventListener,
      GlobalAuditPreDeleteEventListener globalAuditPreDeleteEventListener,
      GlobalAuditPostInsertEventListener globalAuditPostInsertEventListener) {
    this.globalAuditPreUpdateEventListener = globalAuditPreUpdateEventListener;
    this.globalAuditPreInsertEventListener = globalAuditPreInsertEventListener;
    this.globalAuditPreCollectionUpdateEventListener = globalAuditPreCollectionUpdateEventListener;
    this.globalAuditPreDeleteEventListener = globalAuditPreDeleteEventListener;
    this.globalAuditPostInsertEventListener = globalAuditPostInsertEventListener;
  }

  @Override
  public void registerListeners(EventListenerRegistry eventListenerRegistry) {
    eventListenerRegistry.appendListeners(EventType.PRE_UPDATE, globalAuditPreUpdateEventListener);
    eventListenerRegistry.appendListeners(EventType.PRE_INSERT, globalAuditPreInsertEventListener);
    eventListenerRegistry.appendListeners(
        EventType.PRE_COLLECTION_UPDATE, globalAuditPreCollectionUpdateEventListener);
    eventListenerRegistry.appendListeners(EventType.PRE_DELETE, globalAuditPreDeleteEventListener);
    eventListenerRegistry.appendListeners(
        EventType.POST_INSERT, globalAuditPostInsertEventListener);
  }
}
