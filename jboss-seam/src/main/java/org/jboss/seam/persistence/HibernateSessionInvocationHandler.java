package org.jboss.seam.persistence;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.Filter;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.IdentifierLoadAccess;
import org.hibernate.Interceptor;
import org.hibernate.LobHelper;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.NaturalIdLoadAccess;
import org.hibernate.Query;
import org.hibernate.ReplicationMode;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionEventListener;
import org.hibernate.SessionFactory;
import org.hibernate.SharedSessionBuilder;
import org.hibernate.SimpleNaturalIdLoadAccess;
import org.hibernate.Transaction;
import org.hibernate.TypeHelper;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.jdbc.connections.spi.JdbcConnectionAccess;
import org.hibernate.engine.jdbc.spi.JdbcCoordinator;
import org.hibernate.engine.query.spi.sql.NativeSQLQuerySpecification;
import org.hibernate.engine.spi.ActionQueue;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.NamedQueryDefinition;
import org.hibernate.engine.spi.NamedSQLQueryDefinition;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionEventListenerManager;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.jdbc.Work;
import org.hibernate.loader.custom.CustomQuery;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.procedure.ProcedureCall;
import org.hibernate.resource.transaction.TransactionCoordinator;
import org.hibernate.stat.SessionStatistics;

/**
 * InvocationHandler that proxies the Session, and implements EL interpolation
 * in HQL. Needs to implement SessionImplementor because DetachedCriteria casts
 * the Session to SessionImplementor.
 *
 * @author Gavin King
 * @author Emmanuel Bernard
 * @author Mike Youngstrom
 * @author Marek Novotny
 *
 */
@SuppressWarnings("rawtypes")
public class HibernateSessionInvocationHandler<Hibernate> implements InvocationHandler, Serializable, EventSource {

	private static final long serialVersionUID = 4954720887288965536L;

	private Session delegate;

