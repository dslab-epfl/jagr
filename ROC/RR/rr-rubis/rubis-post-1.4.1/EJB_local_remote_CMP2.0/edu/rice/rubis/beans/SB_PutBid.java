package edu.rice.rubis.beans;

import javax.ejb.*;
import java.rmi.*;
import java.util.Vector;

/**
 * This is the Remote Interface of the SB_PutBid Bean
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.1
 */
public interface SB_PutBid extends EJBObject, Remote {

  /**
   * Authenticate the user and get the information to build the html form.
   *
   * @return a string in html format
   * @since 1.1
   */
  public Object[] getBiddingForm(Integer itemId, String username, String password) throws RemoteException;
  public String getBiddingForm(Integer itemId, int userId) throws RemoteException;
 
}
