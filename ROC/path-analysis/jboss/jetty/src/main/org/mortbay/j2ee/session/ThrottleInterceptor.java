// ========================================================================
// Copyright (c) 2002 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: ThrottleInterceptor.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
// ========================================================================

package org.mortbay.j2ee.session;

//----------------------------------------

import java.rmi.RemoteException;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Category;

//----------------------------------------

// this interceptor buffers up changes (by redirecting all calls to a
// private, LocalState object) and allows them to be explicitly
// flushed down the interceptor stack.

// I should step up the granularity on this...

// getting a setState() means doing a flush of your current buffer,
// which belongs to your old state...

public class
  ThrottleInterceptor
  extends StateInterceptor
{
  Category _log=Category.getInstance(getClass().getName());

  final Manager _manager;

  State _buffer;
  boolean _dirty=false;

  State getRealState() { return getState();}
  //  void setRealState(State state) { _state=state;} - TODO

  public
    ThrottleInterceptor(Manager manager, HttpSession session, State state)
  {
    super(session, state);
    _manager=manager;

    int maxInactiveInterval;
//     try
//     {
//       maxInactiveInterval=_state.getMaxInactiveInterval();
//     }
//     catch (RemoteException e)
//     {
//       _log.error("could not retrieve MaxInactiveInterval from State - defaulting...",e );
//       //      maxInactiveInterval=_manager.getMaxInactiveInterval();
      maxInactiveInterval=10;	// TODO
      //    }

    try
    {
      int actualMaxInactiveInterval=60*60*24; // TODO
      _buffer=new LocalState(getState().getId(), maxInactiveInterval, actualMaxInactiveInterval);
    }
    catch (RemoteException e)
    {
      // this will NEVER happen
      _buffer=null;
    }
  }

  // flush the changes that we have buffered up....

  // this does a pretty simple complete flush, we should only flush
  // bits that have changed...
  public void
    flush()
  {
    try
    {
      // copy contents of _buffer into _state...
      getState().setMaxInactiveInterval(_buffer.getMaxInactiveInterval());
      getState().setLastAccessedTime(_buffer.getLastAccessedTime());
      getState().setAttributes(_buffer.getAttributes());
    }
    catch (RemoteException e)
    {
      _log.error("could not flush local cache...", e);
    }

    _dirty=false;
  }

  // redirect changes to a local copy...
  protected State getState() {return _buffer;}

  public void
    setLastAccessedTime(long time)
  {
    try
    {
      if (_buffer.getLastAccessedTime()!=time)
      {
	_buffer.setLastAccessedTime(time);
	_dirty=true;
      }
    }
    catch (RemoteException ignore)
    {
      // this will NEVER happen
    }
  }

  public void
    setMaxInactiveInterval(int interval)
  {
    try
    {
      if (_buffer.getMaxInactiveInterval()!=interval)
      {
	_buffer.setMaxInactiveInterval(interval);
	_dirty=true;
      }
    }
    catch (RemoteException ignore)
    {
      // this will NEVER happen
    }
  }

  public Object
    setAttribute(String name, Object value, boolean returnValue)
  {
    Object tmp=null;
    try
    {
      // can we do a deep comparison on the attributes ? - could be
      // expensive - room for extension here...
      tmp=super.setAttribute(name, value, returnValue);
      _dirty=true;
    }
    catch (RemoteException ignore)
    {
      // this will NEVER happen
    }
    return tmp;
  }

  public Object
    removeAttribute(String name, boolean returnValue)
  {
    Object tmp=null;
    try
    {
      tmp=super.removeAttribute(name, returnValue);
    }
    catch (RemoteException ignore)
    {
      // this will NEVER happen
    }

    if (tmp!=null)		// the attribute was there
      _dirty=true;

    return tmp;
  }

  public void
    setAttributes(Map attributes)
  {
    // we could try to merge new/changed attributes but...
    try
    {
      super.setAttributes(attributes);
    }
    catch (RemoteException ignore)
    {
      // this will NEVER happen
    }
    _dirty=true;
  }
}