	public HibernateSessionInvocationHandler(Session paramDelegate) {
		this.delegate = paramDelegate;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			if ("createQuery".equals(method.getName()) && method.getParameterTypes().length > 0
					&& method.getParameterTypes()[0].equals(String.class)) {
				return handleCreateQueryWithString(method, args);
			}
			if ("reconnect".equals(method.getName()) && method.getParameterTypes().length == 0) {
				return handleReconnectNoArg(method);
			}
			return method.invoke(delegate, args);
		} catch (InvocationTargetException e) {
			throw e.getTargetException();
		}
	}

	protected Object handleCreateQueryWithString(Method method, Object[] args) throws Throwable {
		if (args[0] == null) {
			//return method.invoke(getDelegate(method), args);
			return method.invoke(delegate, args);
		}
		String ejbql = (String) args[0];
		if (ejbql.indexOf('#') > 0) {
			QueryParser qp = new QueryParser(ejbql);
			Object[] newArgs = args.clone();
			newArgs[0] = qp.getEjbql();
			//Query query = (Query) method.invoke(getDelegate(method), newArgs);
			Query query = (Query) method.invoke(delegate, newArgs);
			for (int i = 0; i < qp.getParameterValueBindings().size(); i++) {
				query.setParameter(QueryParser.getParameterName(i), qp.getParameterValueBindings().get(i).getValue());
			}
			return query;
		} else {
			return method.invoke(delegate, args);
		}
	}

	protected Object handleReconnectNoArg(Method method) throws Throwable {
		throw new UnsupportedOperationException("deprecated");
	}

	@Override
	public Interceptor getInterceptor() {
		return ((SessionImplementor) delegate).getInterceptor();
	}

	@Override
	public void setAutoClear(boolean paramBoolean) {
		((SessionImplementor) delegate).setAutoClear(paramBoolean);
	}

	@Override
	public boolean isTransactionInProgress() {
		return ((SessionImplementor) delegate).isTransactionInProgress();
	}

	@Override
	public void initializeCollection(PersistentCollection paramPersistentCollection, boolean paramBoolean) throws HibernateException {
		((SessionImplementor) delegate).initializeCollection(paramPersistentCollection, paramBoolean);
	}

	@Override
	public Object internalLoad(String paramString, Serializable paramSerializable, boolean paramBoolean1, boolean paramBoolean2)
			throws HibernateException {
		return ((SessionImplementor) delegate).internalLoad(paramString, paramSerializable, paramBoolean1, paramBoolean2);
	}

	@Override
	public Object immediateLoad(String paramString, Serializable paramSerializable) throws HibernateException {
		return ((SessionImplementor) delegate).immediateLoad(paramString, paramSerializable);
	}

	@Override
	public long getTimestamp() {
		return ((SessionImplementor) delegate).getTimestamp();
	}

	@Override
	public SessionFactoryImplementor getFactory() {
		return ((SessionImplementor) delegate).getFactory();
	}

	@Override
	public List list(String paramString, QueryParameters paramQueryParameters) throws HibernateException {
		return ((SessionImplementor) delegate).list(paramString, paramQueryParameters);
	}

	@Override
	public Iterator iterate(String paramString, QueryParameters paramQueryParameters) throws HibernateException {
		return ((SessionImplementor) delegate).iterate(paramString, paramQueryParameters);
	}

	@Override
	public ScrollableResults scroll(String paramString, QueryParameters paramQueryParameters) throws HibernateException {
		return ((SessionImplementor) delegate).scroll(paramString, paramQueryParameters);
	}

	public ScrollableResults scroll(CriteriaImpl paramCriteriaImpl, ScrollMode paramScrollMode) {
		return ((SessionImplementor) delegate).scroll(paramCriteriaImpl, paramScrollMode);
	}

	public List list(CriteriaImpl paramCriteriaImpl) {
		return ((SessionImplementor) delegate).list(paramCriteriaImpl);
	}

	@Override
	public List listFilter(Object paramObject, String paramString, QueryParameters paramQueryParameters) throws HibernateException {
		return ((SessionImplementor) delegate).listFilter(paramObject, paramString, paramQueryParameters);
	}

	@Override
	public Iterator iterateFilter(Object paramObject, String paramString, QueryParameters paramQueryParameters) throws HibernateException {
		return ((SessionImplementor) delegate).iterateFilter(paramObject, paramString, paramQueryParameters);
	}

	@Override
	public EntityPersister getEntityPersister(String paramString, Object paramObject) throws HibernateException {
		return ((SessionImplementor) delegate).getEntityPersister(paramString, paramObject);
	}

	@Override
	public Object getEntityUsingInterceptor(EntityKey paramEntityKey) throws HibernateException {
		return ((SessionImplementor) delegate).getEntityUsingInterceptor(paramEntityKey);
	}

	@Override
	public Serializable getContextEntityIdentifier(Object paramObject) {
		return ((SessionImplementor) delegate).getContextEntityIdentifier(paramObject);
	}

	@Override
	public String bestGuessEntityName(Object paramObject) {
		return ((SessionImplementor) delegate).bestGuessEntityName(paramObject);
	}

	@Override
	public String guessEntityName(Object paramObject) throws HibernateException {
		return ((SessionImplementor) delegate).guessEntityName(paramObject);
	}

	@Override
	public Object instantiate(String paramString, Serializable paramSerializable) throws HibernateException {
		return ((SessionImplementor) delegate).instantiate(paramString, paramSerializable);
	}

	@Override
	public List listCustomQuery(CustomQuery paramCustomQuery, QueryParameters paramQueryParameters) throws HibernateException {
		return ((SessionImplementor) delegate).listCustomQuery(paramCustomQuery, paramQueryParameters);
	}

	@Override
	public ScrollableResults scrollCustomQuery(CustomQuery paramCustomQuery, QueryParameters paramQueryParameters)
			throws HibernateException {
		return ((SessionImplementor) delegate).scrollCustomQuery(paramCustomQuery, paramQueryParameters);
	}

	@Override
	public List list(NativeSQLQuerySpecification paramNativeSQLQuerySpecification, QueryParameters paramQueryParameters)
			throws HibernateException {
		return ((SessionImplementor) delegate).list(paramNativeSQLQuerySpecification, paramQueryParameters);
	}

	@Override
	public ScrollableResults scroll(NativeSQLQuerySpecification paramNativeSQLQuerySpecification, QueryParameters paramQueryParameters)
			throws HibernateException {
		return ((SessionImplementor) delegate).scroll(paramNativeSQLQuerySpecification, paramQueryParameters);
	}

	@Override
	public int getDontFlushFromFind() {
		return ((SessionImplementor) delegate).getDontFlushFromFind();
	}

	@Override
	public PersistenceContext getPersistenceContext() {
		return ((SessionImplementor) delegate).getPersistenceContext();
	}

	@Override
	public int executeUpdate(String paramString, QueryParameters paramQueryParameters) throws HibernateException {
		return ((SessionImplementor) delegate).executeUpdate(paramString, paramQueryParameters);
	}

	@Override
	public int executeNativeUpdate(NativeSQLQuerySpecification paramNativeSQLQuerySpecification, QueryParameters paramQueryParameters)
			throws HibernateException {
		return ((SessionImplementor) delegate).executeNativeUpdate(paramNativeSQLQuerySpecification, paramQueryParameters);
	}

	@Override
	public CacheMode getCacheMode() {
		return ((SessionImplementor) delegate).getCacheMode();
	}

	@Override
	public void setCacheMode(CacheMode paramCacheMode) {
		((SessionImplementor) delegate).setCacheMode(paramCacheMode);
	}

	@Override
	public boolean isOpen() {
		return ((SessionImplementor) delegate).isOpen();
	}

	@Override
	public boolean isConnected() {
		return ((SessionImplementor) delegate).isConnected();
	}

	@Override
	public FlushMode getFlushMode() {
		return ((SessionImplementor) delegate).getFlushMode();
	}

	@Override
	public void setFlushMode(FlushMode paramFlushMode) {
		((SessionImplementor) delegate).setFlushMode(paramFlushMode);
	}

	@Override
	public Connection connection() {
		return ((SessionImplementor) delegate).connection();
	}

	@Override
	public void flush() {
		((SessionImplementor) delegate).flush();
	}

	@Override
	public Query getNamedQuery(String paramString) {
		return ((SessionImplementor) delegate).getNamedQuery(paramString);
	}

	@Override
	public Query getNamedSQLQuery(String paramString) {
		return ((SessionImplementor) delegate).getNamedSQLQuery(paramString);
	}

	@Override
	public boolean isEventSource() {
		return ((SessionImplementor) delegate).isEventSource();
	}

	@Override
	public void afterScrollOperation() {
		((SessionImplementor) delegate).afterScrollOperation();
	}

	@Override
	public boolean isClosed() {
		return ((SessionImplementor) delegate).isClosed();
	}

	@Override
	public boolean isAutoCloseSessionEnabled() {
		return ((SessionImplementor) delegate).isAutoCloseSessionEnabled();
	}

	@Override
	public boolean shouldAutoClose() {
		return ((SessionImplementor) delegate).shouldAutoClose();
	}

	@Override
	public SessionFactory getSessionFactory() {
		return delegate.getSessionFactory();
	}

	@Override
	public void close() throws HibernateException {
		delegate.close();
	}

	@Override
	public void cancelQuery() throws HibernateException {
		delegate.cancelQuery();
	}

	@Override
	public boolean isDirty() throws HibernateException {
		return delegate.isDirty();
	}

	@Override
	public boolean isDefaultReadOnly() {
		return ((HibernateSessionInvocationHandler) delegate).isDefaultReadOnly();
	}

	@Override
	public void setDefaultReadOnly(boolean paramBoolean) {
		((HibernateSessionInvocationHandler) delegate).setDefaultReadOnly(paramBoolean);
	}

	@Override
	public Serializable getIdentifier(Object paramObject) throws HibernateException {
		return delegate.getIdentifier(paramObject);
	}

	@Override
	public boolean contains(Object paramObject) {
		return delegate.contains(paramObject);
	}

	@Override
	public void evict(Object paramObject) throws HibernateException {
		delegate.evict(paramObject);
	}

	@Override
	@Deprecated
	public Object load(Class paramClass, Serializable paramSerializable, LockMode paramLockMode) throws HibernateException {
		return delegate.load(paramClass, paramSerializable, paramLockMode);
	}

	@Override
	@Deprecated
	public Object load(String paramString, Serializable paramSerializable, LockMode paramLockMode) throws HibernateException {
		return delegate.load(paramString, paramSerializable, paramLockMode);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object load(Class paramClass, Serializable paramSerializable) throws HibernateException {
		return delegate.load(paramClass, paramSerializable);
	}

	@Override
	public Object load(String paramString, Serializable paramSerializable) throws HibernateException {
		return delegate.load(paramString, paramSerializable);
	}

	@Override
	public void load(Object paramObject, Serializable paramSerializable) throws HibernateException {
		delegate.load(paramObject, paramSerializable);
	}

	@Override
	public void replicate(Object paramObject, ReplicationMode paramReplicationMode) throws HibernateException {
		delegate.replicate(paramObject, paramReplicationMode);
	}

	@Override
	public void replicate(String paramString, Object paramObject, ReplicationMode paramReplicationMode) throws HibernateException {
		delegate.replicate(paramString, paramObject, paramReplicationMode);
	}

	@Override
	public Serializable save(Object paramObject) throws HibernateException {
		return delegate.save(paramObject);
	}

	@Override
	public Serializable save(String paramString, Object paramObject) throws HibernateException {
		return delegate.save(paramString, paramObject);
	}

	@Override
	public void saveOrUpdate(Object paramObject) throws HibernateException {
		delegate.saveOrUpdate(paramObject);
	}

	@Override
	public void saveOrUpdate(String paramString, Object paramObject) throws HibernateException {
		delegate.saveOrUpdate(paramString, paramObject);
	}

	@Override
	public void update(Object paramObject) throws HibernateException {
		delegate.update(paramObject);
	}

	@Override
	public void update(String paramString, Object paramObject) throws HibernateException {
		delegate.update(paramString, paramObject);
	}

	@Override
	public Object merge(Object paramObject) throws HibernateException {
		return delegate.merge(paramObject);
	}

	@Override
	public Object merge(String paramString, Object paramObject) throws HibernateException {
		return delegate.merge(paramString, paramObject);
	}

	@Override
	public void persist(Object paramObject) throws HibernateException {
		delegate.persist(paramObject);
	}

	@Override
	public void persist(String paramString, Object paramObject) throws HibernateException {
		delegate.persist(paramString, paramObject);
	}

	@Override
	public void delete(Object paramObject) throws HibernateException {
		delegate.delete(paramObject);
	}

	@Override
	public void delete(String paramString, Object paramObject) throws HibernateException {
		((EventSource) delegate).delete(paramString, paramObject);
	}

	@Override
	@Deprecated
	public void lock(Object paramObject, LockMode paramLockMode) throws HibernateException {
		delegate.lock(paramObject, paramLockMode);
	}

	@Override
	@Deprecated
	public void lock(String paramString, Object paramObject, LockMode paramLockMode) throws HibernateException {
		delegate.lock(paramString, paramObject, paramLockMode);
	}

	@Override
	public void refresh(Object paramObject) throws HibernateException {
		delegate.refresh(paramObject);
	}

	@Override
	@Deprecated
	public void refresh(Object paramObject, LockMode paramLockMode) throws HibernateException {
		delegate.refresh(paramObject, paramLockMode);
	}

	@Override
	public LockMode getCurrentLockMode(Object paramObject) throws HibernateException {
		return delegate.getCurrentLockMode(paramObject);
	}

	@Override
	public Transaction beginTransaction() throws HibernateException {
		return delegate.beginTransaction();
	}

	@Override
	public Transaction getTransaction() {
		return delegate.getTransaction();
	}

	@Override
	public Criteria createCriteria(Class paramClass) {
		return delegate.createCriteria(paramClass);
	}

	@Override
	public Criteria createCriteria(Class paramClass, String paramString) {
		return delegate.createCriteria(paramClass, paramString);
	}

	@Override
	public Criteria createCriteria(String paramString) {
		return delegate.createCriteria(paramString);
	}

	@Override
	public Criteria createCriteria(String paramString1, String paramString2) {
		return delegate.createCriteria(paramString1, paramString2);
	}

	@Override
	public Query createQuery(String paramString) throws HibernateException {
		return delegate.createQuery(paramString);
	}

	@Override
	public Query createQuery(NamedQueryDefinition namedQueryDefinition) {
		return ((SessionImplementor) delegate).createQuery(namedQueryDefinition);
	}

	@Override
	public SQLQuery createSQLQuery(String paramString) throws HibernateException {
		return delegate.createSQLQuery(paramString);
	}

	@Override
	public SQLQuery createSQLQuery(NamedSQLQueryDefinition namedQueryDefinition) {
		return ((SessionImplementor) delegate).createSQLQuery(namedQueryDefinition);
	}

	@Override
	public Query createFilter(Object paramObject, String paramString) throws HibernateException {
		return delegate.createFilter(paramObject, paramString);
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public Object get(Class paramClass, Serializable paramSerializable) throws HibernateException {
		return delegate.get(paramClass, paramSerializable);
	}

	@Override
	@Deprecated
	public Object get(Class paramClass, Serializable paramSerializable, LockMode paramLockMode) throws HibernateException {
		return delegate.get(paramClass, paramSerializable, paramLockMode);
	}

	@Override
	public Object get(String paramString, Serializable paramSerializable) throws HibernateException {
		return delegate.get(paramString, paramSerializable);
	}

	@Override
	@Deprecated
	public Object get(String paramString, Serializable paramSerializable, LockMode paramLockMode) throws HibernateException {
		return delegate.get(paramString, paramSerializable, paramLockMode);
	}

	@Override
	public String getEntityName(Object paramObject) throws HibernateException {
		return delegate.getEntityName(paramObject);
	}

	@Override
	public Filter enableFilter(String paramString) {
		return delegate.enableFilter(paramString);
	}

	@Override
	public Filter getEnabledFilter(String paramString) {
		return delegate.getEnabledFilter(paramString);
	}

	@Override
	public void disableFilter(String paramString) {
		delegate.disableFilter(paramString);
	}

	@Override
	public SessionStatistics getStatistics() {
		return delegate.getStatistics();
	}

	@Override
	public boolean isReadOnly(Object paramObject) {
		return ((HibernateSessionInvocationHandler) delegate).isReadOnly(paramObject);
	}

	@Override
	public void setReadOnly(Object paramObject, boolean paramBoolean) {
		delegate.setReadOnly(paramObject, paramBoolean);
	}

	@Override
	public void doWork(Work paramWork) throws HibernateException {
		delegate.doWork(paramWork);
	}

	@Override
	public Connection disconnect() throws HibernateException {
		return delegate.disconnect();
	}

	@Override
	public void reconnect(Connection paramConnection) throws HibernateException {
		delegate.reconnect(paramConnection);
	}

	@Override
	public boolean isFetchProfileEnabled(String paramString) {
		return ((HibernateSessionInvocationHandler) delegate).isFetchProfileEnabled(paramString);
	}

	@Override
	public void enableFetchProfile(String paramString) {
		((HibernateSessionInvocationHandler) delegate).enableFetchProfile(paramString);
	}

	@Override
	public void disableFetchProfile(String paramString) {
		((HibernateSessionInvocationHandler) delegate).disableFetchProfile(paramString);
	}

	@Override
	public ActionQueue getActionQueue() {
		return ((EventSource) delegate).getActionQueue();
	}

	@Override
	public Object instantiate(EntityPersister paramEntityPersister, Serializable paramSerializable) throws HibernateException {
		return ((EventSource) delegate).instantiate(paramEntityPersister, paramSerializable);
	}

	@Override
	public void forceFlush(EntityEntry paramEntityEntry) throws HibernateException {
		((EventSource) delegate).forceFlush(paramEntityEntry);
	}

	@Override
	public void merge(String paramString, Object paramObject, Map paramMap) throws HibernateException {
		((EventSource) delegate).merge(paramString, paramObject, paramMap);
	}

	@Override
	public void persist(String paramString, Object paramObject, Map paramMap) throws HibernateException {
		((EventSource) delegate).persist(paramString, paramObject, paramMap);
	}

	@Override
	public void persistOnFlush(String paramString, Object paramObject, Map paramMap) {
		((EventSource) delegate).persistOnFlush(paramString, paramObject, paramMap);
	}

	@Override
	public void delete(String paramString, Object paramObject, boolean paramBoolean, Set paramSet) {
		((EventSource) delegate).delete(paramString, paramObject, paramBoolean, paramSet);
	}

	@Override
	public String getTenantIdentifier() {
		return delegate.getTenantIdentifier();
	}

	@Override
	public JdbcConnectionAccess getJdbcConnectionAccess() {
		return ((SessionImplementor) delegate).getJdbcConnectionAccess();
	}

	@Override
	public EntityKey generateEntityKey(Serializable id, EntityPersister persister) {
		return ((SessionImplementor) delegate).generateEntityKey(id, persister);
	}

	@Override
	public void disableTransactionAutoJoin() {
		((SessionImplementor) delegate).disableTransactionAutoJoin();
	}

	@Override
	public TransactionCoordinator getTransactionCoordinator() {
		return ((SessionImplementor) delegate).getTransactionCoordinator();
	}

	@Override
	public JdbcCoordinator getJdbcCoordinator() {
		return ((SessionImplementor) delegate).getJdbcCoordinator();
	}

	@Override
	public LoadQueryInfluencers getLoadQueryInfluencers() {
		return ((SessionImplementor) delegate).getLoadQueryInfluencers();
	}

	@Override
	public <T> T execute(Callback<T> callback) {
		return ((SessionImplementor) delegate).execute(callback);
	}

	@Override
	public SharedSessionBuilder sessionWithOptions() {
		return ((EventSource) delegate).sessionWithOptions();
	}

	@Override
	public Object load(Class theClass, Serializable id, LockOptions lockOptions) throws HibernateException {
		return ((EventSource) delegate).load(theClass, id, lockOptions);
	}

	@Override
	public Object load(String entityName, Serializable id, LockOptions lockOptions) throws HibernateException {
		return ((EventSource) delegate).load(entityName, id, lockOptions);
	}

	@Override
	public LockRequest buildLockRequest(LockOptions lockOptions) {
		return ((EventSource) delegate).buildLockRequest(lockOptions);
	}

	@Override
	public void refresh(String entityName, Object object) throws HibernateException {
		((EventSource) delegate).refresh(entityName, object);
	}

	@Override
	public void refresh(Object object, LockOptions lockOptions) throws HibernateException {
		((EventSource) delegate).refresh(object, lockOptions);
	}

	@Override
	public void refresh(String entityName, Object object, LockOptions lockOptions) throws HibernateException {
		((EventSource) delegate).refresh(entityName, object, lockOptions);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object get(Class clazz, Serializable id, LockOptions lockOptions) throws HibernateException {
		return ((EventSource) delegate).get(clazz, id, lockOptions);
	}

	@Override
	public Object get(String entityName, Serializable id, LockOptions lockOptions) throws HibernateException {
		return ((EventSource) delegate).get(entityName, id, lockOptions);
	}

	@Override
	public <T> T doReturningWork(ReturningWork<T> work) throws HibernateException {
		return ((EventSource) delegate).doReturningWork(work);
	}

	@Override
	public TypeHelper getTypeHelper() {
		return ((EventSource) delegate).getTypeHelper();
	}

	@Override
	public LobHelper getLobHelper() {
		return ((EventSource) delegate).getLobHelper();
	}

	@Override
	public IdentifierLoadAccess byId(String entityName) {
		return delegate.byId(entityName);
	}

	@Override
	public IdentifierLoadAccess byId(Class entityClass) {
		return delegate.byId(entityClass);
	}

	@Override
	public NaturalIdLoadAccess byNaturalId(String entityName) {
		return delegate.byNaturalId(entityName);
	}

	@Override
	public NaturalIdLoadAccess byNaturalId(Class entityClass) {
		return delegate.byNaturalId(entityClass);
	}

	@Override
	public SimpleNaturalIdLoadAccess bySimpleNaturalId(String entityName) {
		return delegate.bySimpleNaturalId(entityName);
	}

	@Override
	public SimpleNaturalIdLoadAccess bySimpleNaturalId(Class entityClass) {
		return delegate.bySimpleNaturalId(entityClass);
	}

	@Override
	public ScrollableResults scroll(Criteria criteria, ScrollMode scrollMode) {
		return ((SessionImplementor) delegate).scroll(criteria, scrollMode);
	}

	@Override
	public List list(Criteria criteria) {
		return ((SessionImplementor) delegate).list(criteria);
	}

	@Override
	public void removeOrphanBeforeUpdates(String entityName, Object child) {
		((EventSource) delegate).removeOrphanBeforeUpdates(entityName, child);
	}

	@Override
	public SessionEventListenerManager getEventListenerManager() {
		return ((SessionImplementor) delegate).getEventListenerManager();
	}

	@Override
	public void addEventListeners(SessionEventListener... listeners) {
		delegate.addEventListeners(listeners);
	}

	@Override
	public ProcedureCall getNamedProcedureCall(String name) {
		return delegate.getNamedProcedureCall(name);
	}

	@Override
	public ProcedureCall createStoredProcedureCall(String procedureName) {
		return delegate.createStoredProcedureCall(procedureName);
	}

	@Override
	public ProcedureCall createStoredProcedureCall(String procedureName, Class... resultClasses) {
		return delegate.createStoredProcedureCall(procedureName, resultClasses);
	}

	@Override
	public ProcedureCall createStoredProcedureCall(String procedureName, String... resultSetMappings) {
		return delegate.createStoredProcedureCall(procedureName, resultSetMappings);
	}

	@Override
	public void refresh(String entityName, Object object, Map refreshedAlready) throws HibernateException {
		((EventSource) delegate).refresh(entityName, object, refreshedAlready);
	}

}
