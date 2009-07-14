// ========================================================================
// Copyright (c) 2002 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: AbstractReplicatedStore.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
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
  protected Map     _sessions=new HashMap();
  protected Manager _manager;

  //----------------------------------------
  // Store API - Store LifeCycle

  public
    AbstractReplicatedStore(Manager manager)
    {
      _manager=manager;
    }

  public void
    destroy()			// corresponds to ctor
    {
      _sessions.clear();
      _sessions=null;
      _manager=null;
      super.destroy();
    }

  //----------------------------------------
  // Store API - State LifeCycle

  public State
    newState(String id, int maxInactiveInterval)
    throws Exception
    {
      long creationTime=System.currentTimeMillis();
      Class[]  argClasses   = {String.class, String.class, Long.class, Integer.class, Integer.class};
      Object[] argInstances = {getContextPath(), id, new Long(creationTime), new Integer(maxInactiveInterval), new Integer(_actualMaxInactiveInterval)};
      publish("create", argClasses, argInstances);

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
      Class[]  argClasses   = {String.class, String.class};
      Object[] argInstances = {getContextPath(), state.getId()};
      publish("destroy", argClasses, argInstances);

      String id=state.getId();
      synchronized (_sessions){_sessions.remove(id);}
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
 	  if (!((ReplicatedState)((Map.Entry)i.next()).getValue()).isValid(_scavengerExtraTime))
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
      return _manager.getContextPath();
    }

  //----------------------------------------
  // change notification API

  abstract protected void publish(String methodName, Class[] argClasses, Object[] argInstances);

  protected void
    dispatch(String methodName, Class[] argClasses, Object[] argInstances)
    {
      // only stuff meant for our context will be dispatched to us

      String tmp="(";
      for (int i=2; i<argInstances.length; i++)
	tmp=tmp+argInstances[i]+((i<argInstances.length-1)?", ":"");
      tmp=tmp+")";

      //      _log.info("dispatching call: "+argInstances[1]+"."+methodName+tmp);

      String id=(String)argInstances[1];

      // either this is a class method
      if (methodName.equals("create"))
      {
	long creationTime=((Long)argInstances[2]).longValue();
	int maxInactiveInterval=((Integer)argInstances[3]).intValue();
	int actualMaxInactiveInterval=((Integer)argInstances[4]).intValue();
	State state=new ReplicatedState(this, id, creationTime, maxInactiveInterval, actualMaxInactiveInterval);

	synchronized(_sessions) {_sessions.put(id, state);}
      }
      else if (methodName.equals("destroy"))
      {
	synchronized(_sessions) {_sessions.remove(id);}
      }
      else
      {
	// or an instance method..
	((ReplicatedState)_sessions.get(id)).dispatch(methodName, argClasses, argInstances);
      }
    }
}
