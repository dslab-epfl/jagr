/**
 *  MicrorebootInProgress indicates the condition that a microreboot of 
 *  an ejb is now in progress. EJB container exchanges the value of the
 *  JNDI name of the ejb from the home interface of the ejb to this object. 
 *  If application gets this object as the result of JNDI lookup, then 
 *  it should either retry the request later or return the microreboot 
 *  in progress message to the client. 
 *
 *  @author skawamo@stanford.edu
 *  $Id: MicrorebootInProgress.java,v 1.1 2004/09/10 19:31:33 skawamo Exp $
 */
package roc.rr;
import java.io.Serializable;

public class MicrorebootInProgress implements Serializable {
    public MicrorebootInProgress(){}
}
