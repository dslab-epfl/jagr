package roc.pinpoint.analysis.structure;

import java.util.*;
import roc.pinpoint.util.StringAtoms;
import java.io.*;

public class LockedComponentBehavior implements Distanceable, Externalizable {

    static final long serialVersionUID = 6891068039448746545L;

    String compname;

    DoubleEntry[] inputs;
    DoubleEntry[] outputs;
    DoubleEntry[] undirected;

    Set summarizes;
    ArrayList el;

    double weight=1.0;

    public static StringAtoms atoms = new StringAtoms();

    public static void SanityCheckAtom( int atom ) {
	if( atoms.getLabel( atom ) == null )
	    throw new RuntimeException("ILLEGAL ATOM: " + atom );
    }

    public LockedComponentBehavior( String compname, 
                                    Set inputs, Set outputs,
                                    Set undirected, ArrayList el ) {
        this.compname = compname;

        this.summarizes = new HashSet(1);
        this.summarizes.add( compname );
        this.inputs = atomizeSet( inputs );
        this.outputs = atomizeSet( outputs );
        this.undirected = atomizeSet( undirected );
        if( el != null ) {
            this.el = new ArrayList( el.size() );
            this.el.addAll( el );
        }
        else {
            this.el = new ArrayList( 1 );
            this.el.add( this );
        }
    }

    public LockedComponentBehavior() {
    }

    public LockedComponentBehavior( String compname, 
                                    Map inputs, Map outputs,
                                    Map undirected, double weight,
                                    Set summarizes, ArrayList el ) {
        this.compname = compname;

        this.summarizes = new HashSet(1);
        this.summarizes.add( compname );
        this.inputs = atomizeMap( inputs );
        this.outputs = atomizeMap( outputs );
        this.undirected = atomizeMap( undirected );
        this.weight = weight;
        if( (summarizes == null) || (summarizes.size() == 0 )) {
            this.summarizes = new HashSet(1);
            this.summarizes.add( compname );
        }
        else {
            this.summarizes = new HashSet( summarizes );
        }
        if( el != null ) {
            this.el = new ArrayList( el.size() );
            this.el.addAll( el );
        }
        else {
            this.el = new ArrayList(1);
            this.el.add( this );
        }

    }

    public LockedComponentBehavior( Object[] elements ) {
        this.compname = "Summary Component";

        
        HashMap inputSet = new HashMap();
        HashMap outputSet = new HashMap();
        HashMap undirectedSet = new HashMap();        
        summarizes = new HashSet();

        this.el = new ArrayList( elements.length );
        
        this.weight = 0;
	for( int i=0; i<elements.length; i++ ) {
            LockedComponentBehavior lcb = (LockedComponentBehavior)elements[i];
	    this.weight += lcb.weight;
	}

        for( int i=0; i<elements.length; i++ ) {
            LockedComponentBehavior lcb = (LockedComponentBehavior)elements[i];
            addDoubleEntries( inputSet, lcb.inputs, lcb.weight / this.weight );
            addDoubleEntries( outputSet, lcb.outputs, lcb.weight / this.weight );
            addDoubleEntries( undirectedSet, lcb.undirected, lcb.weight / this.weight );
            this.summarizes.addAll( lcb.summarizes );
            el.add( lcb );
        }

        inputs = asArray( inputSet );
        outputs = asArray( outputSet );
        undirected = asArray( undirectedSet );

	/*
        normalize( inputs, this.weight );
        normalize( outputs, this.weight );
        normalize( undirected, this.weight );
	*/
    }

    private DoubleEntry[] asArray( HashMap set ) {
        DoubleEntry[] de = new DoubleEntry[ set.values().size() ];
        Iterator iter = set.values().iterator();
        int i=0;
        while( iter.hasNext( )) {
            de[i++] = (DoubleEntry)iter.next();
        }
        Arrays.sort( de, doubleEntryComparator );
        sanityCheckSorting( de );

        return de;
    }

    private void normalize( DoubleEntry[] de, double normalizer ) {
        for( int i=0; i<de.length; i++ ) {
            de[i].weight /= normalizer;
        }
    }

    private void addDoubleEntries( HashMap set, DoubleEntry[] dearr, 
                                   double multiplier ) {
        int i;
        DoubleEntry de;
        Integer key;
        DoubleEntry inSet;

        for( i=0; i<dearr.length; i++ ) {
            de = dearr[i];

            key = new Integer( de.atom );
            inSet = (DoubleEntry)set.get( key );

            if( inSet == null ) {
                inSet = new DoubleEntry( de, multiplier );
                set.put( key, inSet );
            }
            else {
                inSet.weight += de.weight * multiplier;
            }
        }
    }


