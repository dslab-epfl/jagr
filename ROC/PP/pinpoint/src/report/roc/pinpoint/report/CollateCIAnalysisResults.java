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
package roc.pinpoint.report;

import java.util.*;
import java.io.*;

public class CollateCIAnalysisResults {

    static String faulty_ipaddress="169.229.50.210";

    static String[] components = {
	"Bid",
	"Category",
	"Item",
	"Query",
	"Region",
	"User"
    };

    /**
       petstore 1.1.2
    static String[] components = {
	"TheCatalog",
	"TheInventory",
	"TheOrder",
	"TheSignOn",
	"TheCustomer",
	"TheAccount",
	"TheCart",
	"TheProfileMgr",
	"TheShoppingClientController"
    };
    **/

    /** petstore 1.3.1
    static String[] components = {
	"AccountEJB",
	"AddressEJB",
	"AsyncSenderEJB",
	"CatalogEJB",
	"class_com.sun.j2ee.blueprints.waf.controller.web.MainServlet",
	"class_com.sun.j2ee.blueprints.waf.view.template.TemplateServlet",
	"class_org.apache.jasper.servlet.JspServlet",
	"class_org.mortbay.jetty.servlet.Default",
	"ContactInfoEJB",
	"CounterEJB",
	"CreditCardEJB",
	"CustomerEJB",
	"LineItemEJB",
	"MailOrderApprovalMDB",
	"ManagerEJB",
	"OrderApprovalMDB",
	"OrderFulfillmentFacadeEJB",
	"ProcessManagerEJB",
	"ProfileEJB",
	"PurchaseOrderEJB",
	"PurchaseOrderMDB",
	"ShoppingCartEJB",
	"ShoppingClientFacadeEJB",
	"ShoppingControllerEJB",
	"SignOnEJB",
	"SupplierOrderEJB",
	"SupplierOrderMDB",
	"UniqueIDGeneratorEJB"
    };

    **/
    static List lComponents = Arrays.asList( components );

    public static void main( String argv[] ) {

	try {

	    if( argv.length != 2 ) {
		System.err.println( "Usage: java roc.pinpoint.report.CollateCIAnalysisResults [injecteddir] [analysisdir]" );
		return;
	    }	    
	    
	    String injecteddirname = argv[0];
	    String analysisdirname = argv[1];

	    File injecteddir = new File( injecteddirname );
	    File analysisdir = new File( analysisdirname );

	    File[] injectedfiles = injecteddir.listFiles();
	    File[] analysisfiles = analysisdir.listFiles();
	     
	    Map injectedMap = new HashMap();
	    Map analysisMap = new HashMap();

	    for( int i=0; i<injectedfiles.length; i++ ) {
		File f = injectedfiles[i];
		String fname = f.getName();
		if( fname.endsWith( ".injected" ) ) {
		    fname = fname.substring( 0, 
					     fname.length() - ".injected".length() );
		    injectedMap.put( fname, injectedfiles[i] );
		}
		else {
		    System.err.println( "\tignoring file " + fname );
		}
	    }

	    for( int i=0; i<analysisfiles.length; i++ ) {
		File f = analysisfiles[i];
		String fname = f.getName();
		if( fname.endsWith( ".log.analysis" ) ) {
		    fname = fname.substring( 0, 
					     fname.length() - ".log.analysis".length() );
		    analysisMap.put( fname, analysisfiles[i] );
		}
		else {
		    System.err.println( "\tignoring file " + fname );
		}
	    }

	    Iterator iter = analysisMap.keySet().iterator();
	    while( iter.hasNext() ) {
		String f = (String)iter.next();

		int idx = f.indexOf( "_" );
		int idx2 = f.indexOf( "-", idx+1 );

		String location, faulttype;
		
		if( idx < 0 ) {
		    System.err.println( "ACK: fname = " + f );
		}
		
		if( idx2 > 0 ) {
		    //System.err.println( "f=" + f + "; idx=" + idx + "; idx2=" + idx2 );
		    location = f.substring( idx+1, idx2 );
		    faulttype = f.substring( idx2+1 );
		}
		else {
		    location = f.substring( idx+1 );
		    faulttype = "none";
		}

		location = swig.util.StringHelper.ReplaceAll( location, "_", " " );
		
		
		File injectedfile = (File)injectedMap.get( f );
		Set injectedrequests = loadInjectedIds( injectedfile );
		if( f.indexOf( "nofault" ) != -1 && injectedrequests.size() < 10 ) {
		    System.err.println( "Skipping " + f + ": too few fault injections" );
		    continue;
		}

		File analysisfile = (File)analysisMap.get( f );

		SortedSet ciresults = loadCIResults( analysisfile );
		ComponentInfo ci = null;
		Iterator ciiter = ciresults.iterator();
		boolean detected = false;
		int rank = -1;
		while( ciiter.hasNext() ) {
		    ComponentInfo t = (ComponentInfo)ciiter.next();
		    System.err.println( "\t" + location + " ?= " + t.name );
		    if( t.name.equals( location ) 
			/*&& t.ipaddress.equals( faulty_ipaddress )*/ ) {
			ci = t;
			detected = true;
		    }
		}
		if( detected ) {
		    rank = ciresults.headSet(ci).size();
		}
		int fp = ciresults.size() - (detected?1:0);


		System.out.println( location + "," + faulttype + "," +
				    (detected?"1":"0") + "," +
				    rank + "," + fp );
	    }
	    
	    

	    
	    
	    
   
	}
	catch( Exception e ) {
	    e.printStackTrace();
	}

    }

