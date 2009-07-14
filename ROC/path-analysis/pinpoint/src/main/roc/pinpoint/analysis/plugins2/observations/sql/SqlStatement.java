package roc.pinpoint.analysis.plugins2.observations.sql;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Parses simple SQL statements to extract the tables names. 
 * Currently supports SELECT and UPDATE. 
 *
 * @author  <A HREF="http://www.cs.berkeley.edu/~mikechen/">Mike Chen</A>
 *              (<A HREF="mailto:mikechen@cs.berkeley.edu">
 *                mikechen@cs. berkeley.edu</A>)
 * @version $Id: SqlStatement.java,v 1.1 2003/02/26 23:31:32 emrek Exp $
 */

/**
 * basic sql statement parser
 * @author emrek
 */
public class SqlStatement {
    private String statement;
    private List tables;
    private boolean isUpdate = false;
    private boolean isSelect = false;

    private boolean isRead;

    /**
     * constructor
     * @param statement SQL statement to parse
     * @throws SqlParserException could not parse sql
     */
    public SqlStatement(String statement) throws SqlParserException {
        this.statement = statement;
        parse();
    }

    void parse() throws SqlParserException {
        statement = statement.trim().toLowerCase();
        if (statement.startsWith("update")) {
            String tableStr =
                statement
                    .substring("update".length(), statement.indexOf("set"))
                    .trim();
            parseTableStr(tableStr);
            isRead = false;
        }
        else if (statement.startsWith("select")) {
            int end = statement.indexOf("where");
            if (end == -1) {
                end = statement.length();
            }
            String tableStr =
                statement
                    .substring(statement.indexOf("from") + "from".length(), end)
                    .trim();
            parseTableStr(tableStr);
            isRead = true;
        }
        else {
            throw new SqlParserException(
                "SQL statement type not handled: " + statement);
        }
    }

    void parseTableStr(String tableStr) {
        //System.out.println("tableStr:" + tableStr);
        StringTokenizer st = new StringTokenizer(tableStr, ",");
        tables = new ArrayList();
        while (st.hasMoreTokens()) {
            String table = st.nextToken().trim();
            if (table.indexOf(" ") != -1) {
                tables.add(table.substring(0, table.indexOf(" ")));
            }
            else {
                tables.add(table);
            }
        }

    }

    /**
     * is this SQL statement a read access? 
     * @return boolean returns true if statement is a read access, false if a
     * write.
     */
    public boolean isRead() {
        return isRead;
    }

    /**
     * @return List a list of tables accessed by this query
     */
    public List getTables()  {
        return tables;
    }

    /**
     * quick check to see if an sql statement is a query, as opposed to,
     *  for example, an administrative  statement.
     * @param str sql statement
     * @return boolean true if statement is a read or write access
     */
    public static boolean IsQuery(String str) {
        str = str.toLowerCase();
        return (str.startsWith("select ") || str.startsWith("update "));
    }

}
