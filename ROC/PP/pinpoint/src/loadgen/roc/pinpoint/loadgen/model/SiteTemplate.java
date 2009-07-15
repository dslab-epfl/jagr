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
 * To change the template for this generated file go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
package roc.pinpoint.loadgen.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import swig.util.ArgHelper;
import swig.util.ArgOption;

/**
 * @author emrek
 * 
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SiteTemplate {

    int numTiers;
    Range componentsPerTier;
    Range machinesPerTier;
    Range connectedness;

    public SiteTemplate(
        int numTiers,
        Range componentsPerTier,
        Range machinesPerTier,
        Range connectedness) {
        this.numTiers = numTiers;
        this.componentsPerTier = componentsPerTier;
        this.machinesPerTier = machinesPerTier;
        this.connectedness = connectedness;
    }

    public Site generateSite(Random rand) {
        Site ret = new Site();

        ret.rand = rand;

        ret.tiers = new ArrayList(numTiers);
	ret.tiersbyID = new HashMap( numTiers);
        for (int i = 0; i < numTiers; i++) {
            Tier tier = generateTier(ret, i);
            ret.tiers.add( tier );
            ret.tiersbyID.put(tier.getId(), tier );
        }

        for (int i = 0; i < numTiers - 1; i++) {
            Tier srcTier = ret.getTier(i);
            Tier sinkTier = ret.getTier(i + 1);

            Iterator ctIter = srcTier.getComponentTypeIterator();
            while (ctIter.hasNext()) {
                ComponentType srcCt = (ComponentType) ctIter.next();

                int numLinks =
                    (int) connectedness.getValue(
                        (double) i / (double) numTiers);
                for (int j = 0; j < numLinks; j++) {
                    ComponentType sinkCt = sinkTier.getRandomComponentType();
                    Link l = new Link(srcCt, sinkCt);
                }

            }
        }

        return ret;
    }

    private Tier generateTier(Site site, int tiernum) {
        Tier ret = new Tier(site, "Tier#" + tiernum);

        double depth = ((double) tiernum / (double) numTiers);

        int numcomponenttypes = (int) componentsPerTier.getValue(depth);
        ret.componenttypes = new HashMap(numcomponenttypes);
        ret.componenttypeslist = new ArrayList(numcomponenttypes);
        for (int i = 0; i < numcomponenttypes; i++) {
            ComponentType ct =
                new ComponentType(site, ret, "Tier#" + tiernum + ":Component#" + i);
            ret.componenttypes.put(ct.getName(), ct);
            ret.componenttypeslist.add(ct);
        }

        int nummachines = (int) machinesPerTier.getValue(depth);
        ret.machines = new HashMap(nummachines);
        ret.componentinstances = new HashMap(nummachines);
        ret.componentinstanceslist = new ArrayList(nummachines);
        
        Iterator ctIter = null;
        for (int i = 0; i < nummachines; i++) {
            Machine machine =
                new Machine(site, ret, "Tier#" + tiernum + ":Machine#" + i);
            ret.machines.put(machine.getId(), machine);

            // assume each machine runs one component...

            if (ctIter == null || ctIter.hasNext() == false) {
                ctIter = ret.getComponentTypeIterator();
            }
            ComponentType ct = (ComponentType) ctIter.next();
            ComponentInstance ci =
                new ComponentInstance(
                    site,
                    ret,
                    "Tier#" + tiernum + ":instance#" + i,
                    ct,
                    machine);
            ret.componentinstances.put(ci.getId(), ci);
            ret.componentinstanceslist.add(ci);
        }

        return ret;
    }

    /**
     * toString methode: creates a String representation of the
     * object
     * 
     * @return the String representation
     * @author info.vancauwenberge.tostring plugin
     *  
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("SiteTemplate[");
        buffer.append("numTiers = ").append(numTiers);
        buffer.append(", componentsPerTier = ").append(componentsPerTier);
        buffer.append(", machinesPerTier = ").append(machinesPerTier);
        buffer.append(", connectedness = ").append(connectedness);
        buffer.append("]");
        return buffer.toString();
    }

    static List options =
        java.util.Arrays.asList(
            new ArgOption[] {
                new ArgOption(
                    "numtiers",
                    "t",
                    "number of tiers in model",
                    false,
                    true,
                    null,
                    true),
                new ArgOption(
                    "numcomponentsmin",
                    "cn",
                    "min number of components in each tier of the model",
                    false,
                    true,
                    null,
                    true),
                new ArgOption(
                    "numcomponentsmax",
                    "cx",
                    "max number of components in each tier of the model",
                    false,
                    true,
                    null,
                    true),
                new ArgOption(
                    "nummachinesmin",
                    "mn",
                    "min number of machines in each tier of the model",
                    false,
                    true,
                    null,
                    true),
                new ArgOption(
                    "nummachinesmax",
                    "mx",
                    "max number of machines in each tier of the model",
                    false,
                    true,
                    null,
                    true),
                new ArgOption(
                    "connectednessmin",
                    "ln",
                    "min number of output links from components",
                    false,
                    true,
                    null,
                    true),
                new ArgOption(
                    "connectednessmax",
                    "lx",
                    "max number of output links from components",
                    false,
                    true,
                    null,
                    true),
                new ArgOption(
                    "filename",
                    "f",
                    "output file for xml model",
                    false,
                    true,
                    null,
                    false),
                new ArgOption(
                    "randomseed",
                    "r",
                    "random seed number",
                    true,
                    false,
                    "-1",
                    false),
                });

    public static void main(String[] argv) {

        Map args = null;
        try {
            args = ArgHelper.ParseArgs(argv, options, false);

            if (!args.containsKey("numtiers")
                || !args.containsKey("numcomponentsmin")
                || !args.containsKey("numcomponentsmax")
                || !args.containsKey("nummachinesmin")
                || !args.containsKey("nummachinesmax")
                || !args.containsKey("connectednessmin")
                || !args.containsKey("connectednessmax")
                || !args.containsKey("randomseed")
                || !args.containsKey("filename")) {
                System.err.println("required argument is missing");
                throw new RuntimeException("required arguments missing");
            }
        }
        catch (Exception e) {
            System.err.println("Usage Info");

            System.err.println(
                ArgHelper.UsageInfo(
                    "SiteTemplate",
                    "Site-Model Generator for Pinpoint load generator",
                    options,
                    false));
            return;
        }

        int numtiers = ((Integer) args.get("numtiers")).intValue();
        int mincomponentspertier =
            ((Integer) args.get("numcomponentsmin")).intValue();
        int maxcomponentspertier =
            ((Integer) args.get("numcomponentsmax")).intValue();
        int minmachinespertier =
            ((Integer) args.get("nummachinesmin")).intValue();
        int maxmachinespertier =
            ((Integer) args.get("nummachinesmax")).intValue();
        int minconnectedness =
            ((Integer) args.get("connectednessmin")).intValue();
        int maxconnectedness =
            ((Integer) args.get("connectednessmax")).intValue();
        long rand = Long.parseLong((String) args.get("randomseed"));
        String filename = (String) args.get("filename");

        SiteTemplate sitetemplate =
            new SiteTemplate(
                numtiers,
                new Range(mincomponentspertier, maxcomponentspertier),
                new Range(minmachinespertier, maxmachinespertier),
                new Range(minconnectedness, maxconnectedness));

        Site site = sitetemplate.generateSite(new Random(rand));

        try {
            FileOutputStream os = new FileOutputStream(new File(filename));
            PrintStream ps = new PrintStream(os);

	    ps.println("<?xml version=\"1.0\"?>");
	    ps.println("<!-- GENERATED BY roc.pinpoint.loadgen.model.SiteTemplate\n\n" );
	    ps.println( sitetemplate.toString() );
	    ps.println("-->\n\n" );

            site.toXML(ps);
            ps.close();
            os.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }
}
