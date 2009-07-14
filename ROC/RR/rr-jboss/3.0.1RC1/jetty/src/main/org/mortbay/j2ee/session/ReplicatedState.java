// ========================================================================
// Copyright (c) 2002 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: ReplicatedState.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
// ========================================================================

package org.mortbay.j2ee.session;

//----------------------------------------

import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Map;
import javax.servlet.http.HttpSessionBindingListener;
import org.apache.log4j.Category;

//----------------------------------------

// Should this be some sort of interceptor ?

// we could optimise this by defining methods a,b,c,... and publishing
// using those method names...

/**
 * Maintain synchronisation with other States representing the same
 * session by publishing changes made to ourself and updating ourself
 * according to notifications published by the other State objects.
 *
 * @author <a href="mailto:jules@mortbay.com">Jules Gosnell</a>
 * @version 1.0
 */
public class
  ReplicatedState
  implements State
{
  protected final Category _log=Category.getInstance(getClass().getName());
  protected final AbstractReplicatedStore _store;
  protected final String _context;
  protected final String _id;

  protected LocalState _state;

  public
    ReplicatedState(AbstractReplicatedStore store, String id, long creationTime, int maxInactiveInterval, int actualMaxInactiveInterval)
    {
      _store=store;
      _context=_store.getContextPath();
      _id=id;

      // need to pass through creation time...
      _state=new LocalState(id, maxInactiveInterval, actualMaxInactiveInterval);
    }

  ReplicatedState(AbstractReplicatedStore store, LocalState state)
    {
      _store=store;
      _context=_store.getContextPath();
      _state=state;		// we are taking ownership...
      _id=_state.getId();
    }

  //----------------------------------------

  LocalState
    getLocalState()
    {
      return _state;
    }

  //----------------------------------------
  // readers - simply wrap-n-delegate

  public int
    getActualMaxInactiveInterval()
    {
      return _state.getActualMaxInactiveInterval();
    }

  public long
    getCreationTime()
    {
      return _state.getCreationTime();
    }

  public String
    getId()
    {
      return _state.getId();
    }

  public long
    getLastAccessedTime()
    {
      return _state.getLastAccessedTime();
    }

  public int
    getMaxInactiveInterval()
    {
      return _state.getMaxInactiveInterval();
    }

  public Object
    getAttribute(String name)
    {
      return _state.getAttribute(name);
    }

  public Enumeration
    getAttributeNameEnumeration()
    {
      return _state.getAttributeNameEnumeration();
    }

  public String[]
    getAttributeNameStringArray()
    {
      return _state.getAttributeNameStringArray();
    }

  public Map
    getAttributes()
    {
      return _state.getAttributes();
    }

  public boolean
    isValid()
    {
      return _state.isValid();
    }

  //----------------------------------------
  // writers - wrap-n-publish

  public void
    setLastAccessedTime(long time)
    {
      Class[] argClasses={String.class, String.class, Long.class};
      Object[] argInstances={_context, _id, new Long(time)};
      _store.publish("setLastAccessedTime", argClasses, argInstances);
    }

  public void
    setMaxInactiveInterval(int interval)
    {
      Class[] argClasses={String.class, String.class, Integer.class};
      Object[] argInstances={_context, _id, new Integer(interval)};
      _store.publish("setMaxInactiveInterval", argClasses, argInstances);
    }

  public Object
    setAttribute(String name, Object value, boolean returnValue)
    {
      Class[] argClasses={String.class, String.class, String.class, Object.class, Boolean.class};
      Object[] argInstances={_context, _id, name, value, new Boolean(returnValue)}; // optimise - TODO

      Object oldValue=_state.getAttribute(name);
      _store.publish("setAttribute", argClasses, argInstances);

      return returnValue?oldValue:null;
    }

  public void
    setAttributes(Map attributes)
    {
      Class[] argClasses={String.class, String.class, Map.class};
      Object[] argInstances={_context, _id, attributes};
      _store.publish("setAttributes", argClasses, argInstances);
    }

  public Object
    removeAttribute(String name, boolean returnValue)
    {
      Class[] argClasses={String.class, String.class, String.class, Boolean.class};
      Object[] argInstances={_context, _id, name, new Boolean(returnValue)};	// optimise - TODO

      Object oldValue=_state.getAttribute(name);
      _store.publish("removeAttribute", argClasses, argInstances);

      return returnValue?oldValue:null;
    }

  //----------------------------------------

  void
    dispatch(String methodName, Class[] argClasses, Object[] argInstances)
    {
      // only stuff meant for our session will be dispatched to us..
      try
      {
	getClass().getMethod(methodName, argClasses).invoke(this, argInstances);
      }
      catch (Exception e)
      {
	_log.error("this should never happen - code version mismatch ?", e);
      }
    }

  // yeughhhhh! - but cheaper than reformatting args

  // writers - receive-n-delegate

  public void
    setLastAccessedTime(String context, String id, Long time)
    {
      _state.setLastAccessedTime(time.longValue());
    }

  public void
    setMaxInactiveInterval(String context, String id, Integer interval)
    {
      _state.setMaxInactiveInterval(interval.intValue());
    }

  public Object
    setAttribute(String context, String id, String name, Object value, Boolean returnValue)
    {
      return _state.setAttribute(name, value, returnValue.booleanValue());
    }

  public void
    setAttributes(String context, String id, Map attributes)
    {
      _state.setAttributes(attributes);
    }

  public Object
    removeAttribute(String context, String id, String name, Boolean returnValue)
    {
      return _state.removeAttribute(name, returnValue.booleanValue());
    }

  // hacky...
  public boolean
    isValid(int extraTime)
    {
      return _state.isValid(extraTime);
    }
}
