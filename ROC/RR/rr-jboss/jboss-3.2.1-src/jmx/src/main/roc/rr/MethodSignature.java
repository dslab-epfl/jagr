package roc.rr;

import java.io.Serializable;
import java.util.*;

/**
 *
 * The signature of a method in a recursively rebootable component.
 *      
 *   @author  candea@cs.stanford.edu
 *   @version $Revision: 1.1 $
 *
 **/

public class MethodSignature implements Serializable 
{
    /*---------------------------------------------------------------------------
     * FIELDS
     *---------------------------------------------------------------------------*/

    /**
     * Name of this method.
     **/
    public String name;

    /**
     * A linked list containing the exceptions that can be thrown by this method.
     **/
    public LinkedList faults;


    /*---------------------------------------------------------------------------
     * PUBLIC METHODS
     *---------------------------------------------------------------------------*/

    /** 
     * Constructor for given MethodSignature name.
     *
     * @param  mName  name of the MethodSignature to construct
     *
     **/
    public MethodSignature( String mName ) 
    {
	name = mName;
	faults = new LinkedList();
    }


    /** 
     * Constructor for given MethodSignature name and fault list.
     *
     * @param  mName    name of the MethodSignature to construct
     * @param  mFaults  list of the new MethodSignature's faults
     *
     **/
    public MethodSignature( String mName, LinkedList mFaults ) 
    {
	name = mName;
	faults = mFaults;
    }


    /** 
     * Adds a fault (exception) to this MethodSignature
     *
     * @param  faultName  name of the fault to add
     *
     **/
    public void addFault( String faultName )
    {
	faults.add(faultName);
    }


    /** 
     * Generates a string representation of the method signature.
     *
     * @return  string representing this object
     *
     **/
    public String toString ()
    {
	String rep = name;

	for (ListIterator i = faults.listIterator() ; i.hasNext() ; ) {
	    rep += " " + i.next();
	}

	return rep;
    }


    /** 
     * Generates an HTML representation of the method signature.
     *
     * @return  HTML representing this object
     *
     **/
    public String toHtml ()
    {
	String rep = "<I>" + name + "</I>:";

	for (ListIterator i = faults.listIterator() ; i.hasNext() ; ) {
	    rep += " " + i.next();
	}

	return rep;
    }


    /** 
     * Obtains a MethodSignature object from its string-based
     * representation (extra whitespace on the ends is fine).
     *
     * @param  rep  string representing the desired object
     *
     **/
    static public MethodSignature fromString( String rep )
    {
	String namePlusFaults[] = rep.trim().split(" ", 0);
	MethodSignature mSig = new MethodSignature(namePlusFaults[0]);

	for ( int i=1 ; i < namePlusFaults.length ; i++ ) {
	    mSig.addFault(namePlusFaults[i].trim());
	}

	return mSig;
    }
}
