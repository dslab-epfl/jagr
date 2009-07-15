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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import swig.util.XMLException;
import swig.util.XMLHelper;

/**
 * @author emrek
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Tier {

    Site site;
    
    String id;
    
    Map machines;
    Map componenttypes;
    ArrayList componenttypeslist;
    Map componentinstances;
    ArrayList componentinstanceslist;
    
    /**
     * @param string
     */
    Tier(Site site, String id) {
        this.site = site;
        this.id = id;
    }
    /**
     * @param el
     */
    public Tier(Site site, Element el) throws XMLException {
        this.site = site;
        id = XMLHelper.GetChildText( el, "id" );
        
        NodeList typelist = XMLHelper.GetChildrenByTagName(el,"componenttype");
        componenttypes = new HashMap(typelist.getLength());
        componenttypeslist = new ArrayList( typelist.getLength());
        for( int i=0; i<typelist.getLength(); i++ ) {
            Element eltype = (Element) typelist.item(i);
            ComponentType ct = new ComponentType(site, this, eltype);
            componenttypes.put(ct.getName(), ct );
            componenttypeslist.add(ct);
        }
        typelist=null;
        
        NodeList machinelist = XMLHelper.GetChildrenByTagName(el,"machine");
        machines = new HashMap(machinelist.getLength());
        for( int i=0; i<machinelist.getLength(); i++ ) {
            Element elmachine = (Element) machinelist.item(i);
            Machine m = new Machine(site, this, elmachine);
            machines.put( m.getId(), m );
        }
        
        NodeList instancelist = XMLHelper.GetChildrenByTagName(el,"componentinstance");
        componentinstances = new HashMap(instancelist.getLength());
        componentinstanceslist = new ArrayList(instancelist.getLength());
        for( int i=0; i<instancelist.getLength(); i++ ) {
            Element elinstance = (Element) instancelist.item(i);
            ComponentInstance ci = new ComponentInstance(site, this, elinstance);
            componentinstances.put( ci.getId(), ci );
            componentinstanceslist.add( ci );
        }
        
        
    }
    /**
     * @return Returns the id.
     */
    public String getId() {
        return id;
    }

    public ComponentInstance getRandomComponentInstance() {
        int i = site.rand.nextInt(componentinstances.size() );
        return (ComponentInstance)componentinstanceslist.get(i);
    }
    
    
    public ComponentType getRandomComponentType() {
        int i = site.rand.nextInt(componenttypes.size() );
        return (ComponentType)componenttypeslist.get(i);
    }
    
    public Iterator getComponentTypeIterator() {
        return componenttypeslist.iterator();
    }
    
    /**
     * toString methode: creates a String representation of the object
     * @return the String representation
     * @author info.vancauwenberge.tostring plugin
    
     */
    public String toString() {
        System.err.println("Tier Start");
        StringBuffer buffer = new StringBuffer();
        buffer.append("Tier[");
        //buffer.append("site = ").append(site);
        buffer.append(", id = ").append(id);
        buffer.append(", machines = ").append(machines);
        buffer.append(", componenttypes = ").append(componenttypes);
        buffer.append(", componentinstances = ").append(componentinstances);
        buffer.append("]");
        System.err.println("Tier End");
        return buffer.toString();
    }
    /**
     * @param out
     */
    public void toXML(PrintStream out) {
        out.println("<tier>");
        out.println("    <id>" + id + "</id>");

        Iterator iter = machines.values().iterator();
        while (iter.hasNext()) {
            Machine machine = (Machine) iter.next();
            machine.toXML(out);
        }

        iter = componenttypeslist.iterator();
        while (iter.hasNext()) {
            ComponentType ct = (ComponentType) iter.next();
            ct.toXML(out);
        }
        
        iter = componentinstances.values().iterator();
        while (iter.hasNext()) {
            ComponentInstance ci = (ComponentInstance) iter.next();
            ci.toXML(out);
        }
        
        out.println("</tier>");
    }
    /**
     * @param typename
     * @return
     */
    public ComponentType getComponentType(String typename) {
        return (ComponentType)componenttypes.get(typename);
    }
    /**
     * @param machineid
     * @return
     */
    public Machine getMachine(String machineid) {
        return (Machine)machines.get(machineid);
    }

    public ComponentInstance getComponentInstance(String id2) {
        return (ComponentInstance)componentinstances.get(id2);
    }

    public void linksToXML(PrintStream out) {
        Iterator iter = machines.values().iterator();
        while (iter.hasNext()) {
            Machine machine = (Machine) iter.next();
            machine.LinksToXML(out);
        }
        iter = componentinstances.values().iterator();
        while (iter.hasNext()) {
            ComponentInstance ci = (ComponentInstance) iter.next();
            ci.LinksToXML(out);
        }
        iter = componenttypes.values().iterator();
        while (iter.hasNext()) {
            ComponentType ct = (ComponentType) iter.next();
            ct.LinksToXML(out);
        }
    }

    
}
