package roc.pinpoint.analysis;

import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;

/**
 * A record is a wrapper for an arbitrary java object.  The analysis engine
 * and plugins use this wrapper to track attributes about the object, such
 * as its age, etc.
 * @author emrek
 *
 */
public class Record implements Serializable {

    private static final int MAP_INITIAL_CAPACITY = 0;

    private Object value;

    private Map attributes;
    private Map transientAttributes;
    // transient values aren't copied when a record is duplicated

    /**
     * simple constructor.
     */
    public Record() {
        value = null;
        attributes = new HashMap( MAP_INITIAL_CAPACITY );
        transientAttributes = new HashMap( MAP_INITIAL_CAPACITY );
    }

    /**
     * detailed constructor
     * @param value  the value being wrapped
     * @param attributes  attributes to associate with this value
     * @param transientAttributes transient attributes to associate with this
     * value
     */
    public Record(Object value, Map attributes, Map transientAttributes) {
        this.value = value;
        this.attributes = (attributes == null) ? new HashMap(MAP_INITIAL_CAPACITY) : attributes;
        this.transientAttributes =
            (transientAttributes == null) ? new HashMap(MAP_INITIAL_CAPACITY) : transientAttributes;
    }

    /**
     * creates a duplicate record, shallow copying everything but the transient
     * attributes
     * @param record record to duplicate
     */
    public Record(Record record) {
        this.value = record.value;
        this.attributes = new HashMap(record.attributes);
        this.transientAttributes = new HashMap( MAP_INITIAL_CAPACITY);
        // transient attrs aren't copied upon duplication
    }

    /**
     * simple constructor, attributes are set to null
     * @param value the value being wrapped
     */
    public Record(Object value) {
        this(value, new HashMap( MAP_INITIAL_CAPACITY ), new HashMap( MAP_INITIAL_CAPACITY ));
    }

    /**
     * set accessor method for Value
     * @param value value being wrapped
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * get accessor method for value
     * @return Object value being mapped.
     */
    public Object getValue() {
        return value;
    }

    /**
     * set an attribute associated with this value/record
     * @param key  attribute key
     * @param value attribute value
     * @param isTransient whether this attribute is transient or not.
     */
    public void setAttribute(String key, Object value, boolean isTransient) {
        if (isTransient) {
            transientAttributes.put(key, value);
        }
        else {
            attributes.put(key, value);
        }
    }

    /**
     * removes the specified attribute (whether it's transient or not)
     * @param key attribute key
     */
    public void removeAttribute(String key) {
        if (attributes.containsKey(key)) {
            attributes.remove(key);
        }
        if (transientAttributes.containsKey(key)) {
            transientAttributes.remove(key);
        }
    }

    /**
     * returns attribute value (transient or not)
     * @param key attribute key
     * @return Object attribute value 
     */
    public Object getAttribute(String key) {
        if (attributes.containsKey(key)) {
            return attributes.get(key);
        }
        else {
            return transientAttributes.get(key);
        }

    }

}
