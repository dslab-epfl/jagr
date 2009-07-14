package roc.recomgr.action;

import java.util.*;
import roc.recomgr.*;
import roc.recomgr.event.*;

/**
 * schedule an action that will send us a timer event with an arbitrary
 * message at some point in the future (or many points, if we set a period
 * for repeating the task). 
 * Note: if you set a repeating task, you won't be able to remove it
 * without cancelling all timers!
 */
public class TimerAction extends RecoveryAction {

    static Timer timer = new Timer();
    
    Object msg;
    long delay;
    long period;

    public TimerAction( RecoveryManager mgr, Object msg, long delay ) {
	this( mgr, msg, delay, -1 );
    }

    public TimerAction( RecoveryManager mgr, Object msg, 
			long delay, long period ) {
	super( mgr );
	this.msg = msg;
	this.delay = delay;
	this.period = period;
    }

    public void doAction() {
	TimerTask timertask = new TimerTask() {
		public void run() {
		    mgr.receiveEvent( new TimerEvent( msg ));
		}
	    }; 
	if( period == -1 ) {
	    timer.schedule( timertask, delay );
	}
	else {
	    timer.schedule( timertask, delay, period );
	}
    }


    public static void cancelAllTimerEvents() {
	timer.cancel();
	timer = new Timer();
    }
}
