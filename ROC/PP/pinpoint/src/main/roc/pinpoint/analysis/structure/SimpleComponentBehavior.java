package roc.pinpoint.analysis.structure;

import java.util.*;

public class SimpleComponentBehavior {

    String compname;
    
    Set inputs;
    Set outputs;
    Set undirected;

    public SimpleComponentBehavior( String compname ) {
        this.compname = compname;
        inputs = new HashSet();
        outputs = new HashSet();
        undirected = new HashSet();
    }

    public SimpleComponentBehavior( String compname, int size ) {
        this.compname = compname;
        inputs = new HashSet();
        outputs = new HashSet();
        undirected = new HashSet(size);
    }

    public LockedComponentBehavior lockValues() {
        return new LockedComponentBehavior( compname,
                                            inputs,
                                            outputs,
                                            undirected, null);
    }

    public String getComponentName() {
        return compname;
    }

    public void addLinkToSink( String sink ) {
        outputs.add(sink);
    }

    public void addLinkToSrc( String src ) {
        inputs.add(src);
    }

    public void addUndirectedLink( String other ) {
        undirected.add(other);
    }
    
    public String toString() {
	return "{SimpleComponentBehavior: component=" + compname + 
	    ", inputs=" + inputs.toString() + ", outputs=" + outputs.toString() + 
            ", undirected=" + undirected.toString() +  "}";
    }

}
