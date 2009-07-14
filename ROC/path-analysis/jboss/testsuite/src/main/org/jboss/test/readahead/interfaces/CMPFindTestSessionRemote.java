package org.jboss.test.readahead.interfaces;

import java.rmi.RemoteException;
import javax.ejb.EJBObject;

/**
 * Remote interface for session to test read-ahead finders.
 * 
 * @version $Id: CMPFindTestSessionRemote.java,v 1.1.1.1 2002/11/16 03:16:43 mikechen Exp $
 * 
 * Revision:
 */
public interface CMPFindTestSessionRemote extends EJBObject {
   public void testFinder() throws RemoteException;
   public void testUpdates() throws RemoteException;
   public void testByCity() throws RemoteException;
   public void addressByCity() throws RemoteException;
   public void createTestData() throws RemoteException;
   public void removeTestData() throws RemoteException;
}