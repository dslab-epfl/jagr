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

public class ASCIIToCumulativeLocalization {

    public static String FAULTFILEPREFIX = "faultconfig-name_";


    // ignore these components because they're not rebootable... see
    // what's correlated that is rebootable. Some of the items on
    //  this list are .jsp files, some are DB tables, and some are
    //  just from poorly parsed SQL queries and don't really exist (like
    //  "into" and "(product"
    //  Q: remove .jsp files from this list?
    public static String[] noncomponents = {
	"/petstore/enter_order_information.screen",
	"/petstore/CatalogDAOSQL.xml",
	"(category",
	"/signon.jsp",
	"userejb",
	"/petstore/search.screen",
	"/main.jsp",
	"/enter_order_information.jsp",
	"/signoff.jsp",
	"/petstore/item.screen",
	"/footer.jsp",
	"/petstore/signoff.do",
	"/petstore/signon_welcome.screen",
	"/search.jsp",
	"/banner.jsp",
	"/create_customer.jsp",
	"/template.jsp",
	"accountejb",
	"(product",
	"/cart.jsp",
	"customerejb",
	"/petstore/category.screen",
	"/petstore/product.screen",
	"/sidebar.jsp",
	"profileejb",
	"/item.jsp",
	"creditcardejb",
	"/petstore/main.screen",
	"/mylist.jsp",
	"addressejb",
	"(((item",
	"/category.jsp",
	"/product.jsp",
	"into",
	"/advice_banner.jsp",
	"contactinfoejb",
	"/petstore/order.do"
    };

    

    public static void main( String argv[] ) {
	
	try {

	    double[] largestAnomaly = new double[argv.length];

	    int[] numfalsepositives = new int[ 10 ];

	for( int i=0; i<argv.length; i++ ) {
	    String inputfile = argv[i];

	    int idx = inputfile.indexOf( "-", FAULTFILEPREFIX.length() );
	    String fault = inputfile.substring( FAULTFILEPREFIX.length(), idx ); 

	    LineNumberReader lnr =
		new LineNumberReader( new FileReader( inputfile ));

	
	    boolean done = false;

	    largestAnomaly[i] = 1.0;
    

	    while( !done ) {
		String l = lnr.readLine();
		if( l == null ) {
		    done = true;
		    continue;
		}

		idx = l.indexOf( " " );
		String rank = l.substring( 0, idx );
		double r = Double.parseDouble( rank );

		String comp = l.substring( idx+1 );
		idx = comp.indexOf( "name=" );
		int idx2 = comp.indexOf( "}", idx );
		String name = comp.substring( idx+"name=".length(),
					      idx2 );

		if( name.equals( fault )) {
		    System.err.println( "Correctly recognized fault " + name + " at rank = " + r );
		}

		if( r == 100.0 ) {
		    // ignore
		    continue;
		}

		r = 1 - r;

		if( r < largestAnomaly[i] ) {
		    largestAnomaly[i] = r;
		}

		//Suspect s = new Suspect( r, name, name.equals( fault ));

		

	    }



	}


	System.out.println( "0,0" );

	Arrays.sort( largestAnomaly );
	for( int i=0; i<largestAnomaly.length; i++ ) {
	    
	    System.out.println( (i+1) + "," + largestAnomaly[i] );
	    
	}	    
	    


	}
	catch( Exception e ) {

	    e.printStackTrace();
	}


    }


    class Suspect implements Comparable {
	double rank;
	String name;
	boolean isTruePositive;

	Suspect( double rank, String name, boolean t ) {
	    this.rank = rank;
	    this.name = name;
	    this.isTruePositive = t;
	}

	public int compareTo( Object o ) {
	    Suspect other = (Suspect)o;

	    if( this.rank < other.rank ) 
		return -1;
	    else if( this.rank == other.rank )
		return this.name.compareTo( other.name );
	    else 
		return 1;
	}

    }


}
