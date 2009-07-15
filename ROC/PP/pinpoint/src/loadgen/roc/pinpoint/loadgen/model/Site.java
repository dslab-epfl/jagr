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
/*
 * Created on Feb 10, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package roc.pinpoint.loadgen.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import roc.pinpoint.tracing.RequestInfo;
import swig.util.XMLException;
import swig.util.XMLHelper;

/**
 * @author emrek
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Site {

    ArrayList tiers;
    Map tiersbyID;
    
    Random rand;
    
    Site(){
        // only for use by SiteTemplate
    }
    
    public Site( File file ) throws XMLException, FileNotFoundException, IOException {
        this(XMLHelper.GetDocumentElement( new InputSource(new FileInputStream(file))));
    }
    
    public Site( Reader r ) throws XMLException, IOException {
        this(XMLHelper.GetDocumentElement( new InputSource(r)));
    }

    public Site( Element siteel ) throws XMLException {
        NodeList nl = XMLHelper.GetChildrenByTagName(siteel, "tier");
        tiers = new ArrayList(nl.getLength());
        tiersbyID = new HashMap( nl.getLength() );
        for(int i=0; i<nl.getLength(); i++ ) {
            Element el = (Element)nl.item(i);
            Tier tier = new Tier(this,el);
            tiers.add( tier );
            tiersbyID.put( tier.getId(), tier );
        }
	nl = XMLHelper.GetChildrenByTagName(siteel, "link");
	for( int i=0; i<nl.getLength(); i++ ) {
	    Element el = (Element)nl.item(i);
	    Link l = new Link( el, this );
	}
	rand = new Random();
    }

    public void setRandom( Random rand ) {
	this.rand = rand;
    }
    
    public void toXML( PrintStream out ) {
        out.println( "<site>");
        
        Iterator iter = tiers.iterator();
        while ( iter.hasNext()) {
            Tier tier = (Tier) iter.next();
            tier.toXML( out );
        }        
        
        iter = tiers.iterator();
        while(iter.hasNext()) {
            Tier tier = (Tier)iter.next();
            tier.linksToXML( out );
        }
        out.println( "</site>" );
    }
    
    public Tier getTier( int i ) {
        return (Tier)tiers.get(i);
    }
    /**
     * toString methode: creates a String representation of the object
     * @return the String representation
     * @author info.vancauwenberge.tostring plugin
    
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Site[");
        buffer.append("tiers = ").append(tiers);
        buffer.append(", rand = ").append(rand);
        buffer.append("]");
        return buffer.toString();
    }

    /**
     * @param tierid
     * @return
     */
    public Tier getTier(String tierid) {
        return (Tier)tiersbyID.get(tierid);
    }
    
    
    
    public ArrayList getRandomRequestPath() {
        ArrayList ret = new ArrayList();
        
        RequestInfo requestinfo = new RequestInfo(Long.toString(rand.nextLong()));
	requestinfo.incrementSeqNum(); // set it to 0
        
        ComponentInstance ci = ((Tier)tiers.get(0)).getRandomComponentInstance();
        ci.generateRequestPath(requestinfo, ret);
        
        return ret;
    }
    
    public static void main(String[] argv ) {
        String filename = argv[0];
        
        try {
            Site site = new Site(new File(filename));
            System.out.println("Successfully loaded Site from " +filename );
            System.out.println(site.toString());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
