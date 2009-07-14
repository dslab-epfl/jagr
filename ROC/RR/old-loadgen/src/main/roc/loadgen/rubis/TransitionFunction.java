package roc.loadgen.rubis;

import java.io.*;
import java.lang.NumberFormatException;
import java.util.*;
import java.lang.Math;

import org.apache.log4j.Logger;

/**
 * Transition function for the simulated RUBiS user state machine.
 *
 * @version <tt>$Revision: 1.1 $</tt>
 * @author  <a href="mailto:candea@cs.stanford.edu">George Candea</a>
 *
 * Based on the RUBiS 1.4.1 client emulator, written by
 * <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and
 * <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 */

public class TransitionFunction
{
    private int    nbStates;
    private float  transitions[][];
    private String tableName = null;
    private Random rand;
    private Stack  previousStates = new Stack();
    private static String[] stateNames;

    private int currentState=0;

    private static Logger log = Logger.getLogger( "TransitionFunction" );

    // The RUBiS states

    public static final RubisUserState HOME        = new RubisUserState(  0, "Home" );
    public static final RubisUserState REGISTER    = new RubisUserState(  1, "Register" );
    public static final RubisUserState REG_USER    = new RubisUserState(  2, "RegisterUser" );
    public static final RubisUserState BROWSE      = new RubisUserState(  3, "Browse" );
    public static final RubisUserState BROWSE_CAT  = new RubisUserState(  4, "BrowseCategories" );
    public static final RubisUserState SEARCH_CAT  = new RubisUserState(  5, "SearchItemsInCategory" );
    public static final RubisUserState BROWSE_REG  = new RubisUserState(  6, "BrowseRegions" );
    public static final RubisUserState BR_CAT_REG  = new RubisUserState(  7, "BrowseCategoriesInRegion" );
    public static final RubisUserState SEARCH_REG  = new RubisUserState(  8, "SearchItemsInRegion" );
    public static final RubisUserState VIEW_ITEM   = new RubisUserState(  9, "ViewItem" );
    public static final RubisUserState VIEW_USER   = new RubisUserState( 10, "ViewUserInfo" );
    public static final RubisUserState VIEW_HIST   = new RubisUserState( 11, "ViewBidHistory" );
    public static final RubisUserState BUY_NOW     = new RubisUserState( 12, "BuyNow" );
    public static final RubisUserState STORE_BUY   = new RubisUserState( 13, "StoreBuyNow" );
    public static final RubisUserState PUT_BID     = new RubisUserState( 14, "PutBid" );
    public static final RubisUserState STORE_BID   = new RubisUserState( 15, "StoreBid" );
    public static final RubisUserState PUT_COMMENT = new RubisUserState( 16, "PutComment" );
    public static final RubisUserState STORE_COMM  = new RubisUserState( 17, "StoreComment" );
    public static final RubisUserState SELECT_CAT  = new RubisUserState( 18, "SelectCategorySellItem" );
    public static final RubisUserState SELL_ITEM   = new RubisUserState( 19, "SellItemForm" );
    public static final RubisUserState REG_ITEM    = new RubisUserState( 20, "RegisterItem" );
    public static final RubisUserState ABOUT_ME    = new RubisUserState( 21, "AboutMe" );
    public static final RubisUserState LOGIN       = new RubisUserState( 22, "Login" );
    public static final RubisUserState LOGIN_USR   = new RubisUserState( 23, "Login User" );
    public static final RubisUserState LOGOUT      = new RubisUserState( 24, "Logout" );
    public static final RubisUserState BACK        = new RubisUserState( 25, "Back" );
    public static final RubisUserState ABANDON     = new RubisUserState( 26, "AbandonSession" );

    private static final String SEPARATOR=","; // we expect a comma-separated file

    // A list of all states present in this table
    RubisUserState[] states = { HOME, REGISTER, REG_USER, BROWSE, BROWSE_CAT, SEARCH_CAT, BROWSE_REG,
			        BR_CAT_REG, SEARCH_REG, VIEW_ITEM, VIEW_USER, VIEW_HIST, BUY_NOW,
			        STORE_BUY, PUT_BID, STORE_BID, PUT_COMMENT, STORE_COMM, SELECT_CAT,
			        SELL_ITEM, REG_ITEM, ABOUT_ME, LOGIN, LOGIN_USR, LOGOUT, BACK, ABANDON };

    /**
     * Constructor.
     *
     * @param filename File containing the transition table
     *
     */
    public TransitionFunction( String filename )
	throws FileNotFoundException, IOException, NoSuchElementException, NumberFormatException
    {
	rand = new Random();
	ParseTransitionFunctionFile( filename );
    }


