package roc.jagr.recomgr.event;

import roc.jagr.recomgr.Event;

public class TimerEvent extends Event {

    public static final String TYPE = "TimerEvent";

    Object msg;

    public TimerEvent() {
	this("");
    }

    public TimerEvent( Object msg ) {
	this.msg = msg;
	
    }

    public String getType() {
	return TYPE;
    }

    public Object getMsg() {
	return msg;
    }

}
