package roc.pinpoint.analysis.structure;

import java.util.List;
import java.util.SortedSet;

public interface Deviants {

    public SortedSet getDeviants();

    public void setAcceptableDeviation( double dev );

    public boolean isDeviant( Object o ) throws ClassCastException;

    public double getDeviation( Object o ) throws ClassCastException;

}