    private DoubleEntry[] atomizeSet( Set v ) {
        DoubleEntry[] ret = new DoubleEntry[ v.size() ];
        Iterator iter = v.iterator();
        int i=0;
        while( iter.hasNext() ) {
            ret[i++] = new DoubleEntry( atoms.getAtom( (String)iter.next() ));
        }
        Arrays.sort(ret,doubleEntryComparator);
        sanityCheckSorting( ret );
        return ret;
    }

    private DoubleEntry[] atomizeMap( Map v ) {
        DoubleEntry[] ret = new DoubleEntry[ v.size() ];
        Iterator iter = v.entrySet().iterator();
        int i=0;
        Map.Entry e;
        while( iter.hasNext() ) {
            e = (Map.Entry)iter.next();
            ret[i++] = new DoubleEntry( atoms.getAtom( (String)e.getKey()),
                                        ((Double)e.getValue()).doubleValue() );
        }
        Arrays.sort(ret,doubleEntryComparator);
        sanityCheckSorting( ret );
        return ret;
    }

    public String getComponentName() {
        return compname;
    }
    
    public double getWeight() {
        return weight;
    }

    public LockedComponentBehavior canonicalize( Map translationTable ) {
	LockedComponentBehavior ret = new LockedComponentBehavior();
	ret.compname = this.compname;
	ret.inputs = canonicalizeDoubleEntries( inputs, translationTable );
	ret.outputs = canonicalizeDoubleEntries( outputs, translationTable );
	ret.undirected = canonicalizeDoubleEntries( undirected, translationTable );
	ret.summarizes = new HashSet( this.summarizes );
	ret.weight = this.weight;
	if( el != null ) 
	    ret.el = new ArrayList( this.el );

	return ret;
    }

    private DoubleEntry[] canonicalizeDoubleEntries( DoubleEntry[] dearr,
						     Map translationTable ) {
	Map ret = new HashMap();
	for( int i=0; i<dearr.length; i++ ) {
	    DoubleEntry de = dearr[i];
	    String t = (String)
		translationTable.get( atoms.getLabel( de.atom ));
	    double w = 0.0;
	    if( ret.containsKey( t )) {
		w = ((Double)ret.get(t)).doubleValue();
	    }
	    w += de.weight;
	    ret.put( t, new Double(w) );
	}

	return atomizeMap( ret );
    }


    public void appendTo( WeightedSimpleComponentBehavior scb,
			  Map translationTable ) {
	appendTo( scb, translationTable, false );
    }

    public void appendTo( WeightedSimpleComponentBehavior scb,
                          Map translationTable, boolean useDefaultTranslation ) {

        DoubleEntry de;
        String label;


        for( int i=0; i<inputs.length; i++ ) {
            de = inputs[i];
            label = atoms.getLabel( de.atom );
            String label2 = (String)translationTable.get( label );
            if( label2 == null ) {
		if( useDefaultTranslation ) {
		    label2 = label;
		}
		else {
		    throw new RuntimeException( "Translation Table does not contain translation for " + label );
		}
            }
            scb.addWeightToSrc( label2, de.weight );
        }

        for( int i=0; i<outputs.length; i++ ) {
            de = outputs[i];
            label = atoms.getLabel( de.atom );
            String label2 = (String)translationTable.get( label );
            if( label2 == null ) {
		if( useDefaultTranslation ) {
		    label2 = label;
		}
		else {
		    throw new RuntimeException( "Translation Table does not contain translation for " + label );
		}
            }
            scb.addWeightToSink( label2, de.weight );
        }

        for( int i=0; i<undirected.length; i++ ) {
            de = undirected[i];
            label = atoms.getLabel( de.atom );
            String label2 = (String)translationTable.get( label );
            if( label2 == null ) {
		if( useDefaultTranslation ) {
		    label2 = label;
		}
		else {
		    throw new RuntimeException( "Translation Table does not contain translation for " + label );
		}
            }
            scb.addWeightToUndirected( label2, de.weight );
        }

        scb.weight = this.weight;
        scb.addSummarizes( summarizes );
        //scb.addElements( el );
            
    }

