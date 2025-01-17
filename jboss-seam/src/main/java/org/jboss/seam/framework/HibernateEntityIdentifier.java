package org.jboss.seam.framework;

import java.io.Serializable;

import org.hibernate.Session;
import org.jboss.seam.persistence.HibernatePersistenceProvider;
/**
 * 
 * 
 * @deprecated use {@link EntityIdentifier}
 */
@Deprecated
public class HibernateEntityIdentifier extends Identifier<Session> {

	private static final long serialVersionUID = 1L;

	public HibernateEntityIdentifier(Object entity, Session session) {
		super(HibernatePersistenceProvider.instance().getBeanClass(entity), session.getIdentifier(entity));
	}

	public HibernateEntityIdentifier(Class<?> clazz, Object id) {
		super(clazz, id);
	}

	@Override
	public Object find(Session session) {
		if (session == null) {
			throw new IllegalArgumentException("session must not be null");
		}
		return session.get(getClazz(), (Serializable) getId());
	}

}
