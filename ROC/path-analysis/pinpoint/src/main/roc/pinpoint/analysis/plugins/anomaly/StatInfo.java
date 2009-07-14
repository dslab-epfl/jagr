package roc.pinpoint.analysis.plugins.anomaly;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author emrek
 *
 */
public class StatInfo {

    public Map values; // indexed by (String)id, values are Collections of Numbers

    public MyStat majorStat;
    public Map minorStats;

    public MyStat countStat; // mean/stddev, etc of count of items in each set.
    public MyStat meanStat; // mean/stddev, etc of mean of items in each set.
    public MyStat stddevStat; // mean/stddev, etc of stddev of items in each set.

    public boolean statsAreValid = false;

    StatInfo() {
	values = new HashMap();
	majorStat = new MyStat();
	minorStats = new HashMap();
	countStat = new MyStat();
	meanStat = new MyStat();
	stddevStat = new MyStat();
    }

    void clearValues( Object id ) {
	values.remove( id );
	statsAreValid = false;
    }

    void addValues( Object id, Collection ns ) {
	Collection s = (Collection)values.get( id );
	if( s == null ) {
	    s = new LinkedList();
	    values.put( id, s );
	}
	s.addAll( ns );
	statsAreValid = false;
    }

    void addValue( Object id, Number n ) {
	Collection s = (Collection)values.get( id );
	if( s == null ) {
	    s = new LinkedList();
	    values.put( id, s );
	}
	s.add( n );

	statsAreValid = false;
    }


    void addStatInfo( StatInfo si ) {
	Iterator iter = si.values.keySet().iterator();
	while( iter.hasNext() ) {
	    Object id = iter.next();
	    Collection ns = (Collection)si.values.get( id );
	    addValues( id, ns );
	}
    }

    void calculateCountAndMean() {

        majorStat.count = 0;
        majorStat.mean = 0;

        Iterator iter = values.keySet().iterator();
        while (iter.hasNext()) {
            Object id = iter.next();
            Collection s = (Collection) values.get(id);

            MyStat minorStat = new MyStat();
	    minorStat.id = id;
            minorStats.put(id, minorStat);

            Iterator subIter = s.iterator();
            while (subIter.hasNext()) {
                Number n = (Number) subIter.next();
                minorStat.count++;
                minorStat.mean += n.doubleValue();
            }

            majorStat.count += minorStat.count;
            majorStat.mean += minorStat.mean;

            minorStat.mean = minorStat.mean / minorStat.count;
        }

        majorStat.mean = majorStat.mean / majorStat.count;
    }

    void calculateStdDev() {

        double majorSumSquaredDeviation = 0;

        Iterator iter = values.keySet().iterator();
        while (iter.hasNext()) {
            Object id = iter.next();
            Collection s = (Collection) values.get(id);

            MyStat minorStat = (MyStat) minorStats.get(id);

            double minorSumSquaredDeviation = 0;

            Iterator subIter = s.iterator();
            while (subIter.hasNext()) {
                Number n = (Number) subIter.next();

                minorSumSquaredDeviation
                    += Math.pow(n.doubleValue() - minorStat.mean, 2);
                majorSumSquaredDeviation
                    += Math.pow(n.doubleValue() - majorStat.mean, 2);
            }

            minorStat.stddev =
                Math.sqrt(minorSumSquaredDeviation / minorStat.count);
        }

        majorStat.stddev =
            Math.sqrt(majorSumSquaredDeviation / majorStat.count);
    }

