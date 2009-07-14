package roc.pinpoint.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * a collection of records, plus attributes about them.  This class
 * notifies registered listeners when the collection contents change.
 * @author emrek
 *
 */
public class RecordCollection {

    public static final String IS_TRANSIENT_ATTR = "transient";

    private String collectionName;

    private Map collectionAttributes;
    private Set listeners;

    private Map records;

    private boolean isTransient;

    /**
     * constructor
     * @param name name of this record collection
     * @param attrs attributes to associate with this collection
     */
    public RecordCollection(String name, Map attrs) {
        this.collectionName = name;
        collectionAttributes = new HashMap(attrs);
        records = new HashMap();
        listeners = new HashSet();
	isTransient = false;
	if( attrs.containsKey( IS_TRANSIENT_ATTR )) {
	    setIsTransient( attrs.get( IS_TRANSIENT_ATTR ));
	}
    }

    private void setIsTransient( Object value ) {
	isTransient = (value instanceof Boolean ) 
	    ? ((Boolean)value).booleanValue() 
	    : ( Boolean.valueOf( value.toString() ).booleanValue() );
    }

    /**
     * returns an attribute associated with this record collection
     * @param key attribute key
     * @return Object  attribute value
     */
    public Object getAttribute(String key) {
        return collectionAttributes.get(key);
    }

    /**
     *  sets an attribute, associated with this record collection.
     * @param key  attribute key
     * @param value  attribute value
     */
    public void setAttribute(String key, Object value) {
	if( IS_TRANSIENT_ATTR.equals( key )) {
	    setIsTransient( value );
	}
        collectionAttributes.put(key, value);
    }

    /**
     * returns an unmodifiable view of all attributes
     * @return Map all attributes associted with this record collection
     */
    public Map getAllAttributes() {
        return Collections.unmodifiableMap(collectionAttributes);
    }

    /**
     * returns a record by key
     * @param key the key of a stored record
     * @return Record  the record
     */
    public synchronized Record getRecord(Object key) {
        return (Record) records.get(key);
    }

    /**
     * sets a record, naming it 'key'
     * @param key name of the record
     * @param record record value
     */
    public synchronized void setRecord(Object key, Record record) {
	if( !isTransient ) {
	    records.put(key, record);
	}

        // notifyListeners
        // todo -- batch notifications to listeners...

	record.setAttribute( "key", key, true );

        List r = Collections.singletonList(record);

        Iterator iter = listeners.iterator();
        while (iter.hasNext()) {
            RecordCollectionListener l = (RecordCollectionListener) iter.next();
            try {
                l.addedRecords(collectionName, r);
            }
            catch (Throwable t) {
                t.printStackTrace();
		System.err.println( "[RecordCollection]: Exception ignored. Continuing..." );
	    }
        }
    }

    /**
     * returns an unmodifiable map of all records in this collection
     * @return Map all the records in this collection, indexed by key
     */
    public Map getAllRecords() {
        return Collections.unmodifiableMap(records);
    }

    /**
     * @return int number of records stored in this collection
     */
    public int size() {
        return records.size();
    }

    /**
     * removes a record from this collection
     * @param key the key of a stored record.
     */
    public synchronized void removeRecord(String key) {
        Record record = (Record) records.remove(key);

        List r = Collections.singletonList(record);

        // notifyListeners
        Iterator iter = listeners.iterator();
        while (iter.hasNext()) {
            RecordCollectionListener l = (RecordCollectionListener) iter.next();
            try {
                l.removedRecords(collectionName, r);
            }
            catch (Exception e) {
                e.printStackTrace();
		System.err.println( "[RecordCollection]: Exception ignored. Continuing..." );
            }
        }
    }

    /**
     * removes all records from this collection
     */
    public synchronized void clearAllRecords() {
        List r = new ArrayList(records.values());
        records = new HashMap();

        //notify listeners
        Iterator iter = listeners.iterator();
        while (iter.hasNext()) {
            RecordCollectionListener l = (RecordCollectionListener) iter.next();
            try {
                l.removedRecords(collectionName, r);
            }
            catch (Exception e) {
                e.printStackTrace();
		System.err.println( "[RecordCollection]: Exception ignored. Continuing..." );
            }
        }
    }

    /**
     * add all the specified attributes as attributes of this collection
     * @param attrs attributes to add
     */
    public synchronized void addAllAttributes(Map attrs) {
        collectionAttributes.putAll(attrs);
    }

    /**
     * registers a RecordCollectionListener with this collection.  This
     * listener will be called whenever records are added or removed from this
     * collection
     * @param listener a reference to the listener
     */
    public void registerListener(RecordCollectionListener listener) {
        listeners.add(listener);
    }

    /**
     * unregister a listener from this collection.
     * @param listener a listener 
     */
    public void unregisterListener(RecordCollectionListener listener) {
        listeners.remove(listener);
    }

}
