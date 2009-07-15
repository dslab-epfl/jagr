// ========================================================================
// Copyright (c) 2002 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: AbstractReplicatedStore.java,v 1.1.1.1 2003/03/07 08:26:05 emrek Exp $
// ========================================================================

package org.mortbay.j2ee.session;

//----------------------------------------

import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Map;
import org.apache.log4j.Category;
import org.javagroups.Message;
import org.javagroups.blocks.MessageDispatcher;
import org.javagroups.util.Util;

//----------------------------------------

// implement scavenging
// implement setMaxInactiveInterval
// look for NYI/TODO

// this infrastructure could probably be used across JMS aswell -
// think about it...

/**
 * Maintain synchronisation with other States representing the same
 * session by publishing changes made to ourself and updating ourself
 * according to notifications published by the other State objects.
 *
 * @author <a href="mailto:jules@mortbay.com">Jules Gosnell</a>
 * @version 1.0
 */

abstract public class
  AbstractReplicatedStore
  extends AbstractStore
{
  //----------------------------------------
  // tmp hack to prevent infinite loop
  private final static ThreadLocal _replicating=new ThreadLocal();
  public static boolean     getReplicating()                    {return _replicating.get()==Boolean.TRUE;}
  public static void        setReplicating(boolean replicating) {_replicating.set(replicating?Boolean.TRUE:Boolean.FALSE);}
  //----------------------------------------

  public Object
    clone()
    {
      return super.clone();
    }

  protected Map     _sessions=new HashMap();

  //----------------------------------------
  // Store API - Store LifeCycle

  public void
    destroy()			// corresponds to ctor
    {
      _sessions.clear();
      _sessions=null;
      setManager(null);
      super.destroy();
    }

  //----------------------------------------
  // Store API - State LifeCycle

  public State
    newState(String id, int maxInactiveInterval)
    throws Exception
    {
      long creationTime=System.currentTimeMillis();

      if (!AbstractReplicatedStore.getReplicating())
      {
	Class[]  argClasses   = {String.class, Long.TYPE, Integer.TYPE, Integer.TYPE};
	Object[] argInstances = {id, new Long(creationTime), new Integer(maxInactiveInterval), new Integer(_actualMaxInactiveInterval)};
	publish(null, "createSession", argClasses, argInstances);
      }

      createSession(id, creationTime, maxInactiveInterval, _actualMaxInactiveInterval);

      // if we get one - all we have to do is loadState - because we
      // will have just created it...
      return loadState(id);
    }

  public State
    loadState(String id)
    {
      // pull it out of our cache - if it is not there, it doesn't
      // exist/hasn't been distributed...

      Object tmp;
      synchronized (_sessions) {tmp=_sessions.get(id);}
      return (State)tmp;
    }

  public void
    storeState(State state)
    {
      try
      {
	String id=state.getId();
	synchronized (_sessions){_sessions.put(id, state);}
      }
      catch (Exception e)
      {
	_log.error("error storing session", e);
      }
    }

  public void
    removeState(State state)
    throws Exception
    {
      String id=state.getId();

      if (!AbstractReplicatedStore.getReplicating())
      {
	Class[]  argClasses   = {String.class};
	Object[] argInstances = {id};
	publish(null, "destroySession", argClasses, argInstances);
      }

      destroySession(id);
    }

  //----------------------------------------
  // Store API - garbage collection

  public void
    scavenge()
    throws Exception
    {
      _log.info("distributed scavenging...");
      synchronized (_sessions)
      {
	for (Iterator i=_sessions.entrySet().iterator(); i.hasNext();)
 	  if (!((LocalState)((Map.Entry)i.next()).getValue()).isValid(_scavengerExtraTime))
	  {
	    _log.info("scavenging state");
	    i.remove();
	  }
      }
    }

  //----------------------------------------
  // Store API - hacks... - NYI/TODO

  public void passivateSession(StateAdaptor sa) {}
  public boolean isDistributed() {return true;}

  //----------------------------------------
  // utils

  public String
    getContextPath()
    {
      return getManager().getContextPath();
    }

  //----------------------------------------
  // change notification API

  abstract protected void publish(String id, String methodName, Class[] argClasses, Object[] argInstances);

  protected void
    dispatch(String id, String methodName, Class[] argClasses, Object[] argInstances)
    {
      try
      {
	AbstractReplicatedStore.setReplicating(true);

	Object target=null;
	if (id==null)
	{
	  // either this is a class method
	  target=this;
	}
	else
	{
	  // or an instance method..
	  synchronized (_subscribers){target=_subscribers.get(id);}
	}

	try
	{
	  target.getClass().getMethod(methodName, argClasses).invoke(target, argInstances);
	}
	catch (Exception e)
	{
	  _log.error("this should never happen - code version mismatch ?", e);
	}
      }
      finally
      {
	AbstractReplicatedStore.setReplicating(false);
      }
    }

  public void
    createSession(String id, long creationTime, int maxInactiveInterval, int actualMaxInactiveInterval)
    {
      _log.debug("creating replicated session: "+id);
      State state=new LocalState(id, creationTime, maxInactiveInterval, actualMaxInactiveInterval);
      synchronized(_sessions) {_sessions.put(id, state);}

      if (AbstractReplicatedStore.getReplicating())
      {
	//	_log.info("trying to promote replicated session");
	getManager().getHttpSession(id); // should cause creation of corresponding InterceptorStack
      }
    }

  public void
    destroySession(String id)
    {
      _log.debug("destroying replicated session: "+id);
      synchronized(_sessions) {_sessions.remove(id);}
    }

  //----------------------------------------
  // subscription - Listener management...

  protected Map _subscribers=new HashMap();

  public void
    subscribe(String id, Object o)
    {
      //      _log.info("subscribing: "+id);
      synchronized (_subscribers) {_subscribers.put(id, o);}
    }

  public void
    unsubscribe(String id)
    {
      //      _log.info("unsubscribing: "+id);
      synchronized (_subscribers) {_subscribers.remove(id);}
    }
}
