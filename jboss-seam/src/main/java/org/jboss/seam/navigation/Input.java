package org.jboss.seam.navigation;

import org.jboss.seam.Component;

public class Input extends Put {
	public Input() {
		super();
	}
	public void in() {
		Object object = getScope() == null ? Component.getInstance(getName()) : getScope().getContext().get(getName());
		getValue().setValue(object);
	}

}
