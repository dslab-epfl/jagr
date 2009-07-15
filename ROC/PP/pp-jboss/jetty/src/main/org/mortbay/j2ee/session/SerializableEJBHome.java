// ========================================================================
// Copyright (c) 2002 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: SerializableEJBHome.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $
// ========================================================================

package org.mortbay.j2ee.session;

// utility for unambiguously shipping EJBHomes from node to node...

public class
  SerializableEJBHome
  implements java.io.Serializable
{
  javax.ejb.HomeHandle _handle=null;

  protected
    SerializableEJBHome()
    throws java.rmi.RemoteException
    {
    }

  SerializableEJBHome(javax.ejb.EJBHome home)
    throws java.rmi.RemoteException
    {
      _handle=home.getHomeHandle();
    }

  javax.ejb.EJBHome
    toEJBHome()
    throws java.rmi.RemoteException
    {
      return _handle.getEJBHome();
    }
}
