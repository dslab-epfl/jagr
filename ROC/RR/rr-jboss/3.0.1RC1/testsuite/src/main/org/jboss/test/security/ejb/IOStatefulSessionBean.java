package org.jboss.test.security.ejb;

import java.io.IOException;
import java.security.Principal;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

/** A simple session bean for testing custom security.

@author Scott.Stark@jboss.org
@version $Revision: 1.1.1.1 $
*/
public class IOStatefulSessionBean implements SessionBean
{
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(IOStatefulSessionBean.class);
   
    private SessionContext sessionContext;

    public void ejbCreate() throws CreateException
    {
        log.debug("IOStatefulSessionBean.ejbCreate() called");
    }

    public void ejbActivate()
    {
        log.debug("IOStatefulSessionBean.ejbActivate() called");
    }

    public void ejbPassivate()
    {
        log.debug("IOStatefulSessionBean.ejbPassivate() called");
    }

    public void ejbRemove()
    {
        log.debug("IOStatefulSessionBean.ejbRemove() called");
    }

    public void setSessionContext(SessionContext context)
    {
        sessionContext = context;
    }

    public String read(String path) throws IOException
    {
        log.debug("IOStatefulSessionBean.read, path="+path);
        Principal p = sessionContext.getCallerPrincipal();
        log.debug("IOStatefulSessionBean.read, callerPrincipal="+p);
        return path;
    }

    public void write(String path) throws IOException
    {
        log.debug("IOStatefulSessionBean.write, path="+path);
        Principal p = sessionContext.getCallerPrincipal();
        log.debug("IOStatefulSessionBean.write, callerPrincipal="+p);
    }
}
