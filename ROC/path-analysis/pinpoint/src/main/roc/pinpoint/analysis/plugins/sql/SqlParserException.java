package roc.pinpoint.analysis.plugins.sql;

/**
 * Exception for SQL parsing errors.
 *
 * @author  <A HREF="http://www.cs.berkeley.edu/~mikechen/">Mike Chen</A>
 *              (<A HREF="mailto:mikechen@cs.berkeley.edu">
 *                mikechen@cs. berkeley.edu</A>)
 * @version $Id: SqlParserException.java,v 1.2 2002/12/28 12:27:30 emrek Exp $
 */
public class SqlParserException extends Exception {
    
    /**
     * @see java.lang.Throwable#Throwable(String)
     */
    public SqlParserException(String msg) {
        super(msg);
    }
}