    public String toString() {
	return "{LockedComponentBehavior: component=" + compname + 
            ", WEIGHT=" + weight + 
            ", summarizes: " + SetToPrettyString( summarizes ) +
	    ", inputs=" + DoubleEntry.SetToString(atoms,inputs) +
            ", outputs=" + DoubleEntry.SetToString(atoms,outputs) +
            ", undirected=" + DoubleEntry.SetToString(atoms,undirected ) +
            "}";
    }

    public String toShortString() {
	return "{LockedComponentBehavior: component=" + compname + 
            ", WEIGHT=" + weight + 
            ", summarizes: " + SetToPrettyString( summarizes ) +
            "}";
    }

    private String SetToPrettyString( Set strings ) {
        StringBuffer ret = new StringBuffer();
        ret.append( "{\n" );
        Iterator iter = strings.iterator();
        while( iter.hasNext() ) {
            ret.append( "\t" ).append( iter.next() );
//            if( iter.hasNext() ) 
            ret.append( ",\n\t" );
        }
        ret.append( "\n}" );
        return ret.toString();
    }


    public double getDistance( Distanceable d ) {
        try {
            return getDistance( Distanceable.CHI_SQ, d );
        }
        catch( UnsupportedDistanceMetricException bad ) {
            throw new RuntimeException( bad );
        }
    }

    private DoubleEntry[] normalizeByFactor( DoubleEntry[] test, double factor ) {
	DoubleEntry[] ret = new DoubleEntry[ test.length ];
	for( int i=0; i<test.length; i++ ) {
	    ret[i] = new DoubleEntry( test[i].atom, test[i].weight * factor );
	}
	return ret;
    }


    /**
     *  assumes sorted arrays
     */
    private double countChi( DoubleEntry[] a, DoubleEntry[] b ) {
        int i=0;
        int j=0;

	double ret = 0.0;

        int alen = a.length;
        int blen = b.length;

        if( alen == 0 || blen == 0 ) {
	    // MAJOR TODO
            return ret;
	}

        int ai = a[i].atom;
        int bj = b[j].atom;
        double aw = a[i].weight;
        double bw = b[j].weight;
        double min;

        while( true ) {

	    double actual;
	    double expected;

	    String label;

            if( ai < bj ) {
                actual = aw;
		expected = 1.0;
                i++;
		label = atoms.getLabel( ai );
                if( i >= alen )
                    break;
                ai=a[i].atom;
                aw = a[i].weight;
            }
            else if( ai > bj ) {
		actual = 1.0;
		expected = bw;
		label = atoms.getLabel( bj );
                j++;
                if( j >= blen )
                    break;
                bj = b[j].atom;
                bw = b[j].weight;
            }
            else /* cmp == 0 */ {
		actual = aw;
		expected = bw;
		label = atoms.getLabel( ai );
                i++;
                j++;
                if( i >= alen )
                    break;
                if( j >= blen )
                    break;
                ai = a[i].atom;
                aw = a[i].weight;
                bj = b[j].atom;
                bw = b[j].weight;
            }

	    count_unique++;
	    double contrib =
		(actual - expected) * (actual - expected) / expected;

	    System.err.println( "\tChi^2 contrib: " + label + "->" + contrib );

	    ret += contrib;
        }

        return ret;
    }


    /**
     *  assumes sorted arrays
     */
    private Similarity countInCommon( DoubleEntry[] a, DoubleEntry[] b ) {
        Similarity ret = new Similarity();
        int i=0;
        int j=0;

        int alen = a.length;
        int blen = b.length;

        if( alen == 0 || blen == 0 )
            return ret;

        int ai = a[i].atom;
        int bj = b[j].atom;
        double aw = a[i].weight;
        double bw = b[j].weight;
        double min;

        while( true ) {

            if( ai < bj ) {
                ret.onlyinA += a[i].weight;
                i++;
                if( i >= alen )
                    break;
                ai=a[i].atom;
                aw = a[i].weight;
            }
            else if( ai > bj ) {
                ret.onlyinB += b[j].weight;
                j++;
                if( j >= blen )
                    break;
                bj = b[j].atom;
                bw = b[j].weight;
            }
            else /* cmp == 0 */ {
                min = Math.min( aw,bw );
                ret.common += min;
                ret.onlyinA += aw-min;
                ret.onlyinB += bw-min;
                i++;
                j++;
                if( i >= alen )
                    break;
                if( j >= blen )
                    break;
                ai = a[i].atom;
                aw = a[i].weight;
                bj = b[j].atom;
                bw = b[j].weight;
            }
        }

        return ret;
    }

