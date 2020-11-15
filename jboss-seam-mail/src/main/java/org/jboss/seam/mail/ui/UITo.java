package org.jboss.seam.mail.ui;

import javax.mail.Message.RecipientType;

/**
 * JSF Component for rendering To
 */
public class UITo extends RecipientAddressComponent {
	
	public UITo() {
		super();
	}

	@Override
	protected RecipientType getRecipientType() {
		return RecipientType.TO;
	}

}
