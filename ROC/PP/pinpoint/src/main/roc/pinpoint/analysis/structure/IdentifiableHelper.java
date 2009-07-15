/*
 * Created on Apr 3, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package roc.pinpoint.analysis.structure;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author emrek
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class IdentifiableHelper {

    public static String IdToString( Identifiable id ) {
        return MapToString(id.getId());
    }
    
    public static String MapToString( Map map ) {
        StringBuffer buf = new StringBuffer();
        
        buf.append("{");
        Iterator iter = map.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            buf.append(key).append("=").append(map.get(key));
        }
        buf.append("}");
        return buf.toString();
    }
    
    public static String MapToString( Map map, Collection keys ) {
        StringBuffer buf = new StringBuffer();
        
        buf.append("{");
        
        Iterator iter = keys.iterator();
        while( iter.hasNext() ) {
            String key = (String)iter.next();
            if( map.containsKey( key )) {
                buf.append(key).append("=").append(map.get(key));
            }
        }

        buf.append("}");
        return buf.toString();
    }
    
    public static Map ReduceMap( Map m, Collection keys ) {
        Map attrs = new HashMap();
        Iterator iter = keys.iterator();
        while( iter.hasNext() ) {
            Object k = iter.next();
            if( m.containsKey( k )) {
                attrs.put( k, m.get( k ));
            }
        }

        return attrs;
    }
    
}
