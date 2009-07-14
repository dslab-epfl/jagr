package tracing;

import java.util.*;
import java.sql.*;

/**
 * A map to track SQL statements and the corresponding SQL queries.
 * Useful to map queries to PreparedStatements.
 * 
 * The key is a reference to the PreparedStatment and
 * the values are the corresponding SQL statements.
 * We will use a WeakReference to these statements because we 
 * want the statements to be garbage collected when it's no longer being used.
 *
 * @author  <A HREF="http://www.cs.berkeley.edu/~mikechen/">Mike Chen</A>
 *              (<A HREF="mailto:mikechen@cs.berkeley.edu">mikechen@cs.berkeley.edu</A>)
 * @version $Id: SqlStatementsMap.java,v 1.2 2002/11/18 09:59:11 mikechen Exp $
 */ 


public class SqlStatementsMap {
    static Map map = Collections.synchronizedMap(new WeakHashMap());
    
    public static String put(Statement statement, String query) {
	//System.err.println(map);
	return (String)map.put(statement, query);
    }

    public static String get(Statement statement) {
	return (String)map.get(statement);
    }
}

