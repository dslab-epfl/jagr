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
package roc.pinpoint.tracing;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// marked for release 1.0

/**
 * this class represents a single observation, made as a request accesses a
 * resource or uses a component
 * @author emrek
 *
 */
public class Observation implements Externalizable, Comparable {

    // this is the original serialVersion UID; 
    //static final long serialVersionUID = 7094291743174737727L;

    static final long serialVersionUID = 7094291743174737728L;

    static Map WriteCachedStrings = new HashMap();
    static ArrayList ReadCachedStrings = new ArrayList();
    
    /* types of events */
    
    /** NULL event, means this observation should be ignored by most tools. **/
    public static final int EVENT_NULL = 0;
    /** a component was used **/
    public static final int EVENT_COMPONENT_USE = 1;
    /** a database access **/
    public static final int EVENT_DATABASE_USE = 2;
    /** a likely error was observed **/
    public static final int EVENT_ERROR = 3;
    /** component details **/
    public static final int EVENT_COMPONENT_DETAILS = 4;
    // more observation events to come later, i'm sure...
    public static final int EVENT_LINK = 5;

    /** text description of event types, for toString() **/
    public static final String[] TEXT_EVENT_DESCR =
        { "null", "component use", "database use", "error", 
	  "component details", "async link" };

    public int eventType;
    public String requestId; // id of request we are observing (if applicable)
    public String groupId; // id for async requests...
    public int sequenceNum;

    public long originTimestamp; // time (at origin) event was observed
    public long collectedTimestamp; // time (at central observer)

    public Map originInfo;
    /* component name, method name, args, etc. of origin of observation */

    public Map rawDetails;
    /* unparsed details, such as a low-level SQL query, stack trace, etc. */

    public Map attributes; // other attributes of observation

    public void recycle() {
	recycleMap( originInfo );
	recycleMap( rawDetails );
	recycleMap( attributes );
    }

    private void recycleMap( Map map ) {
	if( map instanceof HashMap ) {
	    map.clear();
	    SimpleObjectPool.HASHMAP_POOL.put( map );
	}
    }

    /**
     * default constructor 
     * @see java.lang.Object#Object()
     */
    public Observation() {
        eventType = EVENT_NULL;

        originInfo = (HashMap)SimpleObjectPool.HASHMAP_POOL.get();
        rawDetails = (HashMap)SimpleObjectPool.HASHMAP_POOL.get();
        attributes = (HashMap)SimpleObjectPool.HASHMAP_POOL.get();
    }

    /**
     * constructor
     * @param eventType eventype
     * @param requestId request id
     * @param sequenceNum sequence number
     * @param originInfo information about where this observation was collected
     * @param rawDetails raw details, such as sql statements, or stack traces
     * @param attributes attributes of the observation
     */
    public Observation(
        int eventType,
        String requestId,
        int sequenceNum,
        Map originInfo,
        Map rawDetails,
        Map attributes) {
        this.eventType = eventType;
        this.requestId = requestId;
	this.groupId = null;
        this.sequenceNum = sequenceNum;
        this.originTimestamp = System.currentTimeMillis();
        this.collectedTimestamp = -1;
        this.originInfo = originInfo;
        this.rawDetails = rawDetails;
        if (this.rawDetails == null) {
            this.rawDetails = (HashMap)SimpleObjectPool.HASHMAP_POOL.get();
        }
        else if (!(this.rawDetails instanceof HashMap)) {
            HashMap tmp = (HashMap)SimpleObjectPool.HASHMAP_POOL.get();
	    tmp.putAll( this.rawDetails );
	    this.rawDetails = tmp;
        }
        this.attributes = attributes;
        if (this.attributes == null) {
            this.attributes = (HashMap)SimpleObjectPool.HASHMAP_POOL.get();
        }
        else if (!(this.attributes instanceof HashMap)) {
	    HashMap tmp = (HashMap)SimpleObjectPool.HASHMAP_POOL.get();
	    tmp.putAll( this.attributes );
	    this.attributes = tmp;
        }
    }

    /**
     * @see roc.pinpoint.tracing.Observation#Observation(int,
     *  String,    int, Map, Map, Map )
     */
    public Observation(
        int eventType,
        RequestInfo reqinfo,
        Map originInfo,
        Map rawDetails,
        Map attributes) {
        this(
            eventType,
            reqinfo.getRequestId(),
            reqinfo.getSeqNum(),
            originInfo,
            rawDetails,
            attributes);
    }

