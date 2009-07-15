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
 * Created on Mar 23, 2004
 * 
 * To change the template for this generated file go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
package roc.pinpoint.analysis.plugins2.observations;

import java.lang.reflect.InvocationTargetException;
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
import roc.pinpoint.tracing.Observation;
import roc.pinpoint.tracing.ObservationException;
import roc.pinpoint.tracing.ObservationPublisher;

/**
 * @author emrek
 * 
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DistributeByRequestID
    implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_NAME_ARG = "input";
    public static final String HOST_LIST_ARG = "hosts";
    public static final String PORT_ARG = "port";

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
                PORT_ARG,
                "port number to connect to on host machines",
                PluginArg.ARG_INTEGER,
                false,
                "17000")};

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
    Class publisherClass;
    ArrayList publishers;
    int port;

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

        publishers = new ArrayList(hosts.size());
        Iterator iter = hosts.iterator();
        while (iter.hasNext()) {
            String host = (String) iter.next();
            publishers.add(createPublisher(host, port));
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

    /*
     * (non-Javadoc)
     * 
     * @see roc.pinpoint.analysis.RecordCollectionListener#addedRecords(java.lang.String,
     *      java.util.List)
     */
    public void addedRecord(String collectionName, Record rec) {
            try {
                Observation obs = (Observation) rec.getValue();
                String requestid = obs.requestId;
                if (requestid == null) {
                    sendToAllPublishers(obs);
                }
                else {
                    sendToOnePublisher(requestid, obs);
                }
            }
            catch (ObservationException ignore) {
                ignore.printStackTrace();
            }

    }

    private void sendToAllPublishers(Observation obs)
        throws ObservationException {
        Iterator iter = publishers.iterator();
        while (iter.hasNext()) {
            ObservationPublisher publisher = (ObservationPublisher) iter.next();
            publisher.send(obs);
        }
    }

    private void sendToOnePublisher(String requestid, Observation obs)
        throws ObservationException {
        int hash = requestid.hashCode() % publishers.size();
        ObservationPublisher publisher =
            (ObservationPublisher) publishers.get(hash);
        publisher.send(obs);
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

    private void InitPublisherClass(String classname) throws PluginException {

        try {
            Class sInterface =
                Class.forName("roc.pinpoint.tracing.ObservationPublisher");
            Class sClass = Class.forName(classname);

            if (!(sInterface.isAssignableFrom(sClass))) {
                System.err.println(classname + " not a publisher!");
            }
            else {
                publisherClass = sClass;
            }
        }
        catch (ClassNotFoundException cnfe) {
            throw new PluginException(
                "Class " + classname + " not found",
                cnfe);
        }
    }

    private ObservationPublisher createPublisher(String hostname, int port)
        throws PluginException {
        try {
            Class[] argtypes = null;
            argtypes =
                new Class[] { java.lang.String.class, java.lang.Integer.TYPE };

            Object[] initargs = new Object[2];
            initargs[0] = hostname;
            initargs[1] = new Integer(port);
            ObservationPublisher ret =
                (ObservationPublisher) publisherClass.getConstructor(
                    argtypes).newInstance(
                    initargs);
            return ret;
        }
        catch (NoSuchMethodException nsm) {
            throw new PluginException(
                "could not find appropriate constructor for class :"
                    + publisherClass.getName(),
                nsm);
        }
        catch (InvocationTargetException ite) {
            throw new PluginException(
                "problem creating observationpublisher:"
                    + publisherClass.getName(),
                ite);
        }
        catch (IllegalAccessException iae) {
            throw new PluginException(
                "Illegal Access: " + publisherClass.getName(),
                iae);
        }
        catch (InstantiationException ie) {
            throw new PluginException(
                "Could not instantiate subscriber " + publisherClass.getName(),
                ie);
        }
    }

}
