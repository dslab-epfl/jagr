package roc.pinpoint.analysis;

/**
 * 
 * @author emrek
 *
 */
public class PluginException extends Exception {

    /**
     * 
     * @see java.lang.Throwable#Throwable(String)
     */
    public PluginException(String s) {
        super(s);
    }

    /**
     * 
     * @see java.lang.Throwable#Throwable(String, Throwable)
     */
    public PluginException(String s, Throwable t) {
        super(s, t);
    }

    /**
     * 
     * @see java.lang.Throwable#Throwable(Throwable)
     */
    public PluginException(Throwable t) {
        super(t);
    }

}
