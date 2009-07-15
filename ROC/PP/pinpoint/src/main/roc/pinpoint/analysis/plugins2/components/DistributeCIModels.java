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
 * Created on Mar 24, 2004
 * 
 * To change the template for this generated file go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
package roc.pinpoint.analysis.plugins2.components;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import roc.pinpoint.analysis.AnalysisEngine;
import roc.pinpoint.analysis.Plugin;
import roc.pinpoint.analysis.PluginArg;
import roc.pinpoint.analysis.PluginException;
import roc.pinpoint.analysis.Record;
import roc.pinpoint.analysis.RecordCollection;
import roc.pinpoint.analysis.structure.IdentifiableHelper;

/**
 * @author emrek
 * 
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DistributeCIModels implements Plugin {

    public static final String INPUT_COLLECTION_NAME_ARG = "input";
    public static final String HOST_LIST_ARG = "hosts";
    public static final String PORT_ARG = "port";
    public static final String PERIOD_ARG = "period";
    public static final String DEFINING_ATTRIBUTES_ARG = "definingAttributes";
    
    PluginArg[] args =
        {
            new PluginArg(
                INPUT_COLLECTION_NAME_ARG,
                "input collection.  this plugin will sample records that get placed in the collection specified by this argument.",
                PluginArg.ARG_RECORDCOLLECTION,
                true,
                null),
            new PluginArg(
                HOST_LIST_ARG,
                "comma separated list of hosts to distribute observations among",
                PluginArg.ARG_LIST,
                true,
                null),
            new PluginArg(
                DEFINING_ATTRIBUTES_ARG,
                "comma-separated component 'defining attributes'. the plugin uses these attributes to define logical components.",
                PluginArg.ARG_LIST,
                true,
                null),
            new PluginArg(
                PORT_ARG,
                "port number to connect to on host machines",
                PluginArg.ARG_INTEGER,
                false,
                "17011"),
            new PluginArg(
                PERIOD_ARG,
                "how often to distribute component models",
                PluginArg.ARG_INTEGER,
                false,
                "10000")};

    /*
     * (non-Javadoc)
     * 
     * @see roc.pinpoint.analysis.Plugin#getPluginArguments()
     */
    public PluginArg[] getPluginArguments() {
        return args;
    }

    RecordCollection inputRecordCollection;
    List hosts;
    int port;
    int period;
    ArrayList outputstreams;
    Timer timer;
    List definingAttributes;
    
    /*
     * (non-Javadoc)
     * 
     * @see roc.pinpoint.analysis.Plugin#start(java.lang.String,
     *      java.util.Map, roc.pinpoint.analysis.AnalysisEngine)
     */
    public void start(String id, Map args, AnalysisEngine engine)
        throws PluginException {
        inputRecordCollection =
            (RecordCollection) args.get(INPUT_COLLECTION_NAME_ARG);
        hosts = (List) args.get(HOST_LIST_ARG);
        port = ((Integer) args.get(PORT_ARG)).intValue();
        period = ((Integer) args.get(PERIOD_ARG)).intValue();
        definingAttributes = (List)args.get(DEFINING_ATTRIBUTES_ARG);
        
        try {
            outputstreams = new ArrayList(hosts.size());
            Iterator iter = hosts.iterator();
            while (iter.hasNext()) {
                String hostname = (String) iter.next();
                outputstreams.add(initializeOutputStream(hostname, port));
            }
        }
        catch (IOException e) {
            throw new PluginException(
                "I/O error while initializing connection...",
                e);
        }

        timer = new Timer(true);
        timer.schedule(new MyWorker(),10000,10000);
    }

    /*
     * (non-Javadoc)
     * 
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() throws PluginException {
        timer.cancel();
    }

    private ObjectOutputStream initializeOutputStream(
        String hostname,
        int port)
        throws IOException {
        Socket s = new Socket(hostname, port);
        OutputStream os = s.getOutputStream();
        return new ObjectOutputStream(os);
    }

    class MyWorker extends TimerTask {

        /*
         * (non-Javadoc)
         * 
         * @see java.util.TimerTask#run()
         */
        public void run() {
	    //System.err.println( "DistributeCIModels.TimerTask" );
            synchronized(inputRecordCollection) {
                Map records = inputRecordCollection.getAllRecords();
                Iterator iter = records.values().iterator();
                while (iter.hasNext()) {
                    Record rec = (Record) iter.next();
                    Map componentbehaviors = (Map) rec.getValue();

                    Iterator keyIter = componentbehaviors.keySet().iterator();
                    while (keyIter.hasNext()) {
                        Map key = (Map) keyIter.next();
                        String keystr = IdentifiableHelper.MapToString(key,definingAttributes);
                        int idx = keystr.hashCode() % outputstreams.size();
                        try {
                            ObjectOutputStream oos =
                                (ObjectOutputStream) outputstreams.get(idx);
                            oos.writeObject(
                                (Record) componentbehaviors.get(key));
                        }
                        catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }

            }
        }

    }
}
