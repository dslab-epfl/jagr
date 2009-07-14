/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ha.httpsession.interfaces;

/**
 * Tag interface to make HttpSession serializable. Used to store the clustered HttpSession
 * in an entity bean.
 * Furthermore, it adds an areAttributesModified method used by the entity bean
 * to determine if the content attributes have been modified.
 * WARNING: the areAttributesModified method should not compare the creation and last
 *          access time but only the attributes and other specific values. Otherwise
 *          the state will be considered as changed for every request (which will cause
 *          to much cluster traffic.
 *
 * @see org.jboss.ha.httpsession.interfaces.ClusteredHttpSessionBusiness
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 1.1.1.1 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>31. décembre 2001 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */

public interface SerializableHttpSession extends java.io.Serializable
{
   /**
    * Method used by the entity bean
    * to determine if the content attributes have been modified.
    * WARNING: the areAttributesModified method should not compare the creation and last
    *          access time but only the attributes and other specific values. Otherwise
    *          the state will be considered as changed for every request (which will cause
    *          to much cluster traffic.
    *
    * @param previousVersion    A previous version of the HttpSession to be compared against.
    */
   public boolean areAttributesModified (SerializableHttpSession previousVersion);
   
   public long getContentCreationTime ();
   public long getContentLastAccessTime ();
}