    private void sanityCheckSorting( DoubleEntry[] a ) {
	for( int i=0; i<a.length-1; i++ ) {
	    if( a[i].atom >= a[i+1].atom ) {
		throw new RuntimeException( " (DoubleEntry[" + i + "].atom == " +
					    a[i].atom + ") >= (DoubleEntry[" + (i+1) +
					    "].atom == " + a[i+1].atom + ")" );
	    }
	}
    }


    /**
     *  assumes sorted arrays
     */
    private Similarity countInCommonVerbose( DoubleEntry[] a, DoubleEntry[] b,
					     StringAtoms atoms ) {
        Similarity ret = new Similarity();
        int i=0;
        int j=0;

	atoms.SanityCheck();

	sanityCheckSorting( a );
	sanityCheckSorting( b );

        int alen = a.length;
        int blen = b.length;

        if( alen == 0 || blen == 0 )
            return ret;

	/*
	System.err.println( "\t\tALL In A: " + 
			    DoubleEntry.SetToString(atoms,a ));

	System.err.println( "\t\tALL In B: " + 
			    DoubleEntry.SetToString(atoms,b ));
	*/

        int ai = a[i].atom;
        int bj = b[j].atom;
        double aw = a[i].weight;
        double bw = b[j].weight;
        double min;

	/**
	System.err.println( "---------------------------------------------" );

	System.err.println( "\ta[" + i + "]: " + a[i].toString( atoms ) );
	System.err.println( "\tb[" + j + "]: " + b[j].toString( atoms) );
	*/

	Set onlyInA = new TreeSet();
	Set onlyInB = new TreeSet();
	Set common = new TreeSet();

        while( true ) {

            if( ai < bj ) {
		//System.err.println( "ai<bj: a[" + i + "] -> onlyInA" );
		onlyInA.add( a[i] );
                ret.onlyinA += a[i].weight;
                i++;
		if( i >= alen )
                    break;
		//System.err.println( "\ti++:  a[" + i + "]=" + a[i].toString( atoms ) );
                ai=a[i].atom;
                aw = a[i].weight;
            }
            else if( ai > bj ) {
		//System.err.println( "ai>bj: b" + j + "] -> onlyInB" );

		onlyInB.add( b[j] );
                ret.onlyinB += b[j].weight;
                j++;
                if( j >= blen )
                    break;
		//System.err.println( "\tj++:  b[" + j + "]=" + b[j].toString( atoms ) );
                bj = b[j].atom;
                bw = b[j].weight;
            }
            else /* cmp == 0 */ {
		//System.err.println( "ai==bj" );

                min = Math.min( aw,bw );
		//System.err.println( "\tmin=" + min );
                ret.common += min;
                ret.onlyinA += aw-min;
                ret.onlyinB += bw-min;

		DoubleEntry a1 = new DoubleEntry( ai, aw-min);
		//System.err.println( "\t     " + a1.toString(atoms) + " -> onlyinA" );
		onlyInA.add( a1 );
		DoubleEntry b1 = new DoubleEntry( ai, bw-min); 
		//System.err.println( "\t     " + b1.toString(atoms) + " -> onlyinB" );
		onlyInB.add( b1 );
		DoubleEntry c1 = new DoubleEntry( ai, min );
		common.add( c1);
		//System.err.println( "\t     " + c1.toString(atoms) + " -> common" );


                i++;
                j++;
                if( i >= alen )
                    break;
                if( j >= blen )
                    break;
		//System.err.println( "\ti++:  a[" + i + "]=" + a[i].toString( atoms ) );
		//System.err.println( "\tj++:  b[" + j + "]=" + b[j].toString( atoms ) );
                ai = a[i].atom;
                aw = a[i].weight;
                bj = b[j].atom;
                bw = b[j].weight;
            }
        }


	System.err.println( "\t\tOnlyA: " + 
			    DoubleEntry.SetToString(atoms,onlyInA ));
	System.err.println( "\t\tOnlyB: " + 
			    DoubleEntry.SetToString(atoms,onlyInB ));
	System.err.println( "\t\tCommon: " +
			    DoubleEntry.SetToString(atoms,common ));


        return ret;
    }
    

