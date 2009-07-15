package roc.pinpoint.analysis.plugins2.components;

import java.util.*;
import roc.pinpoint.analysis.*;
import roc.pinpoint.analysis.structure.*;

public class CountUsage implements Plugin, RecordCollectionListener {

    public static final String INPUT_COLLECTION_NAME_ARG = "inputCollection";
    public static final String OUTPUT_COLLECTION_NAME_ARG = "outputCollection";
    public static final String ONLINE_ARG = "online";

        PluginArg[] args = {
	new PluginArg( INPUT_COLLECTION_NAME_ARG,
		       "input collection.  this plugin will look for requesttraces in the record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null ),
	new PluginArg( OUTPUT_COLLECTION_NAME_ARG,
		       "output collection. this plugin will place the generated links into the record collection specified by this argument",
		       PluginArg.ARG_RECORDCOLLECTION,
		       true,
		       null )
    };

    private RecordCollection inputCollection;
    private RecordCollection outputCollection;

    private AnalysisEngine engine;

    public PluginArg[] getPluginArguments() {
	return args;
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#start(String, Map, AnalysisEngine)
     */
    public void start(String id, Map args, AnalysisEngine engine) {
        this.engine = engine;

        inputCollection = (RecordCollection)
	    args.get(INPUT_COLLECTION_NAME_ARG);
        outputCollection = (RecordCollection)
	    args.get(OUTPUT_COLLECTION_NAME_ARG);

        inputCollection.registerListener(this);
    }

    /**
     * 
     * @see roc.pinpoint.analysis.Plugin#stop()
     */
    public void stop() {
        inputCollection.unregisterListener(this);
    }

    /**
     * 
     * @see roc.pinpoint.analysis.RecordCollectionListener#addedRecords(String,
     * List)
     */
    public void addedRecord(String collectionName, Record rec) {
        GrossComponentBehavior gcb = (GrossComponentBehavior)rec.getValue();
        
        // for each gcb, want to find out:
        //  a) how often component is used
        //  b) how many distinct parents the component has
        //  c) how many distinct children the component has

        int count=0;
        HashSet parents = new HashSet();
        HashSet children = new HashSet();

        Iterator iter = gcb.getComponentBehaviorIterator();
        while( iter.hasNext() ) {
            ComponentBehavior cb = (ComponentBehavior)iter.next();
         
            Component c = cb.getComponent();
            Set links = cb.getLinks();
            Iterator liter = links.iterator();
            while( liter.hasNext() ) {
                Link l = (Link)liter.next();
                if( l.getSource().equals( c )) {
                    // this is an out-going link
                    children.add( l.getSink() );
                }
                else if( l.getSink().equals( c )) {
                    // this is an in-coming link
                    parents.add( l.getSource() );
                    count += l.getCount();
                }
                else {
                    throw new RuntimeException( "something horribly wrong/confusing occurred!" );
                }
            }
        }

        String description = 
            "{Component = " + gcb.getId() + ";\n\t" +
            " usage count = " + count + ";\n\t" +
            " parent count = " + parents.size() + ";\n\t" +
            " child count = " + children.size() + ";\n" +
            "}";
            
        outputCollection.setRecord( gcb.getId(), new Record( description ));
    }

    public void removedRecords( String collectionName, List items ) {
        // do nothing
    }
    
}
