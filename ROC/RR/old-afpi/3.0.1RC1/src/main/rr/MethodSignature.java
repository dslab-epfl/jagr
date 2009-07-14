package rr;

import java.io.Serializable;
import java.util.LinkedList;

/** The signature of a method in a recursively rebootable component.
 *      
 *   @author  candea@cs.stanford.edu
 *   @version $Revision: 1.2 $
 */

public class MethodSignature implements Serializable 
{
    //---------------------------------------------------------------------------
    // FIELDS
    //---------------------------------------------------------------------------
    public String name;     // the name of this method
    public LinkedList faults; // the fault names we know it could generate

    //---------------------------------------------------------------------------
    // PUBLIC METHODS
    //---------------------------------------------------------------------------
    public MethodSignature (String mName) 
    {
	name = mName;
	faults = new LinkedList();
    }

    public MethodSignature( String mName, LinkedList mFaults ) 
    {
	name = mName;
	faults = mFaults;
    }

    public void addFault( String faultName )
    {
	faults.add(faultName);
    }

    public String toString()
    {
	return (name + "()  " + faults.toString());
    }
}
