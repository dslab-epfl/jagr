/*
 * JBoss, the OpenSource J2EE WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ha.framework.interfaces;

/** 
 *   When using HA-RMI, the result of an invocation is embedded in an instance of this class.
 *   It contains the response of the invocation and, if the list of targets has changed,
 *   a new view of the cluster.
 *
 *   @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 *   @author <a href="mailto:sacha.labourey@cogito-info.ch">Sacha Labourey</a>
 *   @version $Revision: 1.2 $
 *
 * <p><b>Revisions:</b><br>
 */

public class HARMIResponse 
   implements java.io.Serializable
{
   public java.util.ArrayList newReplicants = null;
   public long currentViewId = 0;   
   public Object response = null;

    // ROC PINPOINT EMK BEGIN
    public int request_seqnum = -1;
    // ROC PINPOINT EMK END
}

