/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.iiop.server;

import org.omg.CORBA.Any;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;
import org.omg.CORBA.PolicyError;
import org.omg.PortableInterceptor.PolicyFactory;

/**
 * Factory of <code>org.omg.CORBA.Policy</code> objects containing codebase 
 * strings.
 *
 * @author  <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @version $Revision: 1.1.1.1 $
 */
class CodebasePolicyFactory
      extends LocalObject
      implements PolicyFactory 
{
   public CodebasePolicyFactory()
   {
   }

   // org.omg.PortableInterceptor.PolicyFactory operations --------------------

   public Policy create_policy(int type, Any value)
      throws PolicyError
   {
      if (type != CodebasePolicy.TYPE) {
         throw new PolicyError();
      }
      String codebase = value.extract_string();
      return new CodebasePolicy(codebase);
   }
}
