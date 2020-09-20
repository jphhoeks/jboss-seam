package org.jboss.seam.wicket;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

public abstract class SeamPropertyModel implements IModel {

	private static final long serialVersionUID = 1L;
	private String expression;
	private PropertyModel model;

	public SeamPropertyModel(String expression) {
		this.expression = expression;
	}

	public abstract Object getTarget();

	@Override
	public Object getObject() {
		return getModel().getObject();
	}

	@Override
	public void setObject(Object object) {
		getModel().setObject(object);
	}

	private PropertyModel getModel() {
		if (model == null) {
			model = new PropertyModel(getTarget(), expression);
		}
		return model;
	}

	@Override
	public void detach() {
		model = null;
	}

	public String getPropertyExpression() {
		return getModel().getPropertyExpression();
	}

}
