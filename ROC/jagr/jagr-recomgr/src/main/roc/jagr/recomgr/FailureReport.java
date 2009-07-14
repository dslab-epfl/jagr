package roc.jagr.recomgr;

import java.io.*;
import java.util.*;

public class FailureReport implements Serializable {

    String source;
    SortedSet suspects;


    public FailureReport( String source, SortedSet suspects ) {
	this.source = source;
	this.suspects = suspects;
    }

    public FailureReport() {
	this.suspects = new TreeSet();
    }

    public FailureReport( String source ) {
	this.source = source;
	this.suspects = new TreeSet();
    }

    public void setSource( String source ) {
	this.source = source;
    }

    public String getSource() {
	return source;
    }

    public void addSuspect( Suspect s ) {
	suspects.add( s );
    }

    public SortedSet getSuspects() {
	return suspects;
    }

    public static FailureReport fromByteArray( byte[] buf, int offset, int length ) {
	try {
	    ObjectInputStream ois = new ObjectInputStream( new ByteArrayInputStream( buf, offset, length ));
	    FailureReport ret = (FailureReport)ois.readObject();
	    return ret;
	}
	catch( IOException shouldnthappen ) {
	    throw new RuntimeException( "ACK! no real I/O, but still an I/O exception?!", shouldnthappen );
	}
	catch( ClassNotFoundException shouldnthappen ) {
	    throw new RuntimeException( "ACK! no real I/O, but still an I/O exception?!", shouldnthappen );
	}
    }

    public String toString() {
	return "{FailureReport: source=" + source + ", suspects=" + suspects + "}";
    }
    
    public byte[] toByteArray() {
	try {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ObjectOutputStream oos = new ObjectOutputStream( baos );
	    oos.writeObject( this );
	    oos.flush();
	    byte[] ret = baos.toByteArray();
	    return ret;
	}
	catch( IOException shouldnthappen ) {
	    throw new RuntimeException( "ACK! no real I/O, but still an I/O exception?!", shouldnthappen );
	}
    }

    public static class Suspect implements Comparable, Serializable {
	String name;
	double anomaly;	
	
	public Suspect( String name, double anomaly ) {
	    this.name = name;
	    this.anomaly = anomaly;
	}
	
	public String getName() {
	    return name;
	}

	public double getAnomaly() {
	    return anomaly;
	}

	public int compareTo( Object o ) {
	    Suspect other = (Suspect)o;
	    if( this.anomaly > other.anomaly ) {
		return 1;
	    }
	    else if( this.anomaly < other.anomaly ) {
		return -1;
	    }
	    
	    return this.name.compareTo( other.name );
	    
	}

	public String toString() {
	    return "{Suspect: name=" + name + ", anomaly=" + anomaly + "}";
	}
    }
    
}
