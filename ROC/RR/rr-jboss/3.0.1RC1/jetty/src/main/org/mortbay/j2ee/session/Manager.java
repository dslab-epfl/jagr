// ========================================================================
// Copyright (c) 2002 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: Manager.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
// ========================================================================

package org.mortbay.j2ee.session;

//----------------------------------------

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.apache.log4j.Category;
import org.mortbay.j2ee.J2EEWebApplicationContext;

//----------------------------------------

// TODO
//-----

// we need a SnapshotInterceptor
// we need to package this into JBoss and Mort Bay spaces
// Cluster/CMR & JDO Stores should be finished/done
// a DebugInterceptor could be fun....
// Jetty should be user:jetty, role:webcontainer in order to use the session EJBs - wil probably need a SecurityInterceptor
// can I optimise return of objects from set/removeAttribute?
// CMPState should use local not remote interfaces
// FAQ entry should be finished
// Throttle/BufferInterceptor - could be written very like MigrationInterceptor
// StateAdaptor needs a name change
// We need some predefined containers
// tighten up access priviledges
// javadoc
//----------------------------------------

// we need to rethink the strategies for flushing the local cache into
// the distributed cache - specifically how they can be aggregated
// (or-ed as opposed to and-ed).

// the spec does not say (?) whether session attribute events should
// be received in the order that the changes took place - we can
// control this by placing the SynchronizationInterceptor before or
// after the BindingInterceptor

// we could use a TransactionInterceptor to ensure that compound
// operations on e.g. a CMPState are atomic - or we could explicitly
// code the use of transactions where needed (we should be able to
// make all multiple calls collapse into one call to server - look
// into this). Since HttpSessions have no transactional semantics,
// there is no way for the user to inform us of any requirements...

//----------------------------------------

