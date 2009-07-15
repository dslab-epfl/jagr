package roc.pinpoint.analysis.structure;

import java.io.Serializable;
import java.util.*;

public class WeightedSimpleComponentBehavior implements Serializable {

    String compname;
    
    Map inputs;
    Map outputs;
    Map undirected;
    
    Set summarizes;
    ArrayList el;

    double weight=1.0;

    public WeightedSimpleComponentBehavior( String compname ) {
        this.compname = compname;
        summarizes = new HashSet(0);
        inputs = new HashMap();
        outputs = new HashMap();
        undirected = new HashMap();
        el = new ArrayList();
    }

    public WeightedSimpleComponentBehavior( String compname, int size ) {
        this.compname = compname;
        summarizes = new HashSet(0);
        inputs = new HashMap();
        outputs = new HashMap();
        undirected = new HashMap(size);
        el = new ArrayList();
    }

    public LockedComponentBehavior lockValues() {
        return new LockedComponentBehavior( compname,
                                            inputs,
                                            outputs,
                                            undirected,
                                            weight, summarizes, el );
    }

    public String getComponentName() {
        return compname;
    }

    public void addElements( Collection el ) {
        Iterator iter = el.iterator();
        while( iter.hasNext() ) {
            Object e = iter.next();
            if( !( e instanceof LockedComponentBehavior )) {
                throw new RuntimeException( "el not a LCB! but a " + e.getClass() );
            }
        }

        this.el.addAll( el );
    } 

    public ArrayList getElements() {
        return el;
    }

    public void addSummarizes( Set summarizes ) {
        this.summarizes.addAll( summarizes );
    }

    public Set getSummarizes() {
        return summarizes;
    }

    public void addWeightToSink( String sink, double weight  ) {
        if( sink == null ) {
            throw new RuntimeException( "sink == null !!!" );
        }
        Double d = (Double)outputs.get( sink );
        if( d == null ) {
            outputs.put( sink, new Double( weight ));
        }
        else {
            outputs.put( sink, new Double( d.doubleValue() + weight ));
        }
    }

    public void addWeightToSrc( String src, double weight ) {
        if( src == null ) {
            throw new RuntimeException( "src == null !!!" );
        }
        Double d = (Double)inputs.get( src );
        if( d == null ) {
            inputs.put( src, new Double( weight ));
        }
        else {
            inputs.put( src, new Double( d.doubleValue() + weight ));
        } 
   }

    public void addWeightToUndirected( String other, double weight ) {
        if( other == null ) {
            throw new RuntimeException( "other == null !!!" );
        }
        Double d = (Double)undirected.get( other );
        if( d == null ) {
            undirected.put( other, new Double( weight ));
        }
        else {
            undirected.put( other, new Double( d.doubleValue() + weight ));
        }

    }

    public void addOverallWeight( double weight ) {
        this.weight += weight;
    }
    
    public String toString() {
	return "{WeightedSimpleComponentBehavior: component=" + compname + 
	    ", inputs=" + inputs.toString() + ", outputs=" + outputs.toString() + 
            ", undirected=" + undirected.toString() +  "}";
    }

}
