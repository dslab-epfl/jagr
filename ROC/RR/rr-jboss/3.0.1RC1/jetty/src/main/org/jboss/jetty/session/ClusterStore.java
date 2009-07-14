
package org.jboss.jetty.session;

//----------------------------------------

import javax.management.*;
import javax.naming.InitialContext;
import org.jboss.ha.httpsession.server.ClusteredHTTPSessionServiceMBean;
import org.jboss.logging.Logger;
import org.jboss.util.jmx.MBeanProxy;
import org.mortbay.j2ee.session.Store;
import org.mortbay.j2ee.session.State;
import org.mortbay.j2ee.session.StateAdaptor;
import org.mortbay.j2ee.session.Manager;

//----------------------------------------

/**
 * A DistributedSession Store implemented on top of Sacha & Bill's
 * Clustering stuff...
 *
 * @author <a href="mailto:jules_gosnell@@yahoo.com">Jules Gosnell</a>
 * @version 1.0
 * @since 1.0
 */
public class ClusterStore
  implements Store
{
  final Logger                     _log    =Logger.getLogger(getClass().getName());
  MBeanServer                      _server =null;
  ObjectName                       _name   =null;
  ClusteredHTTPSessionServiceMBean _proxy;


  public
    ClusterStore(Manager manager)
  {
  }

  // Store LifeCycle
  public void
    start()
    throws Exception
  {
    // we are only expecting one server...
    _server=(MBeanServer)MBeanServerFactory.findMBeanServer(null).iterator().next();
    _name  =new ObjectName("jboss", "service", "ClusteredHttpSession");
    _proxy=(ClusteredHTTPSessionServiceMBean)
      MBeanProxy.create(ClusteredHTTPSessionServiceMBean.class, _name);
    _log.info("Support for Cluster-based Distributed HttpSessions loaded successfully: "+_name);
  }

  public void
    stop()
  {
  }

  public void
    destroy()
  {
  }

  // State LifeCycle
  public State
    newState(String id, int maxInactiveInterval)
  {
    ClusterStateEnvelope env=new ClusterStateEnvelope(_proxy, id);
    int actualMaxInactiveInterval=60*60*24; // TODO
    ClusterState state=new ClusterState(id, maxInactiveInterval, actualMaxInactiveInterval);
    env.storeState(id, state);
    return env;
  }

  public State
    loadState(String id)
  {
    ClusterStateEnvelope env=new ClusterStateEnvelope(_proxy, id);
    if (env.loadState(id)!=null) // could be static... - TODO
      return env;
    else
      return null;
  }

  public void
    storeState(State state)
  {
    // do nothing - it has already been done
  }

  public void
    removeState(State state)
  {
    String id="<unknown>";
    try
    {
      id=state.getId();
      _proxy.removeHttpSession(id);

      if (_log.isDebugEnabled())
	_log.debug("destroyed ClusterState: "+id);
    }
    catch (Throwable ignore)
    {
      _log.warn("removing unknown ClusterState: "+id);
    }
  }

  // ID allocation - we should use a decent ID allocation strategy here...
  public String
    allocateId()
  {
    String id=_proxy.getSessionId();

    if (_log.isDebugEnabled())
      _log.debug("allocating distributed HttpSession id: "+id);

    return id;
  }

  public void
    deallocateId(String id)
  {
    // the ids are not reused
  }

  public boolean
    isDistributed()
  {
    return true;
  }

  public void
    scavenge()
  {
    // Sacha's stuff does this for us...
  }

  public void
    passivateSession(StateAdaptor sa)
  {
    // if it's in the store - it's already passivated...
  }

  // this stuff has not yet been plumbed in since the HA HttpSession
  // Service does not publish a rich enough API - it just seems to try
  // to clean up every 30 secs... - later
  protected int _scavengerPeriod=60*30;	// 1/2 an hour
  protected int _scavengerExtraTime=60*30; // 1/2 an hour
  protected int _actualMaxInactiveInterval=60*60*24*28;	// 28 days

  public void setScavengerPeriod(int secs) {_scavengerPeriod=secs;}
  public void setScavengerExtraTime(int secs) {_scavengerExtraTime=secs;}
  public void setActualMaxInactiveInterval(int secs) {_actualMaxInactiveInterval=secs;}
}
