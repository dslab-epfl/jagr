/**
 *  SSMException will be thrown at the time of SSM write and read error
 *
 *
 *  $Id: SSMException.java,v 1.1 2004/08/23 17:15:14 skawamo Exp $
 */

package roc.rr.ssmutil;
import java.lang.Exception;

public class SSMException extends Exception {
    public SSMException(String message) {
	super(message);
    }
}
