package roc.pinpoint.util;

import java.util.*;

public class StringAtoms {

    HashMap labelsToAtoms;
    ArrayList atomsToLabels;
    int currAtom;

    public void SanityCheck() {
	if( labelsToAtoms.size() != atomsToLabels.size() ) {
	    System.err.println( "! ACK! String Atoms failed sanity check" );

	    HashSet labels = new HashSet(labelsToAtoms.keySet());

	    for( int i=0; i<atomsToLabels.size(); i++ ) {
		String label = (String)atomsToLabels.get(i);
		Integer atom = (Integer)labelsToAtoms.get(label);
		if( atom == null ) {
		    System.err.println( "<" + label + "," + i + 
					"> exists in atomsToLabels," +
					" but not in labelsToAtoms" );
		}
		else if( atom.intValue() != i ) {
		    System.err.println( "<" + label + "," + i + 
					"> mismatched <" + label + 
					"," + atom + ">" );
		    labels.remove( label );
		}
		else { 
		    labels.remove( label );
		}
	    }

	    Iterator iter = labels.iterator();
	    while( iter.hasNext() ) {
		String label = (String)iter.next();
		Integer atom = (Integer)labelsToAtoms.get( label );
		String otherLabel = "<null>";
		if( atomsToLabels.size() > atom.intValue() )
		    otherLabel = (String)atomsToLabels.get( atom.intValue() );
		System.err.println( "<" + label + "," + atom + "> " +
				    "exists in labelsToAtoms but not in " +
				    " labelsToAtoms: <" + otherLabel + "," +
				    atom.intValue() + ">" );


	    }


	    System.err.println( "\t\tlabelsToAtoms = " + labelsToAtoms.toString() );
	    System.err.println( "\t\tatomsToLabels =" + atomsToLabels.toString() );

	    throw new RuntimeException();
	}
    }

    boolean debug = true;

    public StringAtoms() {
        labelsToAtoms = new HashMap();
        atomsToLabels = new ArrayList();
        currAtom = 0;
    }

    public int getAtom( String s ) {

        if( s == null ) {
            throw new NullPointerException( "called StringAtoms.getAtom() with null value" );
        }

        int ret = -1;

        if( !labelsToAtoms.containsKey( s )) {
            labelsToAtoms.put( s, new Integer( currAtom ));
            atomsToLabels.add( s );
            currAtom++;
            ret = currAtom-1;
        }
        else {
            ret = ((Integer)labelsToAtoms.get(s)).intValue();
        }

        return ret;
    }

    public String getLabel( int atom ) {
        String ret = (String)atomsToLabels.get(atom);
        if( ret == null ) {
            throw new RuntimeException( "ILLEGAL ATOM: " + atom );
        }

        return ret;
    }

    public String toString( int[] atoms ) {
        StringBuffer ret = new StringBuffer();
        ret.append( "{" );
        for( int i=0; i<atoms.length; i++ ) {
            ret.append( (String)atomsToLabels.get(atoms[i]));
            if( i<atoms.length )
                ret.append( "," );
        }
        ret.append( "}" );
        return ret.toString();
    }

}
