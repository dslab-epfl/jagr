package polyglot.ext.ib.extension;

import java.io.*;
import java.util.*;
import polyglot.ast.*;
import polyglot.types.*;
import polyglot.util.Position;

import polyglot.frontend.Job;

    public class BugSpotInfo {

	private static Expr createRuntimeBugReport( NodeFactory nf,
						    TypeSystem ts,
						    Position pos,
						    Type cast,
						    String methodname,
						    List args ) {
	    Expr ret;

	    try {
	    Call call = nf.Call( pos, 
				 nf.CanonicalTypeNode( pos, ts.typeForName( "roc.pinpoint.rtsrcbug.RuntimeSourceBugHelper" )),
				 methodname, args );


	    if( cast == null ) {
		ret = call;
	    }
	    else {
		ret = nf.Cast( pos, nf.CanonicalTypeNode( pos, cast ), call );
	    }
	    }
	    catch( SemanticException ex ) {
		throw new RuntimeException( ex );
	    }
	    
	    return ret;
	}

	public static Expr createRuntimeBugReport( NodeFactory nf,
						    TypeSystem ts,
						   Position pos,
						   String methodname ) {
	    List l = new ArrayList(1);
	    l.add( nf.StringLit( pos, pos.toString() ));
	    return createRuntimeBugReport( nf, ts, pos, null, methodname, l );
	}

	public static Expr createRuntimeBugReport( NodeFactory nf,
						    TypeSystem ts,
						   Position pos,
						   Type cast,
						   String methodname,
						   Expr arg1 ) {
	    List l = new ArrayList(2);
	    l.add( arg1 );
	    l.add( nf.StringLit( pos, pos.toString() ));
	    return createRuntimeBugReport( nf, ts, pos, cast, methodname, l );
	}

	public static Expr createRuntimeBugReport( NodeFactory nf,
						    TypeSystem ts,
						   Position pos,
						   Type cast,
						   String methodname,
						   Expr arg1, Expr arg2 ) {
	    List l = new ArrayList(3);
	    l.add( arg1 );
	    l.add( arg2 );
	    l.add( nf.StringLit( pos, pos.toString() ));
	    return createRuntimeBugReport( nf, ts, pos, cast, methodname, l );
	}

	public static Expr createRuntimeBugReport( NodeFactory nf,
						    TypeSystem ts,
						   Position pos,
						   Type cast,
						   String methodname,
						   Expr arg1, Expr arg2, 
						   int arg3 , int arg4 ) {
	    List l = new ArrayList( 5 );
	    l.add( arg1 );
	    l.add( arg2 );
	    l.add( nf.IntLit( pos, IntLit.INT, (long)arg3 ));
	    l.add( nf.IntLit( pos, IntLit.INT, (long)arg4 ));
	    l.add( nf.StringLit( pos, pos.toString() ));
	    return createRuntimeBugReport( nf, ts, pos, cast, methodname, l );
	}




	static Map bugspotinfos =
	    Collections.synchronizedMap( new HashMap() );

	public static void CreateBugSpotInfo( Job job ) {
	    try {
		bugspotinfos.put( job, new BugSpotInfo() );
	    }
	    catch( IOException ex ) {
		ex.printStackTrace();
	    }
	}

	public static BugSpotInfo GetBugSpotInfo( Job job ) {
	    return (BugSpotInfo)bugspotinfos.get( job );
	}

	/**************************************************/

	ArrayList bugspots;
	int curridnum;

	List activebugs;

	ActiveBug curractive;

	public BugSpotInfo() throws IOException {
	    bugspots = new ArrayList();
	    curridnum=0;
	    initActiveBugList();
	}

	private void initActiveBugList() throws IOException {
	    String filename = System.getProperty( "roc.injectbug.ActiveBugList" );
	    if( filename == null ) {
		System.err.println( "[INFO] No active bug list found.  " + 
				    "Please set java system property " + 
				    "'roc.injectbug.ActiveBugList'" );
	    }
	    else {
		System.err.println( "[INFO] Loading active bug list from '" +
				    filename + "'" );
		initActiveBugList( new File( filename ));
	    }
	}

	public synchronized void initActiveBugList( File f ) 
	    throws IOException {
	    LineNumberReader lnr = new LineNumberReader( new FileReader( f ));

	    activebugs = new ArrayList();

	    while( true ) {
		
		String line = lnr.readLine();
		if( line == null ) {
		    break;
		}

		StringTokenizer st = new StringTokenizer( line, "," );
		String sFile = st.nextToken();
		String sBugspotid = st.nextToken();
		String sParam = st.nextToken();
		
		ActiveBug ab = new ActiveBug(sFile,
					      Integer.parseInt(sBugspotid),
					      Integer.parseInt(sParam));

		System.err.println( "[INFO] Adding ActiveBug: " + 
				    ab.toString() );
		
		activebugs.add( ab );
	    }
	}

	public synchronized void add( Node n, 
				      Class bugtype, int subtype, int maxparam,
				      String descr ) {
	    BugSpot bs = new BugSpot();
	    bs.idnum = curridnum++;
	    bs.nodedescr = n.toString();
	    bs.bugtype = bugtype;
	    bs.position = n.position();
	    bs.subtype = subtype;
	    bs.maxparam = maxparam;
	    bs.bugdescr = descr;

	    bugspots.add( bs );

	    if( activebugs == null )
		System.err.println( "[BUGSPOT] " + bs.toString() );

	    // search to see if curr spot is active node
	    String currfilename = bs.position.file();

	    curractive = null;
	    if( activebugs != null ) {
		Iterator iter = activebugs.iterator();
		while( iter.hasNext() ) {
		    ActiveBug ab = (ActiveBug)iter.next();
		    if( ab.file.equals( currfilename ) &&
			ab.bugspotid == bs.idnum ) {
			curractive = ab;
			break;
		    }
		}
	    }
	}

	public synchronized boolean isActive( Node n, 
					      Class bugtype, int subtype ) {
	    return curractive != null;
	}

	public synchronized int getActiveBugParam( Node n,
						 Class bugtype, int subtype ) {
	    return (curractive==null)?0:curractive.param;
	}

	class ActiveBug {
	    String file;
	    int bugspotid;
	    int param;
	  
	    ActiveBug( String file, int bugspotid, int param ) {
		this.file = file;
		this.bugspotid = bugspotid;
		this.param = param;
	    }

	    public String toString() {
		StringBuffer ret = new StringBuffer();
		
		ret.append( file ).append(',');
		ret.append( bugspotid ).append(',');
		ret.append( param );

		return ret.toString();
	    }
	}

	static class BugSpot {
	    int idnum;

	    Position position;
	    String nodedescr;
	    String bugdescr;
	    Class bugtype;
	    int subtype;
	    int maxparam;

	    public String toString() {
		StringBuffer ret = new StringBuffer();

		ret.append( idnum ).append( ',' );
		ret.append( position.toString() ).append( ',' );
		ret.append( nodedescr ).append( ',' );
		ret.append( bugdescr ).append( ',' );
		ret.append( bugtype.toString() ).append( ',' );
		ret.append( subtype ).append( ',' );
		ret.append( maxparam );

		return ret.toString();
	    }
	}

    }



