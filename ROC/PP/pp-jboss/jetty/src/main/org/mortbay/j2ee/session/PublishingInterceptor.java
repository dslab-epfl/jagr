// ========================================================================
// Copyright (c) 2002 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: PublishingInterceptor.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $
// ========================================================================

package org.mortbay.j2ee.session;

//----------------------------------------

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Category;

//----------------------------------------

// at a later date, this needs to be able to batch up changes and
// flush to JG on various events e.g. immediately (no batching),
// end-of-request, idle, stop (migration)...

public class PublishingInterceptor
  extends StateInterceptor
{
  protected final Category _log=Category.getInstance(getClass().getName());

  protected AbstractReplicatedStore
    getStore()
  {
    AbstractReplicatedStore store=null;
    try
    {
      store=(AbstractReplicatedStore)getManager().getStore();
    }
    catch (Exception e)
    {
      _log.error("could not get AbstractReplicatedStore");
    }

    return store;
  }

  //----------------------------------------
  // writers - wrap-publish-n-delegate - these should be moved into a
  // ReplicatingInterceptor...

  public void
    setLastAccessedTime(long time)
    throws RemoteException
  {
    if (!AbstractReplicatedStore.getReplicating())
    {
      Class[] argClasses={Long.TYPE};
      Object[] argInstances={new Long(time)};
      getStore().publish(getId(), "setLastAccessedTime", argClasses, argInstances);
    }

    super.setLastAccessedTime(time);
  }

  public void
    setMaxInactiveInterval(int interval)
    throws RemoteException
  {
    if (!AbstractReplicatedStore.getReplicating())
    {
      Class[] argClasses={Integer.TYPE};
      Object[] argInstances={new Integer(interval)};
      getStore().publish(getId(), "setMaxInactiveInterval", argClasses, argInstances);
    }

    super.setMaxInactiveInterval(interval);
  }

  public Object
    setAttribute(String name, Object value, boolean returnValue)
    throws RemoteException
  {
    if (!AbstractReplicatedStore.getReplicating())
    {
      Class[] argClasses={String.class, Object.class, Boolean.TYPE};
      Object[] argInstances={name, value, returnValue?Boolean.TRUE:Boolean.FALSE};
      getStore().publish(getId(), "setAttribute", argClasses, argInstances);
    }

    return super.setAttribute(name, value, returnValue);
  }

  public void
    setAttributes(Map attributes)
    throws RemoteException
  {
    if (!AbstractReplicatedStore.getReplicating())
    {
      Class[] argClasses={Map.class};
      Object[] argInstances={attributes};
      getStore().publish(getId(), "setAttributes", argClasses, argInstances);
    }

    super.setAttributes(attributes);
  }

  public Object
    removeAttribute(String name, boolean returnValue)
    throws RemoteException
  {
    if (!AbstractReplicatedStore.getReplicating())
    {
      Class[] argClasses={String.class, Boolean.TYPE};
      Object[] argInstances={name, returnValue?Boolean.TRUE:Boolean.FALSE};
      getStore().publish(getId(), "removeAttribute", argClasses, argInstances);
    }

    return super.removeAttribute(name, returnValue);
  }
}
