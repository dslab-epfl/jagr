package org.jboss.test.readahead.interfaces;

import java.rmi.RemoteException;
import javax.ejb.EJBHome;
import javax.ejb.CreateException;

/**
 * Home interface for finder read-ahead tests
 * 
 * @author <a href="mailto:danch@nvisia.com">danch (Dan Christopherson</a>
 * @version $Id: CMPFindTestSessionHome.java,v 1.1.1.1 2003/03/07 08:26:09 emrek Exp $
 * 
 * Revision:
 */
public interface CMPFindTestSessionHome extends EJBHome {
   public CMPFindTestSessionRemote create() throws RemoteException, CreateException;
}