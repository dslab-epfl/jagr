/*
 * Created on Apr 17, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package roc.loadgen;

/**
 * @author emrek
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class IllegalPluginException extends Exception {


    /**
     * 
     */
    public IllegalPluginException() {
        super();
    }

    /**
     * @param message
     */
    public IllegalPluginException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public IllegalPluginException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public IllegalPluginException(Throwable cause) {
        super(cause);
    }

}
