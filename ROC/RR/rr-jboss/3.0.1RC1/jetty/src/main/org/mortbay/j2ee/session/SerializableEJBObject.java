// ========================================================================
// Copyright (c) 2002 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: SerializableEJBObject.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
// ========================================================================

package org.mortbay.j2ee.session;

// utility for unambiguously shipping EJBObjects from node to node...

public class
  SerializableEJBObject
  implements java.io.Serializable
{
  javax.ejb.Handle _handle=null;

  protected
    SerializableEJBObject()
    throws java.rmi.RemoteException
    {
    }

  SerializableEJBObject(javax.ejb.EJBObject ejb)
    throws java.rmi.RemoteException
    {
      _handle=ejb.getHandle();
    }

  javax.ejb.EJBObject
    toEJBObject()
    throws java.rmi.RemoteException
    {
      return _handle.getEJBObject();
    }
}
