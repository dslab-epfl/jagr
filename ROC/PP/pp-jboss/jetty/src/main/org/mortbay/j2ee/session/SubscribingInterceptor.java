// ========================================================================
// Copyright (c) 2002 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: SubscribingInterceptor.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $
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


// hook SubscribingInterceptor to AbstractReplicatedStore
// lose ReplicatedState

public class SubscribingInterceptor
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

  // this Interceptor is stateful - it is the dispatch point for
  // change notifications targeted at the session that it wraps.

  public void
    start()
  {
    try
    {
      getStore().subscribe(getId(), this);
    }
    catch (RemoteException e)
    {
      _log.error("could not get my ID", e);
    }
  }

  public void
    stop()
  {
    try
    {
      getStore().unsubscribe(getId());
    }
    catch (RemoteException e)
    {
      _log.error("could not get my ID", e);
    }
  }
}
