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
import java.util.Map;

import org.w3c.dom.Element;

import roc.pinpoint.tracing.TracingHelper;
import swig.util.XMLException;
import swig.util.XMLHelper;

/**
 * @author emrek
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ComponentType extends Node {

    	String name;
    	
    	ArrayList instances;

        private HashMap info;
    	
    	public ComponentType( Site site, Tier tier, String name ) {
    	    super(site, tier );
    	    this.name = name;
    	    instances = new ArrayList();
    	}
    
    	
        /**
         * @param eltype
         */
        public ComponentType(Site site, Tier tier, Element eltype) throws XMLException {
            super(site, tier);
            name = XMLHelper.GetChildText(eltype, "name");
            instances = new ArrayList();
        }


        /**
         * @return Returns the name.
         */
        public String getName() {
            return name;
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
        System.err.println("ComponentType Start");
        StringBuffer buffer = new StringBuffer();
        buffer.append("ComponentType[");
        buffer.append("super:");
        buffer.append( super.toString() );
        buffer.append(", name = ").append(name);
        //buffer.append(", instances = ").append(instances);
        buffer.append("]");
        System.err.println("ComponentType End");
        return buffer.toString();
    }

    public String idToString(){
        return "ComponentType[ name = "+ name +"]";
    }
    

    /**
     * @param out
     */
    public void toXML(PrintStream out) {
        out.println("<componenttype>");
        out.println("    <name>" + name + "</name>");
        out.println("</componenttype>");
    }

    private void buildInfo(){
        info = new HashMap();
        info.put("name",name);
        info.put("type",TracingHelper.TYPE_EJB);
    }
    
    /**
     * @return
     */
    public Map getInfo() {
        if(info == null)
            buildInfo();
        return info;
    }


    /**
     * @return
     */
    public ComponentInstance getRandomInstance() {
        int i = site.rand.nextInt(instances.size() );
        return (ComponentInstance)instances.get(i);
    }
    
}
