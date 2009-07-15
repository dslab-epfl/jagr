/**
    Copyright (C) 2004 Emre Kiciman and Stanford University

    This file is part of Pinpoint

    Pinpoint is free software; you can distribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2.1 of the License, or
    (at your option) any later version.

    Pinpoint is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with Pinpoint; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
**/
package roc.pinpoint.tracing.sql;

// marked for release 1.0

import java.sql.Statement;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

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
 *              (<A HREF="mailto:mikechen@cs.berkeley.edu">
 *               mikechen@cs. berkeley.edu</A>)
 * @version $Id: SqlStatementsMap.java,v 1.3 2004/05/10 23:42:13 emrek Exp $
 */
public class SqlStatementsMap {
    private static Map map = Collections.synchronizedMap(new WeakHashMap());

    /**
     * cache an sql query, keyed by a prepared Statement.
     * @param statement a prepared statement, used as the index key.
     * @param query the string query to cache
     * @return String returns the string query previously associated with this
     * key, if any
     */
    public static String put(Statement statement, String query) {
        //System.err.println(map);
        return (String) map.put(statement, query);
    }

    /**
     * get the sql query associated with this statement.
     * @param statement a prepared statement
     * @return String an sql query
     */
    public static String get(Statement statement) {
        return (String) map.get(statement);
    }
}
