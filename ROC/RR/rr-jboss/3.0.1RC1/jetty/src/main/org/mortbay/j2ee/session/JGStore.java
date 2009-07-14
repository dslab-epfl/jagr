// ========================================================================
// Copyright (c) 2002 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: JGStore.java,v 1.1.1.1 2002/10/03 21:07:02 candea Exp $
// ========================================================================

package org.mortbay.j2ee.session;

//----------------------------------------
import java.util.Vector;
import java.util.Iterator;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import org.apache.log4j.Category;
import org.javagroups.Address;
import org.javagroups.Channel;
import org.javagroups.JChannel;
import org.javagroups.MembershipListener; // we are notified of changes to membership list
import org.javagroups.Message;
import org.javagroups.MessageListener; // we are notified of changes to other state
import org.javagroups.View;
import org.javagroups.blocks.GroupRequest;
import org.javagroups.blocks.MessageDispatcher;
import org.javagroups.blocks.MethodCall;
import org.javagroups.blocks.RpcDispatcher;
import org.javagroups.util.Util;

//----------------------------------------

// what happens if a member drops away for a while then comes back -
// can we deal with it ?

// quite a lot left to do:

// how do we bring ourselves or others up to date on startup whilst
// not missing any updates ? - talk to Bela

//how do we avoid the deserialisation cost like Sacha - store updates
//serialised and deserialise lazily (we would need a custom class so
//we don't get confused by a user storing their own Serialised objects
//?

// Talk to Sacha...

// It will be VERY important that nodes using this Store have their clocks synched...

/**
 * publish changes to our state, receive and dispatch notification of
 * changes in other states, initialise our state from other members,
 * allow other members to initialise their state from us - all via
 * JavaGroups...
 *
 * @author <a href="mailto:jules@mortbay.com">Jules Gosnell</a>
 * @version 1.0
 */
