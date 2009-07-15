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

import roc.pinpoint.tracing.Observation;
import roc.pinpoint.tracing.RequestInfo;
import swig.util.XMLException;
import swig.util.XMLHelper;

/**
 * @author emrek
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ComponentInstance extends Node {

    String id;
    ComponentType type;
    Machine machine;
    Tier tier;
    Map originInfo;
    private HashMap attributesbegin;
    private HashMap attributesend;
    

    /**
     * @param ct
     * @param machine2
     */
    public ComponentInstance( Site site, Tier tier, String id, ComponentType type, Machine machine) {
        super(site,tier);
        this.id = id;
        this.type = type;
        type.register(this);
        this.machine = machine;
        machine.register(this);
    }
    
    
    /**
     * @param site
     * @param elinstance
     */
    public ComponentInstance(Site site, Tier tier, Element elinstance) throws XMLException {
        super(site, tier);
        this.id = XMLHelper.GetChildText(elinstance, "id");
        String typename = XMLHelper.GetChildText(elinstance, "typename");
        String machineid = XMLHelper.GetChildText(elinstance, "machineid");
        type = tier.getComponentType(typename);
        machine = tier.getMachine(machineid);
        type.register(this);
        machine.register(this);
    }


    /**
     * toString methode: creates a String representation of the object
     * @return the String representation
     * @author info.vancauwenberge.tostring plugin
    
     */
    public String toString() {
        System.err.println("ComponentInstance Start");
        StringBuffer buffer = new StringBuffer();
        buffer.append("ComponentInstance[");
        buffer.append("super:");
        buffer.append( super.toString() );
        buffer.append(", type = ").append(type.idToString());
        buffer.append(", machine = ").append(machine.idToString());
        buffer.append("]");
        System.err.println("ComponentInstance End");
        return buffer.toString();
    }

    
    public String idToString(){
        return "ComponentInstance[ id = "+id +"]";
    }
    

    /**
     * @param out
     */
    public void toXML(PrintStream out) {
        out.println("<componentinstance>");
        out.println("    <id>" +id +"</id>");
        out.println("    <machineid>"+machine.id +"</machineid>");
        out.println("    <typename>"+type.name+"</typename>");
        out.println("</componentinstance>");
    }



    /**
     * @return Returns the id.
     */
    public String getId() {
        return id;
    }

    public void buildOriginInfo() {
        Map machineInfo = machine.getInfo();
        Map typeInfo = type.getInfo();
        originInfo = new HashMap(machineInfo.size()+typeInfo.size());
        originInfo.putAll(machineInfo);
        originInfo.putAll(typeInfo);

        attributesbegin = new HashMap();
        attributesbegin.put("observationlocation","roc.pinpoint.loadgen.model.ComponentInstance");
        attributesbegin.put("stage","METHODCALLBEGIN");
        attributesend = new HashMap();
        attributesend.put("observationlocation","roc.pinpoint.loadgen.model.ComponentInstance");
        attributesend.put("stage","METHODCALLEND");
        
    }

    /**
     * @param ret
     */
    public void generateRequestPath(RequestInfo ri, ArrayList ret) {
        if(originInfo == null ) {
            buildOriginInfo();
        }
        Observation obs = 
            new Observation( Observation.EVENT_COMPONENT_USE,
                    ri,
                    originInfo, null, attributesbegin );
        
        ret.add(obs);
	ri.incrementSeqNum();
        
        // TODO:fix numtocall to be some decent distribution...
	//        int numtocall = (int)Math.floor( Math.min( 1, Math.log(site.rand.nextInt(128))));
	int numtocall = 1+site.rand.nextInt(5);

        for( int i=0; i<numtocall; i++ ) {
            ComponentType nextType = (ComponentType)type.getRandomNextNode();
            if(nextType != null ) {
                ComponentInstance nextInstance = nextType.getRandomInstance();
                nextInstance.generateRequestPath(ri,ret);
            }
        }

        obs = new Observation( Observation.EVENT_COMPONENT_USE,
                ri,
                originInfo, null, attributesend );
        
        ret.add(obs);
	ri.incrementSeqNum();        

        return;
    }

}
