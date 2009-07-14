/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.test.testbeancluster.interfaces;

/**
 * Extension of the testbean interface for clustering testing
 *
 * @see org.jboss.test.testbean.interfaces.StatelessSession
 *
 * @author  <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>.
 * @version $Revision: 1.1.1.1 $
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>10. avril 2002 Sacha Labourey:</b>
 * <ul>
 * <li> First implementation </li>
 * </ul>
 */

public interface StatelessSession extends org.jboss.test.testbean.interfaces.StatelessSession
{
   public void resetNumberOfCalls ();
   public void makeCountedCall ();
   public long getCallCount ();
   
}
