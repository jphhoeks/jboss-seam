//$Id: EntityManagerFactory.java 6280 2007-09-27 15:29:56Z pmuir $
package org.jboss.seam.persistence;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Persistence;

import org.hibernate.cfg.Environment;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.Unwrap;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.util.Naming;

/**
 * A Seam component that bootstraps an EntityManagerFactory,
 * for use of JPA outside of Java EE 5 / Embedded JBoss.
 * 
 * @author Gavin King
 */
@Scope(ScopeType.APPLICATION)
@BypassInterceptors
@Startup
public class EntityManagerFactory {
	private javax.persistence.EntityManagerFactory jpaEntityManagerFactory;

	private String persistenceUnitName;
	private Map<String, String> persistenceUnitProperties;

	private static final LogProvider log = Logging.getLogProvider(EntityManagerFactory.class);

	public EntityManagerFactory() {
		super();
	}
	
	@Unwrap
	public javax.persistence.EntityManagerFactory getEntityManagerFactory() {
		return jpaEntityManagerFactory;
	}

	@Create
	public void startup(Component component) throws Exception {
		if (persistenceUnitName == null) {
			persistenceUnitName = component.getName();
		}
		jpaEntityManagerFactory = createEntityManagerFactory();
	}

	@Destroy
	public void shutdown() {
		if (jpaEntityManagerFactory != null) {
			jpaEntityManagerFactory.close();
		}
	}

	protected javax.persistence.EntityManagerFactory createEntityManagerFactory() {
		long startTimeNano = System.nanoTime();
		if (log.isInfoEnabled()) {
			log.info("Creating EntityManagerFactory with name:" + persistenceUnitName);
		}
		Map<String, String> properties = new HashMap<String, String>();
		Map<String, String> jndiProperties = Naming.getInitialContextProperties();
		if (jndiProperties != null) {
			// Prefix regular JNDI properties for Hibernate
			for (Map.Entry<String, String> entry : jndiProperties.entrySet()) {
				properties.put(Environment.JNDI_PREFIX + "." + entry.getKey(), entry.getValue());
			}
		}
		if (persistenceUnitProperties != null) {
			properties.putAll(persistenceUnitProperties);
		}

		javax.persistence.EntityManagerFactory retVal = null;
		if (properties.isEmpty()) {
			retVal = Persistence.createEntityManagerFactory(persistenceUnitName);
		} else {
			retVal = Persistence.createEntityManagerFactory(persistenceUnitName, properties);
		}
		long finishTimeNano = System.nanoTime();
		if (log.isInfoEnabled()) {
			log.info("EntityManagerFactory " + persistenceUnitName + " created in " + ((finishTimeNano - startTimeNano)/1000000L) + " ms");
		}

		return retVal;
	}

	/**
	* The persistence unit name
	*/
	public String getPersistenceUnitName() {
		return persistenceUnitName;
	}

	public void setPersistenceUnitName(String persistenceUnitName) {
		this.persistenceUnitName = persistenceUnitName;
	}

	/**
	* Properties to pass to Persistence.createEntityManagerFactory()
	*/
	public Map<String, String> getPersistenceUnitProperties() {
		return persistenceUnitProperties;
	}

	public void setPersistenceUnitProperties(Map<String, String> persistenceUnitProperties) {
		this.persistenceUnitProperties = persistenceUnitProperties;
	}

}
