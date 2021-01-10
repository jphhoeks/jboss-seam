package org.jboss.seam.mock;

import java.util.Collection;
import java.util.Collections;

import javax.faces.context.PartialResponseWriter;
import javax.faces.context.PartialViewContext;
import javax.faces.event.PhaseId;

public class MockPartialViewContext extends PartialViewContext {

	Boolean partialRequest;
	Boolean renderAll;
	Boolean ajaxRequest;

	public MockPartialViewContext() {
		super();
		partialRequest = Boolean.TRUE;
		ajaxRequest = Boolean.TRUE;
		renderAll = Boolean.FALSE;
	}

	@Override
	public Collection<String> getExecuteIds() {
		return Collections.emptyList();
	}

	@Override
	public PartialResponseWriter getPartialResponseWriter() {
		return null;
	}

	@Override
	public Collection<String> getRenderIds() {
		return Collections.emptyList();
	}

	@Override
	public boolean isAjaxRequest() {
		return this.ajaxRequest;
	}

	@Override
	public boolean isExecuteAll() {
		return false;
	}

	@Override
	public boolean isPartialRequest() {
		return this.partialRequest;
	}

	@Override
	public boolean isRenderAll() {
		return this.renderAll;
	}

	@Override
	public void processPartial(PhaseId phaseId) {
		//
	}

	@Override
	public void release() {
		this.renderAll = null;
		this.partialRequest = null;
		this.ajaxRequest = null;
	}

	@Override
	public void setPartialRequest(boolean isPartialRequest) {
		this.partialRequest = isPartialRequest;
	}

	@Override
	public void setRenderAll(boolean renderAll) {
		this.renderAll = renderAll;
	}

}
