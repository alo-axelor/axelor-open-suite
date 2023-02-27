/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2005-2022 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.base.tracking;

import java.io.Serializable;
import java.util.Iterator;
import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.EntityMode;
import org.hibernate.Transaction;
import org.hibernate.type.Type;

@SuppressWarnings("serial")
public class TestEmptyInterceptor extends EmptyInterceptor {

  @Override
  public void onDelete(
      Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {}

  @Override
  public boolean onFlushDirty(
      Object entity,
      Serializable id,
      Object[] currentState,
      Object[] previousState,
      String[] propertyNames,
      Type[] types) {
    return false;
  }

  @Override
  public boolean onLoad(
      Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
    return false;
  }

  @Override
  public boolean onSave(
      Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
    return false;
  }

  @Override
  public void postFlush(Iterator entities) {}

  @Override
  public void preFlush(Iterator entities) {}

  @Override
  public Boolean isTransient(Object entity) {
    return null;
  }

  @Override
  public Object instantiate(String entityName, EntityMode entityMode, Serializable id) {
    return null;
  }

  @Override
  public int[] findDirty(
      Object entity,
      Serializable id,
      Object[] currentState,
      Object[] previousState,
      String[] propertyNames,
      Type[] types) {
    return null;
  }

  @Override
  public String getEntityName(Object object) {
    return null;
  }

  @Override
  public Object getEntity(String entityName, Serializable id) {
    return null;
  }

  @Override
  public void afterTransactionBegin(Transaction tx) {}

  @Override
  public void afterTransactionCompletion(Transaction tx) {}

  @Override
  public void beforeTransactionCompletion(Transaction tx) {}

  @Override
  public String onPrepareStatement(String sql) {
    return sql;
  }

  @Override
  public void onCollectionRemove(Object collection, Serializable key) throws CallbackException {}

  @Override
  public void onCollectionRecreate(Object collection, Serializable key) throws CallbackException {}

  @Override
  public void onCollectionUpdate(Object collection, Serializable key) throws CallbackException {}
}
