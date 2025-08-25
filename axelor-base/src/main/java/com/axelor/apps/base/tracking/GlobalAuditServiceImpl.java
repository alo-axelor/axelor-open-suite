package com.axelor.apps.base.tracking;

import com.axelor.apps.base.db.GlobalTrackingConfigurationLine;
import com.axelor.apps.base.db.GlobalTrackingLog;
import com.axelor.apps.base.db.GlobalTrackingLogLine;
import com.axelor.apps.base.db.repo.GlobalTrackingConfigurationLineRepository;
import com.axelor.apps.base.db.repo.GlobalTrackingLogRepository;
import com.axelor.auth.AuthUtils;
import com.axelor.auth.db.AuditableModel;
import com.axelor.auth.db.User;
import com.axelor.db.EntityHelper;
import com.axelor.db.JPA;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptBindings;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.collections.CollectionUtils;

public class GlobalAuditServiceImpl implements GlobalAuditService {

  private final Cache<String, MetaField> metaFieldCache =
      CacheBuilder.newBuilder()
          .expireAfterWrite(10, TimeUnit.MINUTES)
          .maximumSize(1000)
          .recordStats()
          .build();

  private final Cache<String, List<GlobalTrackingConfigurationLine>>
      globalTrackingConfigurationLineCache =
          CacheBuilder.newBuilder()
              .expireAfterWrite(10, TimeUnit.MINUTES)
              .maximumSize(100)
              .recordStats()
              .build();

  private final Cache<String, MetaModel> metaModelCache =
      CacheBuilder.newBuilder()
          .expireAfterWrite(10, TimeUnit.MINUTES)
          .maximumSize(100)
          .recordStats()
          .build();

  @Override
  public GlobalTrackingLog addLog(AuditableModel entity, int type) {

    GlobalTrackingLog log = new GlobalTrackingLog();
    log.setDateT(LocalDateTime.now());
    log.setMetaModelName(entity.getClass().getSimpleName());
    log.setMetaModel(getMetaModel(log.getMetaModelName()));
    log.setTypeSelect(type);
    log.setUser(AuthUtils.getUser());
    log.setRelatedId(entity.getId());
    log.setRelatedReference(this.getRelatedReference(entity));
    log.setGlobalTrackingLogLineList(new ArrayList<>());

    return log;
  }

  protected String getRelatedReference(AuditableModel entity) {
    Mapper classMapper = Mapper.of(EntityHelper.getEntityClass(entity));

    if (classMapper.getNameField() != null && classMapper.getNameField().getName() != null) {
      String fieldName = classMapper.getNameField().getName();
      return (String) Mapper.toMap(entity).get(fieldName);
    }

    return "";
  }

  @Override
  public GlobalTrackingLog completeLog(GlobalTrackingLog log, User user) {
    List<GlobalTrackingConfigurationLine> configLineList =
        Beans.get(GlobalTrackingConfigurationLineRepository.class)
            .all()
            .filter("self.metaModel.name = ?", log.getMetaModelName())
            .fetch();
    List<GlobalTrackingLogLine> logLinesToSave = new ArrayList<>();

    saveLogLines(log, logLinesToSave, configLineList);
    persistLog(log, user, logLinesToSave, configLineList);
    invalidateAll();
    return log;
  }

  protected void saveLogLines(
      GlobalTrackingLog log,
      List<GlobalTrackingLogLine> logLinesToSave,
      List<GlobalTrackingConfigurationLine> configLineList) {
    ScriptBindings bindings = null;
    if ((CollectionUtils.isNotEmpty(log.getGlobalTrackingLogLineList()))) {
      try {
        bindings =
            new ScriptBindings(
                this.getContext(
                    JPA.find(
                        (Class<Model>) Class.forName(log.getMetaModel().getFullName()),
                        log.getRelatedId())));
      } catch (Exception e) {
      }
      persistLogLine(log, bindings, logLinesToSave, configLineList);
    }
  }

