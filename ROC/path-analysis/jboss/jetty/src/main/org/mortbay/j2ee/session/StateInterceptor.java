// ========================================================================
// Copyright (c) 2002 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: StateInterceptor.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
// ========================================================================

package org.mortbay.j2ee.session;

//----------------------------------------

import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.Map;
import javax.servlet.http.HttpSession;

//----------------------------------------
/**
 * Superlass for StateInterceptors - objects which
 * wrap-n-delegate/decorate a State instance. A stack of
 * StateInterceptors form a StateContainer.
 *
 * @author <a href="mailto:jules@mortbay.com">Jules Gosnell</a>
 * @version 1.0
 */
public class
  StateInterceptor
  implements State
{
  final HttpSession _session;	// TODO - lose session from state - pass in in context
  State _state;

  //----------------------------------------
  // 'StateInterceptor' API
  //----------------------------------------

  StateInterceptor(HttpSession session, State state)
    {
      _session=session;
      _state=state;
    }

  protected State       getState()            {return _state;}
  protected void        setState(State state) {_state=state;}
  protected HttpSession getSession()          {return _session;}

  // lifecycle
  public    void start() {}
  public    void stop() {}

  // misc
  public    String toString() {return "<"+getClass()+"->"+getState()+">";}

  //----------------------------------------
  // wrapped-n-delegated-to 'State' API
  //----------------------------------------
  // invariant field accessors
  public    String      getId()                                                      throws RemoteException {return getState().getId();}
  public    int         getActualMaxInactiveInterval()                               throws RemoteException {return getState().getActualMaxInactiveInterval();}
  public    long        getCreationTime()                                            throws RemoteException {return getState().getCreationTime();}

  // variant field accessors
  public    Map         getAttributes()                                              throws RemoteException {return getState().getAttributes();}
  public    void        setAttributes(Map attributes)                                throws RemoteException {getState().setAttributes(attributes);}
  public    long        getLastAccessedTime()                                        throws RemoteException {return getState().getLastAccessedTime();}
  public    void        setLastAccessedTime(long time)                               throws RemoteException {getState().setLastAccessedTime(time);}
  public    int         getMaxInactiveInterval()                                     throws RemoteException {return getState().getMaxInactiveInterval();}
  public    void        setMaxInactiveInterval(int interval)                         throws RemoteException {getState().setMaxInactiveInterval(interval);}

  // compound fn-ality
  public    Object      getAttribute(String name)                                    throws RemoteException {return getState().getAttribute(name);}
  public    Object      setAttribute(String name, Object value, boolean returnValue) throws RemoteException {return getState().setAttribute(name, value, returnValue);}
  public    Object      removeAttribute(String name, boolean returnValue)            throws RemoteException {return getState().removeAttribute(name, returnValue);}
  public    Enumeration getAttributeNameEnumeration()                                throws RemoteException {return getState().getAttributeNameEnumeration();}
  public    String[]    getAttributeNameStringArray()                                throws RemoteException {return getState().getAttributeNameStringArray();}
  public    boolean     isValid()                                                    throws RemoteException {return getState().isValid();}
}

