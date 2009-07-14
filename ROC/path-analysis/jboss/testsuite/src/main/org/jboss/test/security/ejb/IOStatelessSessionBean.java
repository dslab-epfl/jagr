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
public class IOStatelessSessionBean implements SessionBean
{
   org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(getClass());
   
    private SessionContext sessionContext;

    public void ejbCreate() throws CreateException
    {
        log.debug("IOStatelessSessionBean.ejbCreate() called");
    }

    public void ejbActivate()
    {
        log.debug("IOStatelessSessionBean.ejbActivate() called");
    }

    public void ejbPassivate()
    {
        log.debug("IOStatelessSessionBean.ejbPassivate() called");
    }

    public void ejbRemove()
    {
        log.debug("IOStatelessSessionBean.ejbRemove() called");
    }

    public void setSessionContext(SessionContext context)
    {
        sessionContext = context;
    }

    public String read(String path) throws IOException
    {
        log.debug("IOStatelessSessionBean.read, path="+path);
        Principal p = sessionContext.getCallerPrincipal();
        log.debug("IOStatelessSessionBean.read, callerPrincipal="+p);
        return path;
    }

    public void write(String path) throws IOException
    {
        log.debug("IOStatelessSessionBean.write, path="+path);
        Principal p = sessionContext.getCallerPrincipal();
        log.debug("IOStatelessSessionBean.write, callerPrincipal="+p);
    }
}
