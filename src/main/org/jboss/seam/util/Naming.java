/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.util;

import java.util.Hashtable;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;

public final class Naming {
   
    private static final LogProvider log = Logging.getLogProvider(Naming.class);
    private static Hashtable initialContextProperties;

    public static InitialContext getInitialContext(Hashtable<String, String> props) throws NamingException {
       
        if (props==null)
        {
            throw new IllegalStateException("JNDI properties not initialized, Seam was not started correctly");
        }

        if (log.isDebugEnabled())
        {
            log.debug("JNDI InitialContext properties:" + props);
        }
        
        try {
            return props.size()==0 ?
                    new InitialContext() :
                    new InitialContext(props);
        }
        catch (NamingException e) {
            log.debug("Could not obtain initial context", e);
            throw e;
        }
        
    }
    
    public static InitialContext getInitialContext() throws NamingException {
       return getInitialContext(initialContextProperties);
    }

    private Naming() {}
    
    public static void setInitialContextProperties(Hashtable initialContextProperties) {
       Naming.initialContextProperties = initialContextProperties;
    }

    public static Hashtable getInitialContextProperties() {
       return initialContextProperties;
    }

}

