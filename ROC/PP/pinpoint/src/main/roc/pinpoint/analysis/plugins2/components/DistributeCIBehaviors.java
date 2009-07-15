/*
 * Created on Apr 4, 2004
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

import roc.pinpoint.analysis.AnalysisEngine;
import roc.pinpoint.analysis.Plugin;
import roc.pinpoint.analysis.PluginArg;
import roc.pinpoint.analysis.PluginException;
import roc.pinpoint.analysis.Record;
import roc.pinpoint.analysis.RecordCollection;
import roc.pinpoint.analysis.RecordCollectionListener;
import roc.pinpoint.analysis.structure.ComponentBehavior;
import roc.pinpoint.analysis.structure.IdentifiableHelper;

/*********************************************************************
 * 
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DistributeCIBehaviors
    implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_NAME_ARG = "input";
    public static final String HOST_LIST_ARG = "hosts";
    public static final String PORT_ARG = "port";
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
                "17015")};

    RecordCollection inputRecordCollection;
    List hosts;
    int port;
    int period;
    ArrayList outputstreams;
    List definingAttributes;
    
    /*
     * (non-Javadoc)
     * 
     * @see roc.pinpoint.analysis.Plugin#getPluginArguments()
     */
    public PluginArg[] getPluginArguments() {
        return args;
    }

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

        inputRecordCollection.registerListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() throws PluginException {
        inputRecordCollection.unregisterListener(this);
    }

    private ObjectOutputStream initializeOutputStream(
        String hostname,
        int port)
        throws IOException {
        Socket s = new Socket(hostname, port);
        OutputStream os = s.getOutputStream();
        return new ObjectOutputStream(os);
    }

    /*
     * (non-Javadoc)
     * 
     * @see roc.pinpoint.analysis.RecordCollectionListener#addedRecords(java.lang.String,
     *      java.util.List)
     */
    public void addedRecord(String collectionName, Record rec) {
            ComponentBehavior cb = (ComponentBehavior) rec.getValue();
            String keystr = IdentifiableHelper.MapToString(cb.getId(),definingAttributes);
            int idx = keystr.hashCode() % outputstreams.size();
            try {
                ObjectOutputStream oos =
                    (ObjectOutputStream) outputstreams.get(idx);
                oos.writeObject(rec);
            }
            catch (IOException ex) {
                ex.printStackTrace();
            }

    }

    /*
     * (non-Javadoc)
     * 
     * @see roc.pinpoint.analysis.RecordCollectionListener#removedRecords(java.lang.String,
     *      java.util.List)
     */
    public void removedRecords(String collectionName, List items) {
        // do nothing
    }

}
