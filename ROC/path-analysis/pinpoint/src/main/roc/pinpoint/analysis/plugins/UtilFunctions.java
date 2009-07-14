package roc.pinpoint.analysis.plugins;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class UtilFunctions {

    public static Map CreateComponentAttr( Map originInfo, 
					   Collection definingAttributes) {
        Map ret = new HashMap();

        Iterator iter = definingAttributes.iterator();
        while (iter.hasNext()) {
            String k = (String) iter.next();
            if (originInfo.containsKey(k)) {
		String v = (String)originInfo.get(k);
		
		// special case for name URLs!!
		if( "name".equals( k )) {
		    int idx = v.indexOf( "?" );
		    if( idx != -1 ) 
			v = v.substring( 0, idx );
		}

                ret.put(k, v );
            }
        }

        /*
        if( ret.size() == 0 ) {
            throw new PluginException( "component "
              + "originInfo doesn't have defining attributes!" );
        }
        */

        return ret;
    }
    
}
