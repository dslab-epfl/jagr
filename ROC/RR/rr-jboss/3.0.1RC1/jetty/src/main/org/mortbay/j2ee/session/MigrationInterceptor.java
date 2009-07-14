// ========================================================================
// Copyright (c) 2002 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: MigrationInterceptor.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
// ========================================================================

package org.mortbay.j2ee.session;

//----------------------------------------

import java.rmi.RemoteException;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Category;

//----------------------------------------

// This interceptor does the following :

// maintains two copies of State - local and distributable.

// when setState() is called it assumes it is being initialised with a
// new distributable State object.

// It copies it's local state into it's current distributable state,
// raising passivation notifications where necessary, empties it's
// local state, then populates it from the new distributable state
// raising activation notifications where necessary.

// all other methods are delegated to the local state, keeping that up
// to date.

// N.B. we don't really want the distributable state object hanging
// around for the lifetime of the webapp. It should be removed after
// being used to initialise this interceptors local state, then
// recreated iff and when passivation occurs... - how can we do
// this... - this is actually important - as it may be gc-ed through
// inactivity while we are still hanging on to it.

// we will probably have to dynamically walk the interceptors below
// us, until we find the state and remove it. Then we hold a reference
// to the final interceptor and before we passivate, reallocate a
// State of the correct type and add it to the end of the interceptor
// stack - phew !!

// we might put a SynchronizationInterceptor between us and our
// LocalState, but it is easier, and gives the user more control if we
// put it in front (i.e. leave it to them) of the
// MigrationInterceptor. It has exactly the same effect (I don't see
// why it should be any slower), but the user gets to decide whether
// they need it ot not.

public class
  MigrationInterceptor
  extends StateInterceptor
{
  Category _log=Category.getInstance(getClass().getName());

  final Manager _manager;
  StateInterceptor _last;

  State _buffer;

  State getDistributableState() {return _state;}
  void setDistributableState(State state) { _state=state;}

  State getLocalState() {return _buffer;}
  void setLocalState(State state) {_buffer=state;}

  public
    MigrationInterceptor(Manager manager, HttpSession session, State state)
    {
      super(session, null);
      ((StateAdaptor)session).registerMigrationListener(this);
      _manager=manager;

      try
      {
	setLocalState(new LocalState(state.getId(), state.getMaxInactiveInterval(), state.getActualMaxInactiveInterval()));
      }
      catch(Exception e)
      {
	_log.error("could not initialise", e);
      }
    }

  public void
    start()
    {
    }

  public void
    stop()
    {
      // chucking our local state, indicates that we should revert to
      // behaving like a normal interceptor...
      setLocalState(null);
    }

  public void
    migrate()
    {
      // passivate our localState by copying it into our
      // distributableState...

      State distributableState=getDistributableState();
      State localState=getLocalState();

      // find last interceptor and add State object to it..
      StateInterceptor last=null;
      for (State i=distributableState; i instanceof StateInterceptor; i=((StateInterceptor)i).getState())
	last=(StateInterceptor)i;
      try
      {
	State state=_manager.getStore().newState(getLocalState().getId(), getLocalState().getMaxInactiveInterval());
	last.setState(state); //  somewhere to passivate to
	// now the distributableState is ready for use...
	// distributableState.setMaxInactiveInterval(localState.getMaxInactiveInterval()); // done already for ctor
	distributableState.setLastAccessedTime(localState.getLastAccessedTime());
	// each attribute that is a Activation Listener needs to be
	// notified...
	distributableState.setAttributes(localState.getAttributes());
	_log.info("migrated session: "+_manager.getContextPath()+":"+localState.getId());
      }
      catch (Exception e)
      {
	_log.error("could not passivate state", e);
      }

      // clear up localState - how - TODO

      // OK all done - stop redirecting everything so container can
      // clean us up....
      //      _log.info("MIGRATION DONE");
    }

  public void
    setState(State distributableState)
    {
      State localState=getLocalState();

      if (distributableState!=null)
      {
	// activate the distributableState by copying it into our
	// localState.
	try
	{
	  //	  localState.setMaxInactiveInterval(distributableState.getMaxInactiveInterval()); // no need we did this for LocalState ctor
	  localState.setLastAccessedTime(distributableState.getLastAccessedTime());
	  // each attribute that is a Activation Listener needs to be
	  // notified...
	  localState.setAttributes(distributableState.getAttributes());
	}
	catch (Exception e)
	{
	  _log.error("could not activate state: "+distributableState, e);
	}

	// find last interceptor and remove State object from it - it
	// might get gc-ed or loaded on another node whilst we are
	// proxying for it - we will recreate it when we need it...
	StateInterceptor last=null;
	for (State i=distributableState; i instanceof StateInterceptor; i=((StateInterceptor)i).getState())
	{
	  last=(StateInterceptor)i;
	}
	try
	{
	  _manager.getStore().removeState(last.getState());
	}
	catch (Exception e)
	{
	  _log.error("could not tidy up migration object", e);
	}
	last.setState(null);
      }

      setDistributableState(distributableState);
    }

  public State
    getState()
    {
      State localState=getLocalState();
      return localState==null?getDistributableState():localState;	// redirect
    }
}
