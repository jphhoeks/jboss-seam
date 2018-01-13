package org.jboss.seam.security.openid;

import org.jboss.seam.security.SimplePrincipal;

public class OpenIdPrincipal 
    extends SimplePrincipal
{
    private static final long serialVersionUID = 1L;

	public OpenIdPrincipal(String name) {
        super(name);
    }
}
