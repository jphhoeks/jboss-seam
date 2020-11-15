package org.jboss.seam.mail.ui;

import javax.mail.Message.RecipientType;

/**
 * JSF component for rendering a Bcc
 */
public class UIBcc extends RecipientAddressComponent {
	
	public UIBcc() {
		super();
	}

	@Override
	protected RecipientType getRecipientType() {
		return RecipientType.BCC;
	}

}
