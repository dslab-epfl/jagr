/**
    Copyright (C) 2004 Emre Kiciman and Stanford University

    This file is part of Pinpoint

    Pinpoint is free software; you can distribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2.1 of the License, or
    (at your option) any later version.

    Pinpoint is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with Pinpoint; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
**/
package roc.pinpoint.analysis;

// marked for release 1.0

import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;

import org.apache.log4j.Logger;

/**
 * A record is a wrapper for an arbitrary java object.  The analysis engine
 * and plugins use this wrapper to track attributes about the object, such
 * as its age, etc.
 * @author emrek
 *
 */
public class Record implements Serializable {
	
    static Logger log = Logger.getLogger( "Record" );

    static final long serialVersionUID = 4585623068104990445L;

    private static final int MAP_INITIAL_CAPACITY=0;
    
    static {
    	for( int i=0; i<10; i++ ) {
	    log.info( "loaded");    
    	}
    }

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

    public String toString() {
	return "Record { " + 
	    ((value==null)?"null":(value.toString())) +
	    "}";
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
