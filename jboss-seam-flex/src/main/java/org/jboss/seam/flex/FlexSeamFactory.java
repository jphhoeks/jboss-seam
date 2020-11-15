package org.jboss.seam.flex;

import org.jboss.seam.Component;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

import flex.messaging.FlexFactory;
import flex.messaging.FactoryInstance;
import flex.messaging.config.ConfigMap;

public class FlexSeamFactory implements FlexFactory {
	private static final LogProvider log = Logging.getLogProvider(FlexSeamFactory.class);

	String destinationName;
	String componentName;

	public FlexSeamFactory(String destinationName, String componentName) {
		this.componentName = componentName;
		this.destinationName = destinationName;
	}

	@Override
	public void initialize(String id, ConfigMap configMap) {
		if (log.isInfoEnabled()) {
			log.info("!FSF init " + id + " props=" + configMap);
		}
	}

	@Override
	public FactoryInstance createFactoryInstance(String id, ConfigMap properties) {
		if (log.isInfoEnabled()) {
			log.info("!FSF create factory " + id + " props=" + properties);
		}
		return new FactoryInstance(this, id, properties);
	}

	@Override
	public Object lookup(FactoryInstance factory) {
		if (log.isInfoEnabled()) {
			log.info("!FSF lookup " + factory);
		}

		try {
			Object instance = Component.getInstance(componentName, true);
			return instance;
		} catch (Exception e) {
			log.error(e);
			return null;
		}
	}
}
