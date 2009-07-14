// ========================================================================
// Copyright (c) 2002 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: J2EEWebApplicationContext.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
// ========================================================================

package org.mortbay.j2ee;

import java.io.IOException;
import java.util.List;
import org.mortbay.jetty.servlet.WebApplicationContext;

public class
  J2EEWebApplicationContext
  extends WebApplicationContext
{
  public
    J2EEWebApplicationContext(String warUrl)
    throws IOException
    {
      super(warUrl);
    }

  //----------------------------------------------------------------------------
  // DistributedHttpSession support
  //----------------------------------------------------------------------------

  protected String _distributableHttpSessionManagerClass;
  public void setDistributableHttpSessionManagerClass(String managerClass) {_distributableHttpSessionManagerClass=managerClass;}
  public String getDistributableHttpSessionManagerClass() {return _distributableHttpSessionManagerClass;}

  protected String _distributableHttpSessionStoreClass;
  public void setDistributableHttpSessionStoreClass(String storeClass) {_distributableHttpSessionStoreClass=storeClass;}
  public String getDistributableHttpSessionStoreClass() {return _distributableHttpSessionStoreClass;}

  protected List _distributableHttpSessionInterceptorClasses;
  public void setDistributableHttpSessionInterceptorClasses(List interceptorClasses) {_distributableHttpSessionInterceptorClasses=interceptorClasses;}
  public List getDistributableHttpSessionInterceptorClasses() {return _distributableHttpSessionInterceptorClasses;}

  protected int _httpSessionMaxInactiveInterval=-1;	// never time out
  public void setHttpSessionMaxInactiveInterval(int i) {_httpSessionMaxInactiveInterval=i;}
  public int getHttpSessionMaxInactiveInterval() {return _httpSessionMaxInactiveInterval;}

  protected int _httpSessionActualMaxInactiveInterval=60*60*24*7; // a week
  public void setHttpSessionActualMaxInactiveInterval(int i) {_httpSessionActualMaxInactiveInterval=i;}
  public int getHttpSessionActualMaxInactiveInterval() {return _httpSessionActualMaxInactiveInterval;}

  protected int _localHttpSessionScavengePeriod=60*10; // 10 mins
  public void setLocalHttpSessionScavengePeriod(int i) {_localHttpSessionScavengePeriod=i;}
  public int getLocalHttpSessionScavengePeriod() {return _localHttpSessionScavengePeriod;}

  protected int _distributableHttpSessionScavengePeriod=60*60; // 1 hour
  public void setDistributableHttpSessionScavengePeriod(int i) {_distributableHttpSessionScavengePeriod=i;}
  public int getDistributableHttpSessionScavengePeriod() {return _distributableHttpSessionScavengePeriod;}

  protected int _distributableHttpSessionScavengeOffset=(int)(_localHttpSessionScavengePeriod*1.5); // 15 mins
  public void setDistributableHttpSessionScavengeOffset(int i) {_distributableHttpSessionScavengeOffset=i;}
  public int getDistributableHttpSessionScavengeOffset() {return _distributableHttpSessionScavengeOffset;}

  protected boolean _distributableHttpSession=false;
  public boolean getDistributableHttpSession() {return _distributableHttpSession;}
  public void setDistributableHttpSession(boolean distributable) {_distributableHttpSession=distributable;}

  //----------------------------------------------------------------------------

  protected boolean _stopGracefully=false;

  public void
    setStopGracefully(boolean stopGracefully)
    {
      if (isStarted())
	throw new IllegalStateException("setStopGracefully() must be called before J2EEWebApplicationContext is started");

      _stopGracefully=stopGracefully;
    }

  public boolean getStopGracefully() {return _stopGracefully;}

  public void
    start()
    throws Exception
    {
      if (_stopGracefully && !getStatsOn())
	setStatsOn(true);

      super.start();
    }

}