  protected void persistLogLine(
      GlobalTrackingLog log,
      ScriptBindings bindings,
      List<GlobalTrackingLogLine> logLinesToSave,
      List<GlobalTrackingConfigurationLine> configLineList) {

    for (GlobalTrackingLogLine line : log.getGlobalTrackingLogLineList()) {
      GlobalTrackingConfigurationLine configLine =
          configLineList.stream()
              .filter(l -> l.getMetaField().getName().equals(line.getMetaFieldName()))
              .findFirst()
              .orElse(null);

      if (configLine == null
          || !this.canTrack(configLine, log.getTypeSelect())
          || (!Strings.isNullOrEmpty(configLine.getTrackingCondition())
              && !Boolean.TRUE.equals(
                  new GroovyScriptHelper(bindings).eval(configLine.getTrackingCondition())))) {
        continue;
      }
      line.setMetaField(getMetaField(log.getMetaModel().getId(), line.getMetaFieldName()));
      logLinesToSave.add(line);
    }
  }

  protected void persistLog(
      GlobalTrackingLog log,
      User user,
      List<GlobalTrackingLogLine> logLinesToSave,
      List<GlobalTrackingConfigurationLine> configLineList) {
    if (!logLinesToSave.isEmpty()
        || (GlobalTrackingLogRepository.TYPE_DELETE == log.getTypeSelect()
            && configLineList.stream().anyMatch(l -> Boolean.TRUE.equals(l.getTrackDeletion())))) {
      log.getGlobalTrackingLogLineList().stream().forEach(l -> l.setGlobalTrackingLog(null));
      logLinesToSave.stream().forEach(l -> l.setGlobalTrackingLog(log));
      log.setUser(user);
      /*Beans.get(GlobalTrackingLogRepository.class).save(log);*/
      /*event.getSession().persist(log);*/
    }
  }

  protected MetaField getMetaField(Long metaModelId, String fieldName) {
    String key = metaModelId + "::" + fieldName;
    try {
      return metaFieldCache.get(
          key,
          () ->
              Beans.get(MetaFieldRepository.class)
                  .all()
                  .filter("self.metaModel.id = ? AND self.name = ?", metaModelId, fieldName)
                  .fetchOne());
    } catch (ExecutionException e) {
      throw new RuntimeException(
          "Failed to load MetaField " + fieldName + " for model " + metaModelId, e);
    }
  }

  protected List<GlobalTrackingConfigurationLine> getByModelName(String modelName) {
    try {
      return globalTrackingConfigurationLineCache.get(
          modelName,
          () ->
              Beans.get(GlobalTrackingConfigurationLineRepository.class)
                  .all()
                  .filter("self.metaModel.name = ?", modelName)
                  .fetch());
    } catch (ExecutionException e) {
      throw new RuntimeException("Failed to load config lines", e);
    }
  }

  protected MetaModel getMetaModel(String modelName) {
    try {
      return metaModelCache.get(
          modelName, () -> Beans.get(MetaModelRepository.class).findByName(modelName));
    } catch (ExecutionException e) {
      throw new RuntimeException("Failed to load MetaModel for " + modelName, e);
    }
  }

  private Map<String, Object> getContext(Object obj)
      throws IntrospectionException,
          InvocationTargetException,
          IllegalAccessException,
          IllegalArgumentException {
    Map<String, Object> result = new HashMap<>();
    BeanInfo info = Introspector.getBeanInfo(obj.getClass());
    Method reader = null;
    for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
      reader = pd.getReadMethod();
      if (reader != null) {
        result.put(pd.getName(), reader.invoke(obj));
      }
    }
    return result;
  }

  protected boolean canTrack(GlobalTrackingConfigurationLine confLine, int typeSelect) {

    switch (typeSelect) {
      case GlobalTrackingLogRepository.TYPE_CREATE:
        return confLine.getTrackCreation();
      case GlobalTrackingLogRepository.TYPE_READ:
        return confLine.getTrackReading();
      case GlobalTrackingLogRepository.TYPE_UPDATE:
        return confLine.getTrackUpdate();
      case GlobalTrackingLogRepository.TYPE_DELETE:
        return confLine.getTrackDeletion();
      case GlobalTrackingLogRepository.TYPE_EXPORT:
        return confLine.getTrackExport();
      default:
        return false;
    }
  }

  protected void invalidateAll() {
    globalTrackingConfigurationLineCache.invalidateAll();
    metaModelCache.invalidateAll();
    metaFieldCache.invalidateAll();
  }
}
