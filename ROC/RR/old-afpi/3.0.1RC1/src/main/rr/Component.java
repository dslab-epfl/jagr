package rr;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Enumeration;

/** A recursively rebootable component.
 *      
 *   @author  candea@cs.stanford.edu
 *   @version $Revision: 1.4 $
 */

public class Component implements Serializable 
{
    //---------------------------------------------------------------------------
    // FIELDS
    //---------------------------------------------------------------------------
    public String name;        // name of the component
    public String type;        // free-form type of the component
    public String persistence; // type of state persistence used by this component
    public Hashtable methods; // this component's methods
    public rr.FaultInjectionInterceptor injector;

    //---------------------------------------------------------------------------
    // PUBLIC METHODS
    //---------------------------------------------------------------------------
    public Component( String cName, rr.FaultInjectionInterceptor cInj ) 
    {
	injector = cInj;
	name = cName; // name of the component
	methods = new Hashtable(); // no methods yet, just an empty hashtable
    }

    public void setType( String cType )
    {
	type = cType;
    }

    public void setPersistence( String cPersistence )
    {
	persistence = cPersistence;
    }

    public void addMethod( rr.MethodSignature mSig )
    {
	methods.put(mSig.name, (Object) mSig);
    }

    public String toString()
    {
	String ret = "<h3>" + name + "</h3>" +
	             "(" + type + ", " + 
	             persistence + " persistence)<br><ul>";

	for (Enumeration e = methods.elements() ; e.hasMoreElements() ; ) {
	    ret += "<li>" + e.nextElement().toString();
	}
	ret += "</ul>";
	return ret;
    }

}



