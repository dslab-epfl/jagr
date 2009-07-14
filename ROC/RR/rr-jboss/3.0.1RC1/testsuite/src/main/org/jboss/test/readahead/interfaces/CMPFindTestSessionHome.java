package org.jboss.test.readahead.interfaces;

import java.rmi.RemoteException;
import javax.ejb.EJBHome;
import javax.ejb.CreateException;

/**
 * Home interface for finder read-ahead tests
 * 
 * @author <a href="mailto:danch@nvisia.com">danch (Dan Christopherson</a>
 * @version $Id: CMPFindTestSessionHome.java,v 1.1.1.1 2002/10/03 21:06:56 candea Exp $
 * 
 * Revision:
 */
public interface CMPFindTestSessionHome extends EJBHome {
   public CMPFindTestSessionRemote create() throws RemoteException, CreateException;
}