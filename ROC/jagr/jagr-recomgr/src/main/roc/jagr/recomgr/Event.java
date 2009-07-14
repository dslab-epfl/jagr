package roc.jagr.recomgr;

public abstract class Event {

    long timestamp;

    public Event() {
	timestamp = System.currentTimeMillis();
    }

    public abstract String getType();

    public long getTimeStamp() {
	return timestamp;
    }

}
