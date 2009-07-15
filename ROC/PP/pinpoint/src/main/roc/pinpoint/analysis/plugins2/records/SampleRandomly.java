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
package roc.pinpoint.analysis.plugins2.records;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import roc.pinpoint.analysis.AnalysisEngine;
import roc.pinpoint.analysis.Plugin;
import roc.pinpoint.analysis.PluginArg;
import roc.pinpoint.analysis.PluginException;
import roc.pinpoint.analysis.Record;
import roc.pinpoint.analysis.RecordCollection;
import roc.pinpoint.analysis.RecordCollectionListener;

/**
 * @author emrek
 * 
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SampleRandomly implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_NAME_ARG = "input";
    public static final String OUTPUT_COLLECTION_NAME_ARG = "output";
    public static final String SAMPLE_FREQUENCY_ARG = "frequency";
    public static final String RANDOM_SEED_ARG = "randomseed";
    
    PluginArg[] args =
        {
            new PluginArg(
                INPUT_COLLECTION_NAME_ARG,
                "input collection.  this plugin will sample records that get placed in the collection specified by this argument.",
                PluginArg.ARG_RECORDCOLLECTION,
                true,
                null),
            new PluginArg(
                OUTPUT_COLLECTION_NAME_ARG,
                "output collection.  this plugin will place sampled records into the collection specified by this argument.",
                PluginArg.ARG_RECORDCOLLECTION,
                true,
                null),
            new PluginArg(
                SAMPLE_FREQUENCY_ARG,
                "the proportion of records to sample, between 0.0 and 1.0",
                PluginArg.ARG_DOUBLE,
                true,
                null),
            new PluginArg(
                RANDOM_SEED_ARG,
                "seed for random number generator",
                PluginArg.ARG_STRING,
                false,
                null)
    };

    RecordCollection inputRecordCollection;
    RecordCollection outputRecordCollection;
    double frequency;
    Random rand;
    
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
        outputRecordCollection =
            (RecordCollection) args.get(OUTPUT_COLLECTION_NAME_ARG);
        frequency =
            ((Double)args.get(SAMPLE_FREQUENCY_ARG)).doubleValue();
        String randomseed = (String)args.get(RANDOM_SEED_ARG);
        if(randomseed == null )
            rand = new Random();
        else
            rand = new Random(Long.parseLong(randomseed));
        
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

        double sample = rand.nextDouble();
        if(sample<frequency) {
            outputRecordCollection.setRecord(rec.getAttribute("key"),new Record(rec));
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