    void calculateCountAndMeanStatistics() {

        countStat.count = minorStats.size();
        countStat.mean = 0;
        meanStat.count = minorStats.size();
        meanStat.mean = 0;
        stddevStat.count = minorStats.size();
        stddevStat.mean = 0;

        Iterator iter = minorStats.keySet().iterator();
        while (iter.hasNext()) {
            Object id = iter.next();
            MyStat minorStat = (MyStat) minorStats.get(id);
            countStat.mean += minorStat.count;
            meanStat.mean += minorStat.mean;
            stddevStat.mean += minorStat.stddev;
        }

        countStat.mean = countStat.mean / countStat.count;
        meanStat.mean = meanStat.mean / meanStat.count;
        stddevStat.mean = stddevStat.mean / stddevStat.count;

        double countSumSquaredDeviation = 0;
        double meanSumSquaredDeviation = 0;
        double stddevSumSquaredDeviation = 0;

        iter = minorStats.keySet().iterator();
        while (iter.hasNext()) {
            Object id = iter.next();
            MyStat minorStat = (MyStat) minorStats.get(id);
            countSumSquaredDeviation
                += Math.pow(minorStat.count - countStat.mean, 2);
            meanSumSquaredDeviation
                += Math.pow(minorStat.mean - meanStat.mean, 2);
            stddevSumSquaredDeviation
                += Math.pow(minorStat.stddev - stddevStat.mean, 2);
        }

        countStat.stddev =
            Math.sqrt(countSumSquaredDeviation / countStat.count);
        meanStat.stddev = Math.sqrt(meanSumSquaredDeviation / meanStat.count);
        stddevStat.stddev =
            Math.sqrt(stddevSumSquaredDeviation / stddevStat.count);

        iter = minorStats.keySet().iterator();
        while (iter.hasNext()) {
            Object id = iter.next();
            MyStat minorStat = (MyStat)minorStats.get( id );

	    if( countStat.stddev == 0 )
		minorStat.zscore_count = 0;
	    else
		minorStat.zscore_count =
		    (minorStat.count - countStat.mean) / countStat.stddev;

	    if( meanStat.stddev == 0 )
		minorStat.zscore_mean = 0;
	    else
		minorStat.zscore_mean =
		    (minorStat.mean - meanStat.mean) / meanStat.stddev;

	    if( stddevStat.stddev == 0 )
		minorStat.zscore_stddev = 0;
	    else
		minorStat.zscore_stddev = 
		    (minorStat.stddev - stddevStat.mean ) / stddevStat.stddev;
            
            minorStat.sumSquareZScores = 
                minorStat.zscore_count * minorStat.zscore_count
                + minorStat.zscore_mean * minorStat.zscore_mean 
                + minorStat.zscore_stddev * minorStat.zscore_stddev;
        }
    }

    public void updateStats() {
        if (statsAreValid) {
            return;
        }

        calculateCountAndMean();
        // todo later, also calculate mode and median? to see distribution skew?
        calculateStdDev();
        calculateCountAndMeanStatistics();
        // todo later, calculate zscores ?

        statsAreValid = true;
    }
    
    SortedSet getSortedMinorStats() {
        updateStats();
        return new TreeSet( minorStats.values()  );
    }

    public class MyStat implements Comparable {

	Object id;

        public double count;
        public double mean;
        public double stddev;

        public double zscore_count;
        // the z-score of this count, in relation to all counts
        public double zscore_mean;
        // the z-score of this mean, in relation to all means
        public double zscore_stddev;
        // the z-score of this stddev, in relation to all stddevs

        public double sumSquareZScores;

	MyStat() {
	    count = 0;
	    mean = 0;
	    stddev = 0;
	    zscore_count = 0;

	}

	public String toString() {
	    StringBuffer ret = new StringBuffer();
	    ret.append( "StatInfo.MyStat: [\n" );
	    ret.append( "\tid = " );
	    ret.append( ( id == null ) ? "null" : id.toString() );
	    ret.append( "\n\tSum Squared Z Scores = " + sumSquareZScores );
	    ret.append( "\n\tZ Score_count = " + zscore_count );
	    ret.append( "\n\tZ Score_mean = " + zscore_mean );
	    ret.append( "\n\tZ Score_stddev = " + zscore_stddev );
	    ret.append( "\n\tcount = " + count );
	    ret.append( "\n\tmean = " + mean );
	    ret.append( "\n\tstddev = " + stddev );
	    ret.append( "\n]" );
	    return ret.toString();
	}


        /**
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(Object o) {
            if (!(o instanceof MyStat))
                throw new ClassCastException(
                    "cannot compare MyStat to '" + o.getClass().toString());

            MyStat ms = (MyStat)o;
            
            if( this.sumSquareZScores < ms.sumSquareZScores) 
                return -1;
            
            if( this.sumSquareZScores == ms.sumSquareZScores )
                return 0;
                
            return -1;
        }

    }

    public String toString() {
	StringBuffer ret = new StringBuffer();
	
	updateStats();

	ret.append( "StatInfo: [\n" );
	ret.append( "\tmajorStat = " + 
		    (( majorStat == null ) ? "null" : majorStat.toString()) );
	ret.append( "\n\tminorStats = " + 
		    (( minorStats == null ) ? "null" : minorStats.toString()));
	ret.append( "\n\tcountStat = " + 
		    (( countStat == null ) ? "null" : countStat.toString()) );
	ret.append( "\n\tmeanStat = " + 
		    (( meanStat == null ) ? "null" : meanStat.toString() ));
	ret.append( "\n\tstddevStat = " + 
		    (( stddevStat == null ) ? "null" : stddevStat.toString()));
	ret.append( "\n\tnumMinorStats = " + 
		    minorStats.size() );
	ret.append( "\n]" );

	return ret.toString();
    }

}
