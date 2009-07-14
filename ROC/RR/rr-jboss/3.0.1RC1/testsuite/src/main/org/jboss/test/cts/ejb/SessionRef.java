package org.jboss.test.cts.ejb;

import java.io.Serializable;
import javax.ejb.Handle;

/**
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1.1.1 $
 */
public class SessionRef implements Serializable
{
   Handle sessionHandle;

   /** Creates a new instance of SessionRef */
   public SessionRef(Handle sessionHandle)
   {
      this.sessionHandle = sessionHandle;
   }
   public Handle getHandle()
   {
      return sessionHandle;
   }
}