    public Observation( DataInput dis ) throws IOException {
	readExternalD( dis );
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("Observation: [\n");
        buf.append("\teventType = ");
        buf.append(TEXT_EVENT_DESCR[eventType]);
        buf.append("\n\trequestId = ");
        buf.append(requestId);
        buf.append("\n\tsequenceNum = ");
        buf.append(sequenceNum);
        buf.append("\n\toriginTimestamp = ");
        buf.append(originTimestamp);
        buf.append("\n\tcollectedTimestamp = ");
        buf.append(collectedTimestamp);
        buf.append("\n\toriginInfo = ");
        buf.append(originInfo);
        buf.append("\n\trawDetails = ");
        buf.append(rawDetails);
        buf.append("\n\tattributes = ");
        buf.append(attributes);
        buf.append("\n]");

        return buf.toString();
    }

    /**
     * compare/sort based on sequence number of observation
     * @see java.lang.Comparable#compareTo(Object)
     */
    public int compareTo(Object o) {

        if (!(o instanceof Observation)) {
            throw new ClassCastException(
                "Cannot compare Observation to " + o.getClass().toString());
        }

        Observation obs = (Observation) o;

        if (!obs.requestId.equals( this.requestId )) {
	    System.err.println( "THIS=" + this.toString() );
	    System.err.println( "OTHER=" + o.toString() );
            throw new ClassCastException("Cannot compare Observations" 
   + " with different request ids!");
        }

        return (this.sequenceNum < obs.sequenceNum)
            ? -1
            : ((this.sequenceNum > obs.sequenceNum) ? 1 : 0);
    }


    public void writeExternal( ObjectOutput out ) 
	throws IOException {
	writeExternalD( out );
    }

    public void writeExternalD( DataOutput out ) 
	throws IOException {

	out.writeByte( eventType );
	writeSimpleObjectHelper( out, requestId );
	writeSimpleObjectHelper( out, groupId );
	out.writeInt( sequenceNum );
	out.writeLong( originTimestamp );
	out.writeLong( collectedTimestamp );
	writeMapHelper( out, originInfo );
	writeMapHelper( out, rawDetails );
	writeMapHelper( out, attributes );
    }

    private void writeMapHelper( DataOutput out, Map map ) 
	throws IOException {
	if( map == null ) {
	    out.writeByte( 0 );
	}
	else {
	    out.writeByte( map.size() );

	    Iterator iter = map.entrySet().iterator();
	    while( iter.hasNext() ) {
	        Map.Entry entry = (Map.Entry)iter.next();
	        writeSimpleObjectHelper( out, entry.getKey() );
	        writeSimpleObjectHelper( out, entry.getValue() );
	    }
	}
    }

    private void writeSimpleObjectHelper( DataOutput out, Object o )
	throws IOException {

	if( o == null ) {
	    out.writeByte( 0 );
	}
	else if( o instanceof Boolean ) {
	    out.writeByte( 1 );
	    out.writeBoolean( ((Boolean)o).booleanValue() );
	}
	else if( o instanceof Byte ) {
	    out.writeByte( 2 );
	    out.writeByte( ((Byte)o).intValue() );
	}
	else if( o instanceof Character ) {
	    out.writeByte( 3 );
	    out.writeChar( ((Character)o).charValue() );
	}
	else if( o instanceof Double ) {
	    out.writeByte( 4 );
	    out.writeDouble( ((Double)o).doubleValue() );
	}
	else if( o instanceof Float ) {
	    out.writeByte( 5 );
	    out.writeFloat( ((Float)o).floatValue() );
	}
	else if( o instanceof Integer ) {
	    out.writeByte( 6 );
	    out.writeInt( ((Integer)o).intValue() );
	}
	else if( o instanceof Long ) {
	    out.writeByte( 7 );
	    out.writeLong( ((Long)o).longValue() );
	}
	else if( o instanceof Short ) {
	    out.writeByte( 8 );
	    out.writeShort( ((Short)o).shortValue() );
	}
	else if( o instanceof String ) {
	    if( WriteCachedStrings.containsKey( o )) {
		out.writeByte( 11 );
		out.writeInt( ((Integer)WriteCachedStrings.get( o )).intValue() );
	    }
	    else {
		out.writeByte( 9 );
		String s = (String)o;
		int id = WriteCachedStrings.size();
		//		System.err.println( "EMKDEBUG: Observation(T=" + Thread.currentThread().toString() + "): id=" + id + "; string=" + s );
		WriteCachedStrings.put( s, new Integer( id ));
		out.writeInt( id );
		out.writeInt( s.length() );
		out.writeBytes( s );
	    }
	}
	else if( o instanceof Throwable ) {
	    Throwable t = (Throwable)o;

	    StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter( sw );
	    t.printStackTrace(pw);
	    pw.flush();
	    String s = sw.toString();

	    // write the throwable out as a string.
	    out.writeByte( 9 );
	    out.writeInt( s.length() );
	    out.writeBytes( s );    
	}
	else if( o instanceof List ) {
	    List l = (List)o;
	    out.writeByte( 10 );
	    out.writeByte( l.size() );
            Iterator iter = l.iterator();
	    while( iter.hasNext() ) {
		writeSimpleObjectHelper( out, iter.next() );
	    }
	}
	else {
	    throw new RuntimeException( "AAAACK! UNKNOWN OBJECT TYPE IN OBSERVATION!!!! : " + ((o==null)?"null":(o.getClass().toString())) );
	}

    }

