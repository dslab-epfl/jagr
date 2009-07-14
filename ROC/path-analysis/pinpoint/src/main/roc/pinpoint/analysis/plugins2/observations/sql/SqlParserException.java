package roc.pinpoint.analysis.plugins2.observations.sql;

/**
 * Exception for SQL parsing errors.
 *
 * @author  <A HREF="http://www.cs.berkeley.edu/~mikechen/">Mike Chen</A>
 *              (<A HREF="mailto:mikechen@cs.berkeley.edu">
 *                mikechen@cs. berkeley.edu</A>)
 * @version $Id: SqlParserException.java,v 1.1 2003/02/26 23:31:32 emrek Exp $
 */
public class SqlParserException extends Exception {
    
    /**
     * @see java.lang.Throwable#Throwable(String)
     */
    public SqlParserException(String msg) {
        super(msg);
    }
}
