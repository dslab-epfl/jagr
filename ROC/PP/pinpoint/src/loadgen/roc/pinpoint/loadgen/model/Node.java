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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.w3c.dom.Element;

import swig.util.Debug;
import swig.util.XMLException;
import swig.util.XMLHelper;

/**
 * @author emrek
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public abstract class Node {

    Site site;
    Tier tier;
    
    ArrayList inputLinks;
    ArrayList outputLinks;
    
    protected Node( Site site, Tier tier ) {
        this.site = site;
        this.tier = tier;
        inputLinks = new ArrayList();
        outputLinks = new ArrayList();
    }
    
    /**
     * @param link
     */
    void addOutputLink(Link link) {
        outputLinks.add(link);
    }

    /**
     * @param link
     */
    void addInputLink(Link link) {
        inputLinks.add(link);
    }

    public Node getRandomNextNode() {
        int i = site.rand.nextInt(outputLinks.size() +1 );
	if( i < outputLinks.size()) {
	    return ((Link)outputLinks.get(i)).sink;
	}
	else {
	    return null;
	}
    }

    
    /**
     * toString methode: creates a String representation of the object
     * @return the String representation
     * @author info.vancauwenberge.tostring plugin
    
     */
    public String toString() {
        System.err.println("Node Start");
        StringBuffer buffer = new StringBuffer();
        buffer.append("Node[");
        //buffer.append("site = ").append(site);
        buffer.append(", inputLinks = ").append(inputLinks);
        buffer.append(", outputLinks = ").append(outputLinks);
        buffer.append("]");
        System.err.println("Node End");
        return buffer.toString();
    }

   public abstract String idToString();
    
    
    /**
     * @param ps
     */
    public void IDToXML(PrintStream ps) {
        // it's bad form to break the class abstractions here...
        // but it's a lot easier than abstracting this functionality for such a small purpose
        ps.println("<tierid>" + tier.getId() + "</tierid>");
        if( this instanceof Machine ) {
            ps.println("<type>machine</type>");
            ps.println("<id>"+((Machine)this).getId() + "</id>");
        }
        else if( this instanceof ComponentType ) {
            ps.println("<type>comptype</type>");
            ps.println("<name>"+((ComponentType)this).getName() + "</name>");
        }
        else if( this instanceof ComponentInstance ) {
            ps.println("<type>compinst</type>");
            ps.println("<id>"+((ComponentInstance)this).getId() + "</id>");
        }
        else {
            Debug.AssertNotReached();
        }
        
    }
    
    public void LinksToXML(PrintStream ps ){
        Iterator iter = outputLinks.iterator();
        while (iter.hasNext()) {
            Link link = (Link) iter.next();
            link.toXML(ps);
        }
    }

    /**
     * @param srcel
     * @param s
     * @return
     */
    public static Node GetNodeFromXML(Element el, Site s) throws XMLException {
        String tierid = XMLHelper.GetChildText(el,"tierid");
        String type = XMLHelper.GetChildText(el,"type");
        
        Tier tier = s.getTier(tierid);
        Node ret = null;
        if(type.equals("machine")) {
            String id = XMLHelper.GetChildText(el,"id");
            ret = tier.getMachine(id);
        }
        else if(type.equals("comptype")) {
            String name = XMLHelper.GetChildText(el,"name");
            ret = tier.getComponentType(name);
        }
        else if(type.equals("compinst")) {
            String id = XMLHelper.GetChildText(el,"id");
            ret = tier.getComponentInstance(id);
        }
        else {
            Debug.AssertNotReached();
        }
        
        return ret;
    }
    
    
    
}
