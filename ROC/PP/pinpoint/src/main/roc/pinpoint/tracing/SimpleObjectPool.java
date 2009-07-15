package roc.pinpoint.tracing;

import java.util.*;

public class SimpleObjectPool {

    public static SimpleObjectPool HASHMAP_POOL = new SimpleObjectPool( java.util.HashMap.class, 25000 );

    static final int MAX_CAPACITY = 25000;

    private Class objType;
    private ArrayList pool;
    
    public SimpleObjectPool( Class objType, int initialPoolCapacity ) {
	this.objType = objType;
	pool = new ArrayList(2000);
    }

    public synchronized Object get() {
	Object ret = null;
	if( pool.size() > 0 ) {
	    ret = pool.remove( pool.size() - 1 );
	}
	else {
	    try {
		ret = objType.newInstance();
	    }
	    catch( Exception ex ) {
		throw new RuntimeException( "unable to create new object in pool!", ex );
	    }
	}
	return ret;
    }

    /**
     *  Objects must be cleared before being put back into the pool
     */
    public synchronized void put( Object o ) {
	if( !objType.isInstance( o )) {
	    throw new ClassCastException( "argument not of type: " + objType );
	}
	if( pool.size() < MAX_CAPACITY )
	    pool.add( o );
    }


}
