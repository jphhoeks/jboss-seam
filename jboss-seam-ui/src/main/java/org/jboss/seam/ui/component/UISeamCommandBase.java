package org.jboss.seam.ui.component;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.el.ValueExpression;
import javax.faces.component.ActionSource2;
import javax.faces.component.UIComponent;
import javax.faces.component.UIData;
import javax.faces.component.UIOutput;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionListener;
import javax.faces.model.DataModel;

import org.jboss.seam.core.PropagationType;
import org.jboss.seam.navigation.Pages;
import org.jboss.seam.ui.util.ViewUrlBuilder;

/**
 * Parent of all Seam command like JSF components 
 * 
 * @author mnovotny
 *
 */
public abstract class UISeamCommandBase extends UIOutput implements ActionSource2 {

	public abstract String getView();

	public String getUrl() throws UnsupportedEncodingException {
		String encodedUrl;
		FacesContext context = getFacesContext();
		String viewId = getView();
		if (viewId == null || viewId.isEmpty()) {
			viewId = Pages.getViewId(getFacesContext());
		}

		// JBSEAM-5097 encoded actionMethod in URL causes SafeActions to fail  
		//ViewUrlBuilder url = new ViewUrlBuilder(viewId, getFragment(), !isPortletRequest(getFacesContext()));
		ViewUrlBuilder url = new ViewUrlBuilder(viewId, getFragment());

		Set<String> usedParameters = new HashSet<String>();
		for (Object child : getChildren()) {
			if (child instanceof UIParameter) {
				usedParameters.add(((UIParameter) child).getName());
				url.addParameter((UIParameter) child);
			}
		}

		if (viewId != null && isIncludePageParams()) {
			Map<String, Object> pageParameters = Pages.instance().getStringValuesFromModel(context, viewId, usedParameters);
			for (Map.Entry<String, Object> me : pageParameters.entrySet()) {
				UIParameter uip = new UIParameter();
				uip.setName(me.getKey());
				uip.setValue(me.getValue());
				url.addParameter(uip);
			}
		}

		if (getActionExpression() != null) {

			UIAction uiAction = new UIAction();
			uiAction.setAction(getActionExpression().getExpressionString());
			url.addParameter(uiAction);
		}

		PropagationType propagationType = PropagationType.valueOf(getPropagation().toUpperCase());
		if (propagationType == PropagationType.DEFAULT || propagationType == PropagationType.JOIN
				|| propagationType == PropagationType.NESTED || propagationType == PropagationType.END) {
			UIConversationId uiConversationId = UIConversationId.newInstance();
			uiConversationId.setViewId(viewId);
			url.addParameter(uiConversationId);
		}

		if (propagationType == PropagationType.JOIN || propagationType == PropagationType.NESTED || propagationType == PropagationType.BEGIN
				|| propagationType == PropagationType.END) {
			UIConversationPropagation uiPropagation = UIConversationPropagation.newInstance();
			uiPropagation.setType(getPropagation());
			uiPropagation.setPageflow(getPageflow());
			url.addParameter(uiPropagation);
		}

		//      if (propagationType == PropagationType.NONE)
		//      {
		//         UIConversationPropagation uiPropagation = UIConversationPropagation.newInstance();
		//         uiPropagation.setType(getPropagation());         
		//         url.addParameter(uiPropagation);
		//      }

		if (getConversationName() != null) {
			UIConversationName name = UIConversationName.newInstance();
			name.setValue(getConversationName());
			url.addParameter(name);
		}

		ValueExpression taskInstanceValueExpression = getValueExpression("taskInstance");
		if (taskInstanceValueExpression != null) {
			UITaskId uiTaskId = UITaskId.newInstance();
			uiTaskId.setValueExpression("taskInstance", taskInstanceValueExpression);
			url.addParameter(uiTaskId);
		}

		if (!usedParameters.contains("dataModelSelection")) {
			UISelection uiSelection = getSelection();
			if (uiSelection != null) {
				url.addParameter(uiSelection);
			}
		}
		encodedUrl = url.getEncodedUrl();

		return encodedUrl;
	}

	public abstract void setView(String view);

	public abstract String getPropagation();

	public abstract void setPropagation(String propagtion);

	public abstract String getPageflow();

	public abstract void setPageflow(String pageflow);

	public abstract String getFragment();

	public abstract void setFragment(String fragment);

	public abstract void setConversationName(String name);

	public abstract String getConversationName();

	public abstract void setIncludePageParams(boolean value);

	public abstract boolean isIncludePageParams();

	public UISelection getSelection() {
		UIData parentUIData = getParentUIData();
		if (parentUIData != null) {
			if (parentUIData.getValue() instanceof DataModel) {
				String dataModelExpression = parentUIData.getValueExpression("value").getExpressionString();
				String dataModelName = dataModelExpression.substring(2, dataModelExpression.length() - 1).replace('$', '.');
				UISelection uiSelection = UISelection.newInstance();
				uiSelection.setDataModel(dataModelName);
				uiSelection.setVar(parentUIData.getVar());
				return uiSelection;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public UIData getParentUIData() {
		UIComponent parent = this.getParent();
		while (parent != null) {
			if (parent instanceof UIData) {
				return (UIData) parent;
			} else {
				parent = parent.getParent();
			}
		}
		return null;
	}

	public void removeActionListener(ActionListener listener) {
		// Silently fail, RF requires this
		//throw new UnsupportedOperationException("Action listeners not supported by s:link/s:button");
	}

	public ActionListener[] getActionListeners() {
		// Silently fail, RF requires this
		//throw new UnsupportedOperationException("Action listeners not supported by s:link/s:button");
		return null;
	}

	public void addActionListener(ActionListener listener) {
		// Silently fail, RF requires this
		//throw new UnsupportedOperationException("Action listeners not supported by s:link/s:button");
	}

}