public class
  JGStore
  extends AbstractReplicatedStore
  implements MessageListener//, MembershipListener
{
  String _properties=""+
    "UDP(mcast_addr=228.8.8.8;mcast_port=45566;ip_ttl=32;" +
    "ucast_recv_buf_size=16000;ucast_send_buf_size=16000;" +
    "mcast_send_buf_size=32000;mcast_recv_buf_size=64000;loopback=true):"+
    "PING(timeout=2000;num_initial_members=3):"+
    "MERGE2(min_interval=5000;max_interval=10000):"+
    "FD_SOCK:"+
    "VERIFY_SUSPECT(timeout=1500):"+
    "pbcast.STABLE(desired_avg_gossip=20000):"+
    "pbcast.NAKACK(gc_lag=50;retransmit_timeout=300,600,1200,2400,4800;max_xmit_size=8192):"+
    "UNICAST(timeout=2000):"+
    "FRAG(frag_size=8192;down_thread=false;up_thread=false):"+
    "pbcast.GMS(join_timeout=5000;join_retry_timeout=2000;shun=false;print_local_addr=true):"+
    "pbcast.STATE_TRANSFER";

  protected Channel       _channel;
  protected RpcDispatcher _dispatcher;

  //----------------------------------------
  // Store API - Store LifeCycle

  public
    JGStore(Manager manager)
    {
      super(manager);

      try
      {
	// start up our channel...
	_channel=new JChannel(_properties); // channel should be JBoss or new Jetty channel

	MessageListener messageListener=this;
	MembershipListener membershipListener=null; //this - later
	Object serverObject=this;
	_dispatcher=new RpcDispatcher(_channel, messageListener, membershipListener, serverObject);

	_channel.setOpt(Channel.GET_STATE_EVENTS, new Boolean(true));
      }
      catch (Exception e)
      {
	_log.error("could not initialise JavaGroups Channel and Dispatcher");
      }
    }

  public String
    getChannelName()
    {
      return "HTTPSESSION_REPLICATION:"+getContextPath();
    }

  public void
    start()
    throws Exception
    {
      super.start();

      if (!_channel.isOpen()) _channel.open();
      _channel.connect(getChannelName()); // group should be on a per-context basis
      _dispatcher.start();

      int timeOut=500;
      if (!_channel.getState(null, timeOut))
	_log.info("could not retrieve current sessions from JavaGroups");
    }

  public void
    stop()
    {
      _dispatcher.stop();
      _channel.disconnect();
      _channel.close();

      super.stop();
    }

  public void
    destroy()
    {
      _dispatcher=null;
      _channel=null;

      super.destroy();
    }

  //----------------------------------------
  // AbstractReplicatedStore API

  protected void
    publish(String methodName, Class[] argClasses, Object[] argInstances)
    {
      try
      {
	Class[] tmp={String.class, Class[].class, Object[].class};
	MethodCall method = new MethodCall(getClass().getMethod("dispatch",tmp));
	method.addArg(methodName);
	method.addArg(argClasses);
	method.addArg(argInstances);

	synchronized (_dispatcher)
	{
	  // why doesn't dispatcher do this for us ?
	  // is it intended to be multi-threaded ?
	  _dispatcher.callRemoteMethods(null,method,GroupRequest.GET_ALL,0); // synchronous
	}
      }
      catch(Exception e)
      {
	_log.error("problem publishing change in state over JavaGroups", e);
      }
    }

  // JG doesn't find this method in our superclass ...
  public void
    dispatch(String methodName, Class[] argClasses, Object[] argInstances)
    {
      super.dispatch(methodName, argClasses, argInstances);
    }

  //----------------------------------------
  // 'MessageListener' API

  /**
   * receive notification of someone else's change in state
   *
   * @param msg a <code>Message</code> value
   */
  public void
    receive(Message msg)
    {
      //      _log.info("**************** RECEIVE CALLED *********************");
      byte[] buf=msg.getBuffer();
    }

  /**
   * copy our state to be used to initialise another store...
   *
   * @return an <code>Object</code> value
   */
  public Object
    getState()
    {
      _log.info("initialising another store from our current state");

      // this is a bit problematic - since we really need to freeze
      // every session before we can dump them... - TODO
      LocalState[] state;
      synchronized (_sessions)
      {
	_log.info("sending "+_sessions.size()+" sessions");

	state=new LocalState[_sessions.size()];
	int j=0;
	for (Iterator i=_sessions.values().iterator(); i.hasNext();)
	  state[j++]=((ReplicatedState)i.next()).getLocalState();
      }

      Object[] data={new Long(System.currentTimeMillis()), state};
      return data;
    }

  /**
   * initialise ourself from the current state of another store...
   *
   * @param new_state an <code>Object</code> value
   */
  public void
    setState(Object tmp)
    {
      if (tmp!=null)
      {
	_log.info("initialising our state from another Store");

	Object[] data=(Object[])tmp;

	long remoteTime=((Long)data[0]).longValue();
	long localTime=System.currentTimeMillis();
	long disparity=(localTime-remoteTime)/1000;
	_log.info("time disparity: "+disparity+" secs");

	LocalState[] state=(LocalState[])data[1];
	_log.info("receiving "+state.length+" sessions...");

	for (int i=0; i<state.length; i++)
	{
	  LocalState ls=state[i];
	  _sessions.put(ls.getId(), new ReplicatedState(this, ls));
	}
      }
    }

  //----------------------------------------
//   // 'MembershipListener' API
//
//   // Block sending and receiving of messages until viewAccepted() is called
//   public void
//     block()
//     {
//       _log.info("??? block()");
//     }
//
//   // Called when a member is suspected
//   public void
//     suspect(Address suspected_mbr)
//     {
//       _log.info("??? suspect()");
//     }
//
//   protected Vector _members=new Vector(); // keeps track of all DHTs
//   protected Vector _notifs=new Vector();  // to be notified when mbrship changes
//
//   public void
//     viewAccepted(View new_view)
//     {
//       _log.info("??? viewAccepted()");
//
//       Vector new_mbrs=new_view.getMembers();
//
//       if (new_mbrs != null)
//       {
// 	sendViewChangeNotifications(new_mbrs, _members); // notifies observers (joined, left)
// 	_members.removeAllElements();
// 	_members.addAll(new_mbrs);
//       }
//     }
//
//   public interface Notification
//   {
//     void entrySet(Object key, Object value);
//     void entryRemoved(Object key);
//     void viewChange(Vector new_mbrs, Vector old_mbrs);
//   }
//
//   protected void
//     sendViewChangeNotifications(Vector new_mbrs, Vector old_mbrs)
//     {
//       Vector        joined, left;
//       Object        mbr;
//       Notification  n;
//
//       if(_notifs.size() == 0 || old_mbrs == null || new_mbrs == null ||
// 	 old_mbrs.size() == 0 || new_mbrs.size() == 0)
// 	return;
//
//       // 1. Compute set of members that joined: all that are in new_mbrs, but not in old_mbrs
//       joined=new Vector();
//       for (int i=0; i < new_mbrs.size(); i++)
//       {
// 	mbr=new_mbrs.elementAt(i);
// 	if (!old_mbrs.contains(mbr))
// 	  joined.addElement(mbr);
//       }
//
//
//       // 2. Compute set of members that left: all that were in old_mbrs, but not in new_mbrs
//       left=new Vector();
//       for (int i=0; i < old_mbrs.size(); i++)
//       {
// 	mbr=old_mbrs.elementAt(i);
// 	if (!new_mbrs.contains(mbr))
// 	{
// 	  left.addElement(mbr);
// 	}
//       }
//
//       for (int i=0; i < _notifs.size(); i++)
//       {
// 	n=(Notification)_notifs.elementAt(i);
// 	n.viewChange(joined, left);
//       }
//     }
}