    public void readExternal( ObjectInput in ) 
	throws IOException {
	readExternalD( in );
    }

    public void readExternalD( DataInput in ) 
	throws IOException {
	
	eventType = in.readByte();
	requestId = (String)readSimpleObjectHelper(in);
	groupId = (String)readSimpleObjectHelper(in);
	sequenceNum = in.readInt();
	originTimestamp = in.readLong();
	collectedTimestamp = in.readLong();
	originInfo = readMapHelper( in );
	rawDetails = readMapHelper( in );
	attributes = readMapHelper( in );
    }


    private Map readMapHelper( DataInput in ) 
	throws IOException {
	int n = in.readByte();
	
	if( n == 0 )
	    return null;

	HashMap ret = (HashMap)SimpleObjectPool.HASHMAP_POOL.get();

	for( int i=0; i<n; i++ ) {
	    Object k = readSimpleObjectHelper(in);
	    Object v = readSimpleObjectHelper(in);
	    ret.put( k, v );
	}

	return ret;
    } 

    private Object readSimpleObjectHelper( DataInput in ) 
	throws IOException {

	int code = in.readByte();

	if( code == 0 ) {
	    return null;
	}
	else if( code == 1 ) {
	    return Boolean.valueOf( in.readBoolean());
	}
	else if( code == 2 ) {
	    return new Byte( in.readByte() );
	}
	else if( code == 3 ) {
	    return new Character( in.readChar() );
	}
	else if( code == 4 ) {
	    return new Double( in.readDouble() );
	}
	else if( code == 5 ) {
	    return new Float( in.readFloat() );
	}
	else if( code == 6 ) {
	    return new Integer( in.readInt() );
	}
	else if( code == 7 ) {
	    return new Long( in.readLong() );
	}
	else if( code == 8 ) {
	    return new Short( in.readShort() );
	}
	else if( code == 9 ) {
	    int id = in.readInt();
	    int len = in.readInt();
	    byte[] b = new byte[ len ];
	    in.readFully( b );
	    String ret = new String( b );
	    //System.err.println( "EMKDEBUG: Observation: id=" + id + "; string=" + ret );
	    if( ReadCachedStrings.size() == id ) {
		ReadCachedStrings.add( ret );
	    }
	    else {
		//System.err.println( "EMKDEBUG: Observation.java got id=" + id + " but cache.size()=" + ReadCachedStrings.size() );
		throw new RuntimeException( "ACK! we lost synchronization on cached Strings!!!" );
	    }
	    return ret;
	}
	else if( code == 10 ) {
	    int size = in.readByte();
	    ArrayList l = new ArrayList( size );
	    for( int i=0;i<size;i++ ) {
		Object el = readSimpleObjectHelper(in);
		l.add( el );
	    }
	    return l;
	}
	else if( code == 11 ) {
	    int id = in.readInt();
	    return (String)ReadCachedStrings.get(id);
	}
	else {
	    throw new RuntimeException( "Unrecognized object type '" + code + "' in Observation.ReadSimpleObjectHelper();" );
	}
    }


}
