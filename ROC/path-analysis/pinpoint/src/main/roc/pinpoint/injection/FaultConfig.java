package roc.pinpoint.injection;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import swig.util.StringHelper;
import swig.util.XMLException;
import swig.util.XMLHelper;


public class FaultConfig {

    String name;
    Set faultTriggers; // set of roc.pinpoint.injection.FaultTrigger;

    public FaultConfig() {
	this("Untitled");
    }

    public FaultConfig( String name ) {
	this.name = name;
	faultTriggers = new HashSet();
    }

    public static FaultConfig ParseFaultConfig( File configDataFile ) 
	throws XMLException, IOException {
	FileInputStream fis = new FileInputStream( configDataFile );
	String configData = StringHelper.loadString( fis );

	return ParseFaultConfig( configData );
    }

    public static FaultConfig ParseFaultConfig( String configData )
	throws XMLException {
	try {
	    return ParseFaultConfig( XMLHelper.GetASCIIDocumentElement( configData ));
	}
	catch( IOException e ) {
	    // This should not happen!
	    e.printStackTrace();
	    throw new RuntimeException( "Assert Failure", e );
	}
    }

    public static FaultConfig ParseFaultConfig( Element configData ) 
	throws XMLException, IOException {
	FaultConfig ret = new FaultConfig();

	ret.name = XMLHelper.GetChildText( configData, "name" );

	NodeList nl = XMLHelper.GetChildrenByTagName( configData, 
						      "faultTrigger" );
	for( int i=0; i<nl.getLength(); i++ ) {
	    Element eFt = (Element)nl.item( i );
	    FaultTrigger ft = FaultTrigger.ParseFaultTrigger( eFt );
	    ret.addFaultTrigger( ft );
	    System.err.println( "faultconfig: read faulttrigger" );
	}

	return ret;
    }


    public synchronized int checkFaultTriggers( Map currComponent ) {

	Iterator iter = faultTriggers.iterator();
	while( iter.hasNext() ) {
	    FaultTrigger ft = (FaultTrigger)iter.next();
	    if( ft.matches( currComponent )) {
		return ft.faultType;
	    }
	}
	return FaultTrigger.FT_NOFAULT;
    }


    public synchronized void addFaultTrigger( FaultTrigger ft ) {
	faultTriggers.add( ft );
    }

    public synchronized void RemoveAllFaultTriggers() {
        faultTriggers.clear();
    }

}
