/*
 * $Id: TTL.java,v 1.1 2004/06/08 13:33:04 emrek Exp $
 *
 * TTL: Hash map of time to live (TTL) for each jar file
 *
 */
package roc.rr.afpi;

import java.util.HashMap;
import java.util.Set;

public class TTL {
    private HashMap ttlMap;

    public TTL() {
	ttlMap = new HashMap();
    }

    public void add(String jarName, long interval){
	ttlMap.put((Object)jarName,new Long(interval));
    }

    public long get(String jarName){
	Long TTL = (Long)ttlMap.get((Object)jarName);
	return TTL.longValue();
    }

    public Set getAllJarName(){
	return ttlMap.keySet();
    }
}
