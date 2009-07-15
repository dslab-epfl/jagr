/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mx.persistence;

import javax.management.MBeanInfo;

/**
 * Persistence manager interface adds <tt>MBeanInfo</tt> to <tt>PersistenMBean</tt>
 * operations. This allows generic persistence manager implementations to store
 * and load the metadata of an MBean.
 *
 * @see javax.management.PersistentMBean
 *
 * @author  <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @version $Revision: 1.1.1.1 $
 *   
 */
public interface PersistenceManager
{

   void load(MBeanInfo metadata);
   void store(MBeanInfo metadata);

}
