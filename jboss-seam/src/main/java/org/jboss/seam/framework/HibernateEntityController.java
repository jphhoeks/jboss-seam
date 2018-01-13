package org.jboss.seam.framework;

import java.io.Serializable;

import org.hibernate.Criteria;
import org.hibernate.Filter;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;

/**
 * Base class for controller objects that perform
 * persistence operations using Hibernate. Adds
 * convenience methods for access to the Hibernate
 * Session object.
 * 
 * @author Gavin King
 *
 */
public class HibernateEntityController extends PersistenceController<Session>
{
   
   private static final long serialVersionUID = 1L;

public Session getSession()
   {
      return getPersistenceContext();
   }
   
   public void setSession(Session session)
   {
      setPersistenceContext(session);
   }

   @Override
   protected String getPersistenceContextName()
   {
      return "hibernateSession";
   }
   
   protected Criteria createCriteria(Class<?> clazz)
   {
      return getSession().createCriteria(clazz);
   }

   protected Query createQuery(String hql) throws HibernateException
   {
      return getSession().createQuery(hql);
   }

   protected SQLQuery createSQLQuery(String sql) throws HibernateException
   {
      return getSession().createSQLQuery(sql);
   }

   protected void delete(Object entity) throws HibernateException
   {
      getSession().delete(entity);
   }

   protected Filter enableFilter(String name)
   {
      return getSession().enableFilter(name);
   }

   protected void flush() throws HibernateException
   {
      getSession().flush();
   }


   protected <T> T get(Class<T> clazz, Serializable id, LockMode lockMode) throws HibernateException
   {
      return get(clazz, id, new LockOptions(lockMode));
   }
   @SuppressWarnings("unchecked")
   protected <T> T get(Class<T> clazz, Serializable id, LockOptions lockOptions) throws HibernateException
   {
      return (T) getSession().get(clazz, id, lockOptions);
   }

   @SuppressWarnings("unchecked")
   protected <T> T get(Class<T> clazz, Serializable id) throws HibernateException
   {
      return (T) getSession().get(clazz, id);
   }

   protected Query getNamedQuery(String name) throws HibernateException
   {
      return getSession().getNamedQuery(name);
   }



   protected <T> T load(Class<T> clazz, Serializable id, LockMode lockMode) throws HibernateException
   {
      return get(clazz, id, new LockOptions(lockMode));
   }
   @SuppressWarnings("unchecked")
   protected <T> T load(Class<T> clazz, Serializable id, LockOptions lockOptions) throws HibernateException
   {
      return (T) getSession().load(clazz, id, lockOptions);
   }

   @SuppressWarnings("unchecked")
   protected <T> T load(Class<T> clazz, Serializable id) throws HibernateException
   {
      return (T) getSession().load(clazz, id);
   }



   protected void lock(Object entity, LockMode lockMode) throws HibernateException
   {
	   lock(entity, new LockOptions(lockMode));
   }
   protected void lock(Object entity, LockOptions lockOptions) throws HibernateException {
	   getSession().buildLockRequest(lockOptions).lock(entity);
   }

   @SuppressWarnings("unchecked")
   protected <T> T merge(T entity) throws HibernateException
   {
      return (T) getSession().merge(entity);
   }

   protected void persist(Object entity) throws HibernateException
   {
      getSession().persist(entity);
   }


   protected void refresh(Object entity, LockMode lockMode) throws HibernateException
   {
      refresh(entity, new LockOptions(lockMode));
   }
   protected void refresh(Object entity, LockOptions lockOptions) throws HibernateException
   {
      getSession().refresh(entity, lockOptions);
   }

   protected void refresh(Object entity) throws HibernateException
   {
      getSession().refresh(entity);
   }
   
}
