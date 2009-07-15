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

import org.w3c.dom.Element;

import swig.util.XMLException;
import swig.util.XMLHelper;

/**
 * @author emrek
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Link {

    Node src;
    Node sink;
   
    Link( Node src, Node sink ) {
        this.src = src;
        this.sink = sink;
        src.addOutputLink(this);
        sink.addInputLink(this);
    }
    
    Link( Element linkel, Site s ) throws XMLException {
        Element srcel = XMLHelper.GetChildElement(linkel, "src");
        Element sinkel = XMLHelper.GetChildElement(linkel, "sink");
        this.src = Node.GetNodeFromXML(srcel,s);
        this.sink = Node.GetNodeFromXML(sinkel,s);
	src.addOutputLink(this);
	sink.addInputLink(this);
    }
    
    
    /**
     * toString methode: creates a String representation of the object
     * @return the String representation
     * @author info.vancauwenberge.tostring plugin
    
     */
    public String toString() {
        System.err.println("Link Start");
        StringBuffer buffer = new StringBuffer();
        buffer.append("Link[");
        buffer.append("src = ").append(src.idToString());
        buffer.append(", sink = ").append(sink.idToString());
        buffer.append("]");
        System.err.println("Link End");
        return buffer.toString();
    }
    
    public void toXML( PrintStream ps ) {
        ps.println("<link>");
        ps.println("<src>");
        src.IDToXML(ps);
        ps.println("</src>");
        ps.println("<sink>");
        sink.IDToXML(ps);
        ps.println("</sink>");
        ps.println("</link>");
    }
}