   public static final Set loadInjectedIds( File f ) throws IOException {
       Set ret = new HashSet();

	LineNumberReader lnr =
	    new LineNumberReader( new FileReader( f ));

	while( true ) {
	    String line = lnr.readLine();
	    if( line == null )
		break;
	    ret.add( line );
	    //System.err.println( "badrequestid: " + line );
	}

	return ret;
    }


    public static SortedSet loadCIResults( File f ) throws IOException {
	LineNumberReader lnr = new LineNumberReader( new FileReader( f ));

	SortedSet ret = new TreeSet();

	while( true ) {

	    String line = lnr.readLine();
	    if( line == null )
		break;

	    if( line.startsWith( "timestamp" )) {
		// new output -> clear the treeset
		ret.clear();
	    }
	    else if( line.startsWith( "RECORDCOLLECTION" )) {
		// ignore
	    }
	    else if( line.startsWith( "{Rank = " )) {
		int idx1 = "{Rank = ".length();
		int idx2 = line.indexOf( "; id={", idx1 );
		
		int idx3 = line.indexOf( "ipaddress=" ) + "ipaddress=".length();
		int idx4 = line.indexOf( ",", idx3 );

		int idx5 = line.indexOf( "name=" ) + "name=".length();
		int idx6 = line.length() - "}}".length();
		
		String rank = line.substring( idx1, idx2 );
		String ipaddress = line.substring( idx3,idx4);
		String name = line.substring( idx5, idx6 );

		double r = Double.parseDouble( rank );
		if( r >= 1.0 && lComponents.contains( name )) {
		    ret.add( new ComponentInfo( name, r, ipaddress ));
		}
	    }
	}

	lnr.close();

	return ret;
    }

	
    

}

class ComponentInfo implements Comparable {

    String name;
    double rank;

    String ipaddress;

    ComponentInfo( String name, double r, String ipaddress ) {
	this.name = name;
	this.rank = r;
	this.ipaddress = ipaddress;
    }
    
    public int compareTo( Object o ) {
	ComponentInfo other = (ComponentInfo)o;
	if( rank < other.rank ) 
	    return 1;
	else if( rank > other.rank )
	    return -1;
	else {
	    return other.name.compareTo( name );
	}
    }
}
