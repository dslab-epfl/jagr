package roc.pinpoint.analysis.plugins.anomaly;

import java.util.Map;
import java.io.Serializable;

/**
 * Link between src and sink components.  counts the number of requests which
 * have been observed passing from the src to sink component.
 * @author emrek
 *
 */
public class Link implements Serializable {

    private Map srcComponentAttributes;
    private Map sinkComponentAttributes;

    private StatInfo statInfo;

    /**
     * default constructor.  
     * @param srcComponentAttributes defining attributes of the source component
     * @param sinkComponentAttributes defining attributes of the sink component
     */
    public Link(Map srcComponentAttributes, Map sinkComponentAttributes) {
        this.srcComponentAttributes = srcComponentAttributes;
        this.sinkComponentAttributes = sinkComponentAttributes;
	this.statInfo = new StatInfo();
    }

    /**
     * @return long count of how often this link has been followed
     */
    public synchronized StatInfo getStatInfo() {
        return statInfo;
    }

    /**
     * increment the number of times this link has been followed
     */
    public synchronized void incrementCount() {
        statInfo.addValue( getKey(), new Integer( 1 ));
    }

    /**
     * @return Map returns defining attributes for source component
     */
    public Map getSrcComponentAttributes() {
        return srcComponentAttributes;
    }

    /**
     * @return Map returns defining attributes of sink component
     */
    public Map getSinkComponentAttributes() {
        return sinkComponentAttributes;
    }

    public static String GetKey( Map srcComponentAttrs, 
				 Map sinkComponentAttrs ) {
	String key = 
	    "[src=" + srcComponentAttrs.toString() 
	    + ", sink=" + sinkComponentAttrs.toString() + "]";
	return key;
    }

    /**
     * @return Integer returns a hash code / key based on the src and sink
     * components
     */
    public String getKey() {
	return GetKey( srcComponentAttributes, sinkComponentAttributes );
    }

    public String toString() {
	StringBuffer ret = new StringBuffer();

	ret.append( "Link: [\n" );
	ret.append( "\tsrc = " );
	ret.append( srcComponentAttributes.toString() );
	ret.append( "\n\tsink = " );
	ret.append( sinkComponentAttributes.toString() );
	ret.append( "\n\tStat Info = " );
	ret.append( statInfo == null ? "null" : statInfo.toString() );
	ret.append( "\n]\n" );

	return ret.toString();
    }

}
