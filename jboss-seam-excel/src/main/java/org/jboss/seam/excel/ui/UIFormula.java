package org.jboss.seam.excel.ui;

public class UIFormula extends UICell {
	public static final String COMPONENT_TYPE = "org.jboss.seam.excel.ui.UIFormula";

	public UIFormula() {
		super();
	}
	
	@Override
	public String getFamily() {
		return COMPONENT_TYPE;
	}

}