public class Manager
  implements org.mortbay.jetty.servlet.SessionManager
{
  Category _log=Category.getInstance(getClass().getName());


  final Map _sessions = new HashMap();

  Store _store = null;
  public Store getStore() {return _store;}

  protected final String _contextPath;
  public String getContextPath() {return _contextPath;}

  public
    Manager(J2EEWebApplicationContext context)
  {
    _contextPath=context.getContextPath();

    // default interceptor stack
    if (_interceptorClasses==null)
      _interceptorClasses=context.getDistributableHttpSessionInterceptorClasses();

    // default store
    if (_storeClass==null)
      _storeClass=context.getDistributableHttpSessionStoreClass();

    setMaxInactiveInterval(context.getHttpSessionMaxInactiveInterval());
    setActualMaxInactiveInterval(context.getHttpSessionActualMaxInactiveInterval());
    setLocalScavengePeriod(context.getLocalHttpSessionScavengePeriod());
    setDistributableScavengePeriod(context.getDistributableHttpSessionScavengePeriod());
    setDistributableScavengeOffset(context.getDistributableHttpSessionScavengeOffset());

    _log.debug("constructed");
  }

  // this is really only for tests... - lose later..
  public
    Manager()
  {
    _contextPath="/";
    setStoreClass("org.mortbay.j2ee.session.LocalStore");
    List list=new ArrayList();
    list.add("org.mortbay.j2ee.session.TypeCheckingInterceptor");
    list.add("org.mortbay.j2ee.session.BindingInterceptor");
    list.add("org.mortbay.j2ee.session.MarshallingInterceptor");
    setInterceptorClasses(list);

    setMaxInactiveInterval(12*60*60);
    setActualMaxInactiveInterval(60);
    setLocalScavengePeriod(10);
    setDistributableScavengePeriod(20);
    setDistributableScavengeOffset(10);

    //    initialize(null);
  }

  //----------------------------------------
  // LifeCycle API
  //----------------------------------------

  boolean _started=false;
  Object  _startedLock=new Object();
  Timer   _scavenger;

  protected int _localScavengePeriod=60*10; // every 10 mins
  public void setLocalScavengePeriod(int period) {_localScavengePeriod=period;}
  public int getLocalScavengePeriod() {return _localScavengePeriod;}

  protected int _distributableScavengePeriod=60*60; // 1 hour
  public void setDistributableScavengePeriod(int period) {_distributableScavengePeriod=period;}
  public int getDistributableScavengePeriod() {return _distributableScavengePeriod;}

  protected int _distributableScavengeOffset=(int)(_localScavengePeriod*1.5); // 15 mins;
  public void setDistributableScavengeOffset(int offset) {_distributableScavengeOffset=offset;}
  public int getDistributableScavengeOffset() {return _distributableScavengeOffset;}

  protected int _actualMaxInactiveInterval=60*60*24*7;	// a week;
  public void setActualMaxInactiveInterval(int interval) {_actualMaxInactiveInterval=interval;}
  public int getActualMaxInactiveInterval() {return _actualMaxInactiveInterval;}

  class Scavenger extends TimerTask {public void run() {scavenge();}}

  public void
    start()
  {
    synchronized (_startedLock)
    {
      _store.setScavengerPeriod(_distributableScavengePeriod);
      _store.setScavengerExtraTime(_distributableScavengeOffset);
      _store.setActualMaxInactiveInterval(_actualMaxInactiveInterval);

      try
      {
	_store.start();
      }
      catch (Throwable t)
      {
	_log.warn("distributed Store ("+_store.getClass().getName()+") failed to initialise", t);
	_log.warn("falling back to a local session implementation - NO HTTPSESSION DISTRIBUTION");
	_store=new LocalStore(this);
      }
      boolean isDaemon=true;
      _scavenger=new Timer(isDaemon);
      long delay=_localScavengePeriod*1000;
      _scavenger.scheduleAtFixedRate(new Scavenger() ,delay,delay);
      _started=true;
    }

    _log.debug("started");
  }

  public boolean
    isStarted()
  {
    synchronized (_startedLock) {return _started;}
  }

  public void
    stop()
  {
    synchronized (_startedLock)
    {

      // I guess we will have to ask the store for a list of sessions
      // to migrate... - TODO

      synchronized (_sessions)
      {
	List copy=new ArrayList(_sessions.values());
	for (Iterator i=copy.iterator(); i.hasNext();)
	  ((StateAdaptor)i.next()).migrate();

	_sessions.clear();
      }

      _scavenger.cancel();
      _scavenger=null;
      scavenge();
      _store.stop();
      _store.destroy();
      _store=null;
      _started=false;
    }

    _log.debug("stopped");
  }

  //----------------------------------------
  // SessionManager API
  //----------------------------------------

  org.mortbay.jetty.servlet.ServletHandler _handler;
  List _interceptorClasses=null;
  String _storeClass=null;

  public void
    setInterceptorClasses(List interceptorClasses)
  {
    _interceptorClasses=interceptorClasses;
  }

  public void
    setStoreClass(String storeClass)
  {
    _storeClass=storeClass;
  }

  public void
    initialize(org.mortbay.jetty.servlet.ServletHandler handler)
  {
    _handler=handler;
    //    _log = Logger.getLogger(getClass().getName()+"#" + getServletContext().getServletContextName());

    try
    {
      Class[] ctorParams={Manager.class};
      Object[] params={this};
      _store=(Store)Class.forName(_storeClass, true, Thread.currentThread().getContextClassLoader()).getConstructor(ctorParams).newInstance(params);
    }
    catch (Exception e)
    {
      _log.error("could not create Store: "+_storeClass, e);
    }

    // perhaps we should cache the interceptor classes here as well...

    //    _log.info("initialised("+_handler+"): "+Thread.currentThread().getContextClassLoader());
  }

  //----------------------------------------
  // SessionManager API
  //----------------------------------------

  public HttpSession
    getHttpSession(String id)
  {
    return findSession(id);
  }

  public HttpSession
    newHttpSession(HttpServletRequest request) // TODO
  {
    return newSession();
  }

  //----------------------------------------
  // this does not need locking as it is an int and access should be atomic...

  int _maxInactiveInterval;

  public int
    getMaxInactiveInterval()
  {
    return _maxInactiveInterval;
  }

  public void
    setMaxInactiveInterval(int seconds)
  {
    //    _log.info("setMaxInactiveInterval("+seconds+")");
    _maxInactiveInterval=seconds;
  }

  //----------------------------------------
  // Listeners

  // These lists are only modified at webapp [un]deployment time, by a
  // single thread, so although read by multiple threads whilst the
  // Manager is running, need no synchronization...

  final List _sessionListeners          =new ArrayList();
  final List _sessionAttributeListeners =new ArrayList();

  public void
    addEventListener(EventListener listener)
    throws IllegalArgumentException, IllegalStateException
  {
    synchronized (_startedLock)
    {
      if (isStarted())
	throw new IllegalStateException("EventListeners must be added before a Session Manager starts");

      boolean known=false;
      if (listener instanceof HttpSessionAttributeListener)
      {
	//	_log.info("adding HttpSessionAttributeListener: "+listener);
	_sessionAttributeListeners.add(listener);
	known=true;
      }
      if (listener instanceof HttpSessionListener)
      {
	//	_log.info("adding HttpSessionListener: "+listener);
	_sessionListeners.add(listener);
	known=true;
      }

      if (!known)
	throw new IllegalArgumentException("Unknown EventListener type "+listener);
    }
  }

  public void
    removeEventListener(EventListener listener)
    throws IllegalStateException
  {
    synchronized (_startedLock)
    {
      if (isStarted())
	throw new IllegalStateException("EventListeners may not be removed while a Session Manager is running");

      if (listener instanceof HttpSessionAttributeListener)
	_sessionAttributeListeners.remove(listener);
      if (listener instanceof HttpSessionListener)
	_sessionListeners.remove(listener);
    }
  }

  //----------------------------------------
  // Implementation...
  //----------------------------------------

  public ServletContext
    getServletContext()
  {
    return _handler.getServletContext();
  }

  public HttpSessionContext
    getSessionContext()
  {
    return org.mortbay.jetty.servlet.SessionContext.NULL_IMPL;
  }

  //--------------------
  // session lifecycle
  //--------------------


  // I need to think more about where the list of extant sessions is
  // held...

  // is it held by the State Factory/Type (static), which best knows
  // how to find it ? The trouble is we are not asking for just the
  // State but the whole container...

  // if the State was an EJB, the Manager could hold a HashMap of
  // id:State and the State would just be an EJB handle...

  // How does the ThrottleInterceptor fit into this. If it is holding
  // a local cache in front of a DistributedState, do we check them
  // both and compare timestamps, or flush() all ThrottleInterceptors
  // in the WebApp before we do the lookup (could be expensive...)

  // when finding a distributed session assume we are on nodeB
  // receiving a request for a session immediately after it has just
  // been created on NodeA. If we can't find it straight away, we need
  // to take into account how long it's flushing and distribution may
  // take, wait that long and then look again. This may hold up the
  // request, but without the session the request is not much good.

  // overload this to change the construction of the Container....

  protected HttpSession
    newContainer(String id, State state)
  {
    // put together the make-believe container and HttpSession state

    StateAdaptor adp=new StateAdaptor(id, this, getMaxInactiveInterval(), currentSecond());

    State last=state;
    try
    {
      Class[] ctorParams={Manager.class, HttpSession.class, State.class};
      for (ListIterator i=_interceptorClasses.listIterator(_interceptorClasses.size()); i.hasPrevious();)
      {
	String name=(String)i.previous();
	_log.debug("adding interceptor instance: "+name);
	Class clazz=Class.forName(name, true, Thread.currentThread().getContextClassLoader());
	Object[] params={this, adp, last};
	StateInterceptor interceptor=(StateInterceptor)clazz.getConstructor(ctorParams).newInstance(params);
	interceptor.setState(last); // this is also passed into ctor - make up your mind - TODO
	interceptor.start();
	last=interceptor;
      }
    }
    catch (Exception e)
    {
      _log.error("could not build distributed HttpSession container", e);
    }

    adp.setState(last);

    return adp;
  }

  protected HttpSession
    newSession()
  {
    String id=null;
    HttpSession session=null;
    try
    {
      id=_store.allocateId();
      State state=_store.newState(id, getMaxInactiveInterval());
      session=newContainer(id, state);
    }
    catch (Exception e)
    {
      _log.error("could not create HttpSession", e);
      return null;		// BAD - TODO
    }

    _log.debug("remembering session - "+id);

    synchronized (_sessions) {_sessions.put(id, session);}

    notifySessionCreated(session);

    return session;
  }

  protected State
    destroyContainer(HttpSession session)
  {
    // dissasemble the container here to aid GC

    StateAdaptor sa=(StateAdaptor)session;
    State last=sa.getState(); sa.setState(null);

    for (int i=_interceptorClasses.size(); i>0; i--)
    {
      StateInterceptor si=(StateInterceptor)last;
      si.stop();
      State s=si.getState();
      si.setState(null);
      last=s;
    }

    return last;
  }

  protected void
    destroySession(HttpSession container)
  {
    String id=container.getId();
    _log.debug("forgetting session - "+id);
    Object tmp;
    synchronized (_sessions) {tmp=_sessions.remove(id);}
    container=(HttpSession)tmp;
    _log.debug("forgetting session - "+ container);

    if (container==null)
    {
      _log.warn("session - "+ container+" has already been destroyed");
      return;
    }

    // TODO remove all the attributes - generating correct events
    // check ordering on unbind and destroy notifications - The
    // problem is that we want these calls to go all the way through
    // the container - but not to the store - because that would be
    // too expensive and we can predict the final state...

    // we don't need to do this if we know that none of the attributes
    // are BindingListers AND there are no AttributeListeners
    // registered... - TODO

    // This will do for the moment...


    // LATER - TODO

    try
    {
      State state=((StateAdaptor)container).getState();

      // filthy hack...
      // stop InvalidInterceptors - otherwise we can't clean up session... - TODO
      {
	State s=state;
	StateInterceptor si=null;
	while (s instanceof StateInterceptor)
	{
	  si=(StateInterceptor)s;
	  s=si.getState();	// next interceptor
	  if (si instanceof ValidationInterceptor)
	    si.stop();
	}
      }

      String[] names=state.getAttributeNameStringArray();
      for (int i=0; i<names.length; i++)
	state.removeAttribute(names[i], false);

      // should just do this for attributes which are BindingListeners
      // - then just clear() the rest... - TODO
    }
    catch(RemoteException e)
    {
      _log.error("could not raise events on session destruction - problem in distribution layer", e);
    }

    _log.debug("notifying session - "+id);
    notifySessionDestroyed(container);

    _log.debug("destroying container - "+id);
    State state=destroyContainer(container);

    try
    {
      if (state!=null)		// an interceptor may preempt us, if
				// it does not want this state
				// removed...
      {
	_log.debug("removing state - "+id);
	_store.removeState(state);
      }
    }
    catch (Exception e)
    {
      _log.error("could not remove session state", e);
    }
  }


  protected HttpSession
    findSession(String id)
  {
    HttpSession container=null;

    try
    {
      // find the state
      State state=_store.loadState(id);

      // is it valid ?
      state=((state!=null) && state.isValid())?state:null; // expensive ?

      // if so
      if (state!=null)
      {

	// this looks slow - but to be 100% safe we need to make sure
	// that no-one can enter another container for the same id,
	// whilst we are thinking about it...

	// is there a container already available ?
	synchronized (_sessions)
	{
	  // do we have an existing container ?
	  container=(HttpSession)_sessions.get(id);

	  // if not...
	  if (container==null)
	  {
	    // make a new one...
	    container=newContainer(id, state);// we could lower contention by preconstructing containers... - TODO
	    _sessions.put(id, container);
	  }
	}
      }
    }
    catch (Exception ignore)
    {
      _log.debug("did not find distributed session: "+id);
    }

    return container;
  }

  //--------------------
  // session events
  //--------------------

  // should this all be delegated to the event raising interceptor....

  public Object
    notifyAttributeAdded(HttpSession session, String name, Object value)
  {
    int n=_sessionAttributeListeners.size();
    if (n>0)
    {
      HttpSessionBindingEvent event =
	new HttpSessionBindingEvent(session, name, value);

      for(int i=0;i<n;i++)
	((HttpSessionAttributeListener)
	 _sessionAttributeListeners.get(i)).attributeAdded(event);

      event=null;
    }

    return value;
  }

  public Object
    notifyAttributeReplaced(HttpSession session, String name, Object value)
  {
    int n=_sessionAttributeListeners.size();
    if (n>0)
    {
      HttpSessionBindingEvent event =
	new HttpSessionBindingEvent(session, name, value);

      for(int i=0;i<n;i++)
	((HttpSessionAttributeListener)
	 _sessionAttributeListeners.get(i)).attributeReplaced(event);

      event=null;
    }

    return value;
  }

  public Object
    notifyAttributeRemoved(HttpSession session, String name, Object value)
  {
    int n=_sessionAttributeListeners.size();
    if (n>0)
    {
      HttpSessionBindingEvent event =
	new HttpSessionBindingEvent(session, name, value);

      for(int i=0;i<n;i++)
	((HttpSessionAttributeListener)
	 _sessionAttributeListeners.get(i)).attributeRemoved(event);

      event=null;
    }

    return value;
  }

  public void
    notifySessionCreated(HttpSession session)
  {
    int n=_sessionListeners.size();
    if (n>0)
    {
      HttpSessionEvent event = new HttpSessionEvent(session);

      for(int i=0;i<n;i++)
	((HttpSessionListener)_sessionListeners.get(i)) .sessionCreated(event);

      event=null;
    }
  }

  public void
    notifySessionDestroyed(HttpSession session)
  {
    int n=_sessionListeners.size();
    if (n>0)
    {
      HttpSessionEvent event = new HttpSessionEvent(session);

      for(int i=0;i<n;i++)
	((HttpSessionListener)_sessionListeners.get(i)).sessionDestroyed(event);

      event=null;
    }
  }

  // this is to help sessions decide if they have timed out... It is
  // wrapped here so that if I decide that System.currentTimeMillis()
  // is too heavy, I can figure out a lighter way to return a rough
  // time to the sessions...

  public long
    currentSecond()
  {
    return System.currentTimeMillis();
  }

  // ensure that this code is run with the correct ContextClassLoader...
  protected void
    scavenge()
  {
    _log.info("local scavenging...");
    //
    // take a quick copy...
    Collection copy;
    synchronized (_sessions) {copy=new ArrayList(_sessions.values());}
    //
    // iterate over it at our leisure...
    for (Iterator i=copy.iterator(); i.hasNext();)
    {
      // all we have to do is check if a session isValid() to force it
      // to examine itself and invalidate() itself if necessary... -
      // because it has a local cache of the necessary details, it
      // will only go to the Stored State if it really thinks that it
      // is invalid...
      String id=null;;
      try
      {
	StateAdaptor sa=(StateAdaptor)i.next();
	id=sa.getId();
	sa.getLastAccessedTime();
      }
      catch (Exception ignore)
      {
	synchronized (_sessions) {_sessions.remove(id);}
      }
    }
  }

  // tmp hack....
  protected String _workerName;

  public String getWorkerName() { return _workerName; }
  public void setWorkerName(String workerName) { _workerName=workerName; }
}
