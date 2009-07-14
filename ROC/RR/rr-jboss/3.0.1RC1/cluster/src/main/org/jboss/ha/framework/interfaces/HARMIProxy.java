/*
 * JBoss, the OpenSource J2EE WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ha.framework.interfaces;

/** 
 *
 *   @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>
 *   @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 *   @version $Revision: 1.1.1.1 $
 *
 * <p><b>Revisions:</b><br>
 */

public interface HARMIProxy 
   extends java.io.Serializable
{
   public boolean isLocal();
}