package org.jboss.seam.navigation;

import java.util.Map;

import org.jboss.seam.core.ConversationIdGenerator;
import org.jboss.seam.core.ConversationPropagation;
import org.jboss.seam.core.Manager;

/**
 * 
 * Seam's default strategy for propagating conversations.
 *
 */
public class SyntheticConversationIdParameter implements ConversationIdParameter {
	@Override
	public String getName() {
		return null;
	}

	@Override
	public String getParameterName() {
		return Manager.instance().getConversationIdParameter();
	}

	@Override
	public String getParameterValue() {
		return Manager.instance().getCurrentConversationId();
	}

	@Override
	public String getParameterValue(String value) {
		return value;
	}

	@Override
	public String getInitialConversationId(Map parameters) {
		return ConversationIdGenerator.instance().getNextId();
	}

	@Override
	public String getConversationId() {
		return ConversationIdGenerator.instance().getNextId();
	}

	@Override
	public String getRequestConversationId(Map parameters) {
		return ConversationPropagation.getRequestParameterValue(parameters, getParameterName());
	}
}
