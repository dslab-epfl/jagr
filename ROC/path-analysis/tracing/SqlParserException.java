package tracing;

import java.util.*;

/**
 * Exception for SQL parsing errors.
 *
 * @author  <A HREF="http://www.cs.berkeley.edu/~mikechen/">Mike Chen</A>
 *              (<A HREF="mailto:mikechen@cs.berkeley.edu">mikechen@cs.berkeley.edu</A>)
 * @version $Id: SqlParserException.java,v 1.1 2002/11/13 20:18:21 mikechen Exp $
 */ 


public class SqlParserException extends Exception {
    public SqlParserException(String msg) {
	super(msg);
    }
}

