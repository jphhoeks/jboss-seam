package org.jboss.seam.security;

import java.util.Objects;

/**
 * Represents a user role.  A conditional role is a special type of role that is assigned to a user
 * based on the contextual state of a permission check.
 *  
 * @author Shane Bryzak
 */
public class Role extends SimplePrincipal {
	private static final long serialVersionUID = 1L;
	private boolean conditional;

	public Role(String name) {
		super(name);
	}

	public Role(String name, boolean conditional) {
		this(name);
		this.conditional = conditional;
	}

	public boolean isConditional() {
		return conditional;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(conditional);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Role other = (Role) obj;
		return conditional == other.conditional;
	}
	
	
}