    public double getDistance( int distancemetric, Distanceable d ) 
	throws UnsupportedDistanceMetricException {
	double ret;
	if( distancemetric == CHI_SQ ) {
	    ret = getDistanceChiSq( d );
	}
	else {
	    ret = getDistanceSimple( distancemetric, d );
	}

	return ret;
    }

    int count_unique = 0;

    public double getDistanceChiSq( Distanceable d ) {

        LockedComponentBehavior other = (LockedComponentBehavior)d;

	double ret=0;

	count_unique = 0;

	DoubleEntry[] de_in = normalizeByFactor( inputs, 
						 other.weight / weight );

        ret += countChi( de_in, other.inputs );

	DoubleEntry[] de_out = normalizeByFactor( outputs,
						  other.weight / weight );

        ret += countChi( de_out, other.outputs );

	DoubleEntry[] de_un = normalizeByFactor( undirected,
						 other.weight / weight );

	System.err.println( "\t\tOriginalOTHER: " + 
			    DoubleEntry.SetToString(atoms,other.undirected ));
	System.err.println( "\t\tOriginalTHIS: " + 
			    DoubleEntry.SetToString(atoms,undirected ));
	System.err.println( "\t\tNormalizedTHIS: " + 
			    DoubleEntry.SetToString(atoms,de_un ));


        ret += countChi( de_un, other.undirected );

	// TODO: should normalize ret by the chi^2 threshold for some number of degrees of freedom...

	double chithreshold = getChi2( count_unique );

	System.err.println( "\tChi Threshold=" + chithreshold );

	ret /= chithreshold;

	return ret;
    }

    private double getChi2( int n ) {
	double[] chi_0995 = {
	    7.87943857662242, // 1 degree of freedom
	    10.5966347330961,
	    12.8381564665987,
	    14.8602590005602,
	    16.749602343639,
	    18.5475841785111,
	    20.2777398749626,
	    21.9549549906595,
	    23.5893507812574,
	    25.1881795719712,
	    26.7568489164696,
	    28.299518822046,
	    29.8194712236532,
	    31.3193496225953,
	    32.8013206457918,
	    34.2671865378267,
	    35.7184656590046,
	    37.1564514566067,
	    38.5822565549342,
	    39.9968463129386,
	    41.4010647714176,
	    42.7956549993085,
	    44.1812752499711,
	    45.5585119365306,
	    46.9278901600808,
	    48.2898823324568,
	    49.6449152989942,
	    50.9933762684994,
	    52.3356177859336,
	    53.6719619302406,
	    55.0027038800239,
	    56.3281149597109,
	    57.6484452558585,
	    58.9639258755194,
	    60.274770904781,
	    61.5811791147573,
	    62.8833354537412,
	    64.1814123574062,
	    65.475570903468,
	    66.7659618328039,
	    68.0527264554416,
	    69.3359974569004,
	    70.6158996179664,
	    71.8925504589992,
	    73.166060818225,
	    74.4365353721017,
	    75.7040731046948,
	    76.9687677320445,
	    78.2307080866899,
	    79.4899784668289,
	    80.7466589540133,
	    82.0008257027754,
	    83.2525512051611,
	    84.5019045327765,
	    85.748951558641,
	    86.9937551608717,
	    88.2363754099821,
	    89.476869741381,
	    90.7152931144758,
	    91.9516981596297,
	    93.1861353140891,
	    94.4186529478745,
	    95.6492974805285,
	    96.8781134895179,
	    98.1051438110095,
	    99.3304296336631,
	    100.554010586028,
	    101.775924818064,
	    102.996209077265,
	    104.214898779817,
	    105.432028077177,
	    106.647629918433,
	    107.861736108763,
	    109.074377364285,
	    110.28558336358,
	    111.495382796113,
	    112.70380340779,
	    113.910872043852,
	    115.116614689292,
	    116.321056506969,
	    117.524221873581,
	    118.726134413634,
	    119.926817031548,
	    121.126291942023,
	    122.324580698781,
	    123.521704221777,
	    124.717682822992,
	    125.912536230897,
	    127.106283613653,
	    128.298943601145,
	    129.49053430592,
	    130.681073343076,
	    131.870577849188,
	    133.059064500317,
	    134.246549529152,
	    135.433048741346,
	    136.61857753108,
	    137.803150895913,
	    138.986783450939,
	    140.169489442314
	};

	if( n >= chi_0995.length ) {
	    System.err.println( "WARNING: requesting chi^2 significance that is too large: " + n );
	    n = chi_0995.length - 1;
	}

	return chi_0995[n-1];
    }


