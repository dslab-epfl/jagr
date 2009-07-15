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
 * Created on Mar 26, 2004
 * 
 * To change the template for this generated file go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
package roc.pinpoint.analysis.plugins2.paths;

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
import roc.pinpoint.analysis.structure.Path;

/**
 * @author emrek
 * 
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SplitDiagnosisPaths implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_ARG = "input";
    public static final String BAD_OUTPUT_COLLECTION_ARG = "badpaths";
    public static final String GOOD_OUTPUT_COLLECTION_ARG = "goodpaths";

    public static final String SENSITIVITY_ARG = "sensitivity";

    PluginArg[] args =
        {
            new PluginArg(
                INPUT_COLLECTION_ARG,
                "input collection.  this plugin compares the paths in the input collection against a PCFG",
                PluginArg.ARG_RECORDCOLLECTION,
                true,
                null),
            new PluginArg(
                BAD_OUTPUT_COLLECTION_ARG,
                "bad output collection. this plugin will place anomalous paths into this record collection",
                PluginArg.ARG_RECORDCOLLECTION,
                true,
                null),
            new PluginArg(
                GOOD_OUTPUT_COLLECTION_ARG,
                "bad output collection. this plugin will place anomalous paths into this record collection",
                PluginArg.ARG_RECORDCOLLECTION,
                true,
                null)};

    RecordCollection inputCollection;
    RecordCollection badOutputCollection;
    RecordCollection goodOutputCollection;

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
        inputCollection = (RecordCollection) args.get(INPUT_COLLECTION_ARG);
        badOutputCollection = (RecordCollection) args.get(BAD_OUTPUT_COLLECTION_ARG);
        goodOutputCollection = (RecordCollection) args.get(GOOD_OUTPUT_COLLECTION_ARG);

        inputCollection.registerListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() throws PluginException {
        inputCollection.unregisterListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see roc.pinpoint.analysis.RecordCollectionListener#addedRecords(java.lang.String,
     *      java.util.List)
     */
    public void addedRecord(String collectionName, Record rec) {
        Path path = (Path)rec.getValue();
        if(path.hasErrors()) {
            badOutputCollection.setRecord(rec.getAttribute("key"), new Record(rec));
        }
        else {
            goodOutputCollection.setRecord(rec.getAttribute("key"), new Record(rec));
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
