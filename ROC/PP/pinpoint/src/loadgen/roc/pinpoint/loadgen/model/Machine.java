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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Element;

import swig.util.XMLException;
import swig.util.XMLHelper;

/**
 * @author emrek
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Machine extends Node {

    String id;
    Set instances;
    private HashMap info;
    
    /**
     * @param string
     */
    public Machine(Site site, Tier tier, String id) {
        super(site, tier );
        this.id = id;
        instances = new HashSet();
    }

    /**
     * @param site
     * @param elmachine
     */
    public Machine(Site site, Tier tier, Element elmachine) throws XMLException {
        super(site, tier );
        this.id = XMLHelper.GetChildText(elmachine,"id");
        instances = new HashSet();
    }

    /**
     * @return Returns the id.
     */
    public String getId() {
        return id;
    }

    /**
     * @param instance
     */
    public void register(ComponentInstance instance) {
        instances.add(instance);
    }

    /**
     * toString methode: creates a String representation of the object
     * @return the String representation
     * @author info.vancauwenberge.tostring plugin
    
     */
    public String toString() {
        System.err.println("Machine Start");
        StringBuffer buffer = new StringBuffer();
        buffer.append("Machine[");
        buffer.append("super:");
        buffer.append( super.toString() );
        buffer.append(", id = ").append(id);
        //buffer.append(", instances = ").append(instances);
        buffer.append("]");
        System.err.println("Machine End");
        return buffer.toString();
    }

    public String idToString(){
        return "Machine[ id = "+id +"]";
    }
    
    /**
     * @param out
     */
    public void toXML(PrintStream out) {
        out.println("<machine>");
        out.println("    <id>" +id + "</id>" );
        out.println("</machine>" );
    }

    private void buildInfo(){
        info = new HashMap();
        info.put("hostname",id);
    }
    
    /**
     * @return
     */
    public Map getInfo() {
        if( info == null )
            buildInfo();
        return info;
    }
}