    public double getDistanceSimple( int distancemetric, Distanceable d ) 
        throws UnsupportedDistanceMetricException {

        LockedComponentBehavior other = (LockedComponentBehavior)d;

        Similarity sim;

	DoubleEntry[] de_in = normalizeByFactor( inputs, 
						 other.weight / weight );

        sim = countInCommon( de_in, other.inputs );

	DoubleEntry[] de_out = normalizeByFactor( outputs,
						  other.weight / weight );

        Similarity tmp = countInCommon( de_out, other.outputs );
        sim.add( tmp );

	DoubleEntry[] de_un = normalizeByFactor( undirected,
						 other.weight / weight );

        tmp = countInCommon( de_un, other.undirected );
        sim.add( tmp );

        return DistanceableHelper.calculateDistance( distancemetric,
                                                     Distanceable.JACCARD,
                                                     sim.common,
                                                     sim.onlyinA,
                                                     sim.onlyinB,
                                                     0 );
    }


    public void getDistanceVerbose( Distanceable d ) {

        LockedComponentBehavior other = (LockedComponentBehavior)d;

        Similarity sim;

        sim = countInCommon( inputs, other.inputs );

        Similarity tmp = countInCommonVerbose( outputs, other.outputs, atoms );
        sim.add( tmp );

        tmp = countInCommonVerbose( undirected, other.undirected, atoms );
        sim.add( tmp );

	System.err.println( "\t{Common=" + sim.common + "," +
			    "\t onlyinA=" + sim.onlyinA + "," +
			    "\t onlyinB=" + sim.onlyinB + "}" );

    }

    public void writeExternal( ObjectOutput out ) 
        throws IOException {

        out.writeObject( compname );
        writeDoubleEntryArray( out, inputs );
        writeDoubleEntryArray( out, outputs );
        writeDoubleEntryArray( out, undirected );
        writeSet( out, summarizes );
        out.writeDouble( weight );
    }

    private void writeDoubleEntryArray( ObjectOutput out,
                                        DoubleEntry[] de ) 
        throws IOException {
	DoubleEntry[] deSorted = new DoubleEntry[ de.length ];
	System.arraycopy( de, 0, deSorted, 0, de.length );
	Arrays.sort( deSorted );
        out.writeInt( deSorted.length );
        for( int i=0; i<deSorted.length; i++ ) {
            deSorted[i].writeExternal( atoms, out );
        }
    }

   private void writeSet( ObjectOutput out,
                          Set set ) 
        throws IOException {
       out.writeInt( set.size() );
       Iterator iter = set.iterator();
       while( iter.hasNext() ) {
           out.writeObject( iter.next() );
       }
    }
        
    public void readExternal( ObjectInput in ) 
        throws IOException {

        try {
            compname = (String)in.readObject();
            inputs = readDoubleEntryArray( in );
            outputs = readDoubleEntryArray( in );
            undirected = readDoubleEntryArray( in );
            summarizes = readSet( in );
            weight = in.readDouble();
        }
        catch( ClassNotFoundException cnfe ) {
            throw new RuntimeException( cnfe );
        }
    }

    private DoubleEntry[] readDoubleEntryArray( ObjectInput in ) 
        throws IOException, ClassNotFoundException {
        int len = in.readInt();
        DoubleEntry[] ret = new DoubleEntry[ len ];

        for( int i=0; i<len; i++ ) {
            ret[i] = DoubleEntry.readExternal( atoms, in );
        }

	Arrays.sort( ret, doubleEntryComparator );

        return ret;
    }

    private Set readSet( ObjectInput in ) 
        throws IOException, ClassNotFoundException {
        int size = in.readInt();
        Set ret = new HashSet(size);

        for( int i=0; i<size; i++ ) {
            ret.add( in.readObject() );
        }

        return ret;
    }





    static final class Similarity {
        double common=0;
        double onlyinA=0;
        double onlyinB=0;
        
        public void add( Similarity tmp ) {
            this.common += tmp.common;
            this.onlyinA += tmp.onlyinA;
            this.onlyinB += tmp.onlyinB;
        }
    }

    static Comparator doubleEntryComparator = new Comparator() {

            public int compare( Object o1, Object o2 ) {
                return ((DoubleEntry)o1).atom - ((DoubleEntry)o2).atom;
            }

            public boolean equals( Object o ) {
                return this.equals( o );
            }
        };



}
