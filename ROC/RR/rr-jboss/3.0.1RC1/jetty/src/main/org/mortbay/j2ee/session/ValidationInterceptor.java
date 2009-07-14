// ========================================================================
// Copyright (c) 2002 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: ValidationInterceptor.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
// ========================================================================

package org.mortbay.j2ee.session;

//----------------------------------------

import java.rmi.RemoteException;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Category;

//----------------------------------------


public class ValidationInterceptor
  extends AroundInterceptor
{
  protected Category _log=Category.getInstance(getClass().getName());

  public
    ValidationInterceptor(Manager manager, HttpSession session, State state)
  {
    super(session, state);
  }

  protected void before() throws IllegalStateException {if (_running) checkValid();}
  protected void after() {}

  //----------------------------------------

  protected boolean _running=false;

  public void start() {_running=true;}
  public void stop()  {_running=false;}

  protected void
    checkValid()
    throws IllegalStateException
  {
    boolean valid=false;
    try
    {
      valid=_state.isValid();
    }
    catch (java.rmi.NoSuchObjectException ignore)
    {
      //      _log.info("IGNORE ABOVE NoSuchEntityException - harmless");
    }
    catch (javax.ejb.NoSuchEntityException ignore)
    {
      //      _log.info("IGNORE ABOVE NoSuchEntityException - harmless");
    }
   catch (Exception e)
    {
      _log.error("couldn't determine validity of HttpSession: "+_session, e);
    }

    if (!valid)
    {
      stop();			// relax - or we will bounce session tidy-up
      throw new IllegalStateException("invalid HttpSession");
    }
  }
}
