/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.iiop;

import java.security.Principal;
import javax.transaction.Transaction;

/**
 * Interface used by local IIOP invocations.
 *      
 * @author  <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @version $Revision: 1.1.1.1 $
 */
public interface LocalInvoker
{
   Object invoke(String opName,
                 Object[] arguments,
                 Transaction tx,
                 Principal identity,
                 Object credential)
      throws Exception;
}