    /**
     * Compute the next state from the current state, based on the
     * transition matrix.
     *
     * @param  currentState user's current state (if null, user is uninitialized)
     * @return next state
     *
     */
    public RubisUserState nextState( RubisUserState currentState )
    {
	assert currentState != null;
	assert currentState != ABANDON;
	assert currentState != BACK;

	log.debug( "nextState: " + currentState );

	float step = rand.nextFloat();
	float cumul = 0;
	float row[] = transitions[ currentState.getId() ];

	for( int i=0 ; i < row.length ; i++ )
        {
	    cumul = cumul + row[ i ];
	    if( step < cumul )
	    {
		return (RubisUserState) states[ i ];
	    }
	}

	assert false : "row.length= " + row.length + " cumul= " + cumul + " step= " + step + " row= " + row;
	return null;
    }

  
    /**
     * Read the transition matrix from a file.
     *
     * @param filename name of the file to read the matrix from
     *
     */
    private void ParseTransitionFunctionFile( String filename )
	throws FileNotFoundException, IOException, NoSuchElementException, NumberFormatException
    { 
	log.info( "Parsing workload file " + filename );

	BufferedReader reader = new BufferedReader( new FileReader(filename) );
	ParseColumnHeaders( reader );
	ParseRows( reader );

	validateTransitionFunction();
    }


    /**
     * Parse the header portion of the transition table.
     *
     * @param in The source to read from
     *
     */
    private void ParseColumnHeaders( BufferedReader in )
	throws IOException
    {
	int i;
	StringTokenizer st = new StringTokenizer( in.readLine(), SEPARATOR );
	
	String s = st.nextToken();   assert s.equals( "\"RUBiS Transition Table\"" ) : s;
	tableName = st.nextToken(); 
	log.info( "Found " + tableName );

	s = in.readLine();   assert s.startsWith( SEPARATOR )        : s; // empty line
	s = in.readLine();   assert s.startsWith( "\"To >>>\"" )     : s;
	s = in.readLine();   assert s.startsWith( "\"From vvvvv\"" ) : s; // column names
	
	String[] columnNames = s.split( SEPARATOR );

	// First column has state names, and
	// last two columns have "wait time" and "probability sum", respectively
 	for( i=1 ; i < columnNames.length-2 ; i++ )
	{
	    String name = columnNames[i];
	    name = name.substring( 1, name.length()-1 );  // skip the surrounding quotes

	    assert name.equals( states[i-1].getName() ) : name + " != " + states[i-1].getName();
	}
    }


    /**
     * Parse the rows of the transition table.
     *
     * @param in The source to read from
     * @throws IOException
     */
    private void ParseRows( BufferedReader in )
	throws IOException
    {
	int nbRows = states.length - 2; // we don't have rows for "Back" and "EndSession"
	int nbCols = states.length;

	transitions = new float[ nbRows ][ nbCols ];

	for( int i=0 ; i < nbRows ; i++ )
	{
	    StringTokenizer st = new StringTokenizer( in.readLine(), SEPARATOR );
	    String stateName = st.nextToken();
	    stateName = stateName.substring( 1, stateName.length()-1 );  // skip the surrounding quotes
	    
	    assert stateName.equals( states[i].getName() ) : stateName + " != " + states[i].getName();
	    
	    // Fill out the values for row i in the transition matrix
	    for( int j=0 ; j < nbCols ; j++ )
	    {
		Float f = new Float( st.nextToken() );
		transitions[ i ][ j ] = f.floatValue();
	    }

	    // Ignore the remaining columns ("wait time" and "probability sum")
	}
    }

    /**
     * Convert this TransitionFunction to a string
     */
    public String toString()
    {
	String ret = "";

	for( int i=0 ; i < states.length ; i++ )
	{
	    ret += "\n" + states[i];
	}

	return ret;
    }

    /**
     * Check that the probabilities add up to 1 in each row of the
     * transition matrix.
     *
     */
    private void validateTransitionFunction()
    {
	for( int i=0 ; i < transitions.length ; i++ )
	{
	    float rowTotal=0;
	    for( int j=0 ; j < transitions[i].length ; j++ )
	    {
		rowTotal += transitions[ i ][ j ];
	    }
 	    if( Math.abs( rowTotal - 1.0 ) > 1.0e-6 ) 
	    {
		throw new RuntimeException( "Row " + i + " adds up to " + rowTotal + " instead of 1" );
	    }
	}
    }	    
}
