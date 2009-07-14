package roc.pinpoint.analysis;


import java.util.*;

/**
 * defines arguments for plugins.  used by parser.
 *
 */
public class PluginArg {

    public static final int ARG_BOOLEAN = 0;
    public static final int ARG_STRING = 1;
    public static final int ARG_INTEGER = 2;
    public static final int ARG_DOUBLE = 3;
    public static final int ARG_LIST = 4;
    public static final int ARG_MAP = 5;
    public static final int ARG_RECORDCOLLECTION = 6;

    public String name;
    public String description;
    public int argType; // choose from one of the ARG_* above.
    public boolean required;
    public String defaultValue;

    public PluginArg( String name,
		      String description,
		      int argType,
		      boolean required,
		      String defaultValue ) {
	this.name = name;
	this.description = description;
	this.argType = argType;
	this.required = required;
	this.defaultValue = defaultValue;
    }

    Object parseArgument( String s, 
			  AnalysisEngine engine ) throws PluginException {

	if( required && s == null )
	    throw new PluginException( "Required argument '" + name + "' not declared!" );

	if( s == null )
	    s = defaultValue;

	if( s != null && s.startsWith( "$" )) {
	    String key = s.substring( 1 );
	    s = (String)engine.engineArguments.get( key );
	}

	if( argType == ARG_BOOLEAN ) {
	    return Boolean.valueOf( s );
	}
	else if( argType == ARG_STRING ) {
	    return s;
	}
	else if( argType == ARG_INTEGER ) {
	    return new Integer( s );
	}
	else if( argType == ARG_DOUBLE ) {
	    return new Double( s );
	}
	else if( argType == ARG_LIST ) {
	    List ret = new LinkedList();
	    StringTokenizer st = new StringTokenizer(s, ",\n");
	    while (st.hasMoreTokens()) {
		String t = st.nextToken();
		t = t.trim();
		if( t.length() != 0 )
		    ret.add(t);
	    }
	    return ret;
	}
	else if( argType == ARG_MAP ) {
	    Map ret = new HashMap();
	    StringTokenizer st = new StringTokenizer(s, ",\n");
	    while (st.hasMoreTokens()) {
		String t = st.nextToken();
		int i = t.indexOf( '=' );
		String k = t.substring( 0, i );
		String v = t.substring( i+1 );
		k = k.trim();
		v = v.trim();
		ret.put( k, v );
	    }
	    return ret;	    
	}
	else if( argType == ARG_RECORDCOLLECTION ) {
	    RecordCollection rc = engine.getRecordCollection( s );
	    if( rc == null )
		throw new PluginException( "PluginArg: while parsing argument '" 
					   + name + "', could not find record collection '" + s + "'" );
	    return rc;
	}
	else {
	    throw new PluginException( "PluginArg: while parsing argument '" + name + "', unrecognized argument type: " + argType );
	}

    }
}
