package org.jboss.test.readahead.interfaces;

import java.rmi.RemoteException;
import javax.ejb.EJBHome;
import javax.ejb.CreateException;

/**
 * Home interface for finder read-ahead tests
 * 
 * @author <a href="mailto:danch@nvisia.com">danch (Dan Christopherson</a>
 * @version $Id: CMPFindTestSessionHome.java,v 1.1.1.1 2002/11/16 03:16:43 mikechen Exp $
 * 
 * Revision:
 */
public interface CMPFindTestSessionHome extends EJBHome {
   public CMPFindTestSessionRemote create() throws RemoteException, CreateException;
}