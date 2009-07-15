/*
 * Created on Apr 5, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package roc.pinpoint.analysis.plugins2.components;

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
import roc.pinpoint.analysis.structure.Component;
import roc.pinpoint.analysis.structure.ComponentBehavior;
import roc.pinpoint.analysis.structure.IdentifiableHelper;

/**
 * @author emrek
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class MergeCB implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_NAME_ARG = "input";
    public static final String OUTPUT_COLLECTION_NAME_ARG = "output";
    public static final String DEFINING_ATTRIBUTES_ARG = "definingAttributes";
    
    PluginArg[] args =
        {
            new PluginArg(
                    INPUT_COLLECTION_NAME_ARG,
                    "input collection. ",
                    PluginArg.ARG_RECORDCOLLECTION,
                    true,
                    null),
                    new PluginArg(
                            OUTPUT_COLLECTION_NAME_ARG,
                            "output collection. ",
                            PluginArg.ARG_RECORDCOLLECTION,
                            true,
                            null),
                            new PluginArg(
                                    DEFINING_ATTRIBUTES_ARG,
                                    "comma-separated component 'defining attributes'. the plugin uses these attributes to define logical components.",
                                    PluginArg.ARG_LIST,
                                    true,
                                    null)};

    RecordCollection inputRecordCollection;
    RecordCollection outputRecordCollection;
    List definingAttributes;
    
    /* (non-Javadoc)
     * @see roc.pinpoint.analysis.Plugin#getPluginArguments()
     */
    public PluginArg[] getPluginArguments() {
        return args;
    }

    /* (non-Javadoc)
     * @see roc.pinpoint.analysis.Plugin#start(java.lang.String, java.util.Map, roc.pinpoint.analysis.AnalysisEngine)
     */
    public void start(String id, Map args, AnalysisEngine engine)
        throws PluginException {
        inputRecordCollection =
            (RecordCollection) args.get(INPUT_COLLECTION_NAME_ARG);
        outputRecordCollection =
            (RecordCollection) args.get(OUTPUT_COLLECTION_NAME_ARG);
        definingAttributes = (List)args.get(DEFINING_ATTRIBUTES_ARG);
        
        inputRecordCollection.registerListener(this);
    }

    /* (non-Javadoc)
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() throws PluginException {
        inputRecordCollection.unregisterListener(this);
    }

    /* (non-Javadoc)
     * @see roc.pinpoint.analysis.RecordCollectionListener#addedRecords(java.lang.String, java.util.List)
     */
    public void addedRecord(String collectionName, Record rec) {
            ComponentBehavior cb = (ComponentBehavior)rec.getValue();
            Map id = IdentifiableHelper.ReduceMap(cb.getId(),definingAttributes);
            
            Record outputRec = outputRecordCollection.getRecord(id);
            if(outputRec == null) {
                outputRec = new Record();
                outputRec.setValue( new ComponentBehavior(new Component(id)));
            }

            ComponentBehavior outcb = (ComponentBehavior)outputRec.getValue();
            outcb.addComponentBehavior(cb);

            outputRecordCollection.setRecord(id,outputRec);

    }

    /* (non-Javadoc)
     * @see roc.pinpoint.analysis.RecordCollectionListener#removedRecords(java.lang.String, java.util.List)
     */
    public void removedRecords(String collectionName, List items) {
        // do nothing
    }

}
