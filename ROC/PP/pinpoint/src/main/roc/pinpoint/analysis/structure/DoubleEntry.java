package roc.pinpoint.analysis.structure;

import roc.pinpoint.util.StringAtoms;
import java.io.*;
import java.util.*;


public final class DoubleEntry implements Comparable {
    int atom;
    double weight;

    DoubleEntry( int atom ) {
	LockedComponentBehavior.SanityCheckAtom( atom );
        this.atom = atom;
        weight = 1;
    }
        
    DoubleEntry( DoubleEntry de, double multiplier ) {
	LockedComponentBehavior.SanityCheckAtom( de.atom );
        this.atom = de.atom;
        this.weight = de.weight * multiplier;
    }

    DoubleEntry( int atom, double weight ) {
	LockedComponentBehavior.SanityCheckAtom( atom );
        this.atom = atom;
        this.weight = weight;
    }

    public int compareTo( Object o ) {
	DoubleEntry other = (DoubleEntry)o;

	double diff = weight - other.weight;
	if( diff < 0 ) {
	    return 1;
	}
	else if( diff > 0 ) {
	    return -1;
	}
	else {
	    return atom - other.atom;
	}
    }

    static String SetToString( StringAtoms atoms, DoubleEntry[] entries ) {
        StringBuffer ret = new StringBuffer();
        ret.append( "{\n" );
        for( int i=0; i <entries.length; i++ ) {
            DoubleEntry de = entries[i];
            ret.append( "\t" ).append( atoms.getLabel( de.atom ));
	    ret.append( "#" ).append( de.atom );
            ret.append( "[" ).append( de.weight ).append( "]" );
            if( i+1<entries.length )
                ret.append( ",\n\t" );
        }
        ret.append( "\n}" );
        return ret.toString();
    }


    static String SetToString( StringAtoms atoms, Collection entries ) {
        StringBuffer ret = new StringBuffer();
        ret.append( "{\n" );
	Iterator iter = entries.iterator();
	while( iter.hasNext() ) {
            DoubleEntry de = (DoubleEntry)iter.next();
            ret.append( "\t" ).append( atoms.getLabel( de.atom ));
	    ret.append( "#" ).append( de.atom );
            ret.append( "[" ).append( de.weight ).append( "]" );
	    ret.append( ",\n\t" );
        }
        ret.append( "\n}" );
        return ret.toString();
    }


    static String SortSetToString( StringAtoms atoms, Collection entries ) {
        StringBuffer ret = new StringBuffer();

	TreeSet sortedEntries = new TreeSet( entries );

        ret.append( "{\n" );
	Iterator iter = sortedEntries.iterator();
	while( iter.hasNext() ) {
            DoubleEntry de = (DoubleEntry)iter.next();
            ret.append( "\t" ).append( atoms.getLabel( de.atom ));
	    ret.append( "#" ).append( de.atom );
            ret.append( "[" ).append( de.weight ).append( "]" );
	    ret.append( ",\n\t" );
        }
        ret.append( "\n}" );
        return ret.toString();
    }


    public String toString( StringAtoms atoms ) {
	StringBuffer ret = new StringBuffer();
	ret.append( atoms.getLabel( atom ));
	ret.append( "#" ).append( atom );
	ret.append( "[" ).append( weight ).append( "]" );
	return ret.toString();
    }


    public void writeExternal( StringAtoms atoms, ObjectOutput out ) 
        throws IOException {
        out.writeObject( atoms.getLabel( atom ));
        out.writeDouble( weight );
    }

    public static DoubleEntry readExternal( StringAtoms atoms, 
                                            ObjectInput in ) 
        throws IOException, ClassNotFoundException {
        String label = (String)in.readObject();
        double weight = (double)in.readDouble();

//        System.err.println( "EMKDEBUG: DoubleEntry.readExternal: label=" + label + "; weight=" + weight );

        return new DoubleEntry( atoms.getAtom( label ),
                                weight );
    }
}

