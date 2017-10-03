package org.jboss.seam.mock;

import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.faces.render.ResponseStateManager;

public class MockResponseStateManager extends ResponseStateManager {
	public MockResponseStateManager() {
		super();
	}

	@Override
	@Deprecated
	public Object getComponentStateToRestore(FacesContext ctx) {
		return new Object();
	}

	@Override
	@Deprecated
	public Object getTreeStructureToRestore(FacesContext ctx, String x) {
		return new Object();
	}

	@Override
	@Deprecated
	public void writeState(FacesContext ctx, javax.faces.application.StateManager.SerializedView viewState)
			throws IOException {

	}

	@Override
	public boolean isPostback(FacesContext context) {
		return context.getExternalContext().getRequestParameterMap().containsKey(ResponseStateManager.VIEW_STATE_PARAM);
	}

}
