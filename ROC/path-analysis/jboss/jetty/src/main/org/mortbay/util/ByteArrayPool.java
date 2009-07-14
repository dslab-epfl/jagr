// ===========================================================================
// Copyright (c) 2002 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: ByteArrayPool.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
// ---------------------------------------------------------------------------

package org.mortbay.util;
import java.util.ArrayList;

/* ------------------------------------------------------------ */
/** Byte Array Pool
 * Simple pool for recycling byte arrays of a fixed size.
 *
 * @version $Id: ByteArrayPool.java,v 1.1.1.1 2002/11/16 03:16:49 mikechen Exp $
 * @author Greg Wilkins (gregw)
 */
public class ByteArrayPool
{
    public static final int __POOL_SIZE=
        Integer.getInteger("org.mortbay.util.ByteArrayPool.pool_size",20).intValue();
    public static final int __MAX_POOLS=
        Integer.getInteger("org.mortbay.util.ByteArrayPool.max_pools",5).intValue();
    
    public static final ArrayList __pools=new ArrayList(__MAX_POOLS);
    public static int __lastSize=4096;
    
    /* ------------------------------------------------------------ */
    /** Get byte array from pool of any size.
     * @return Byte array of any size.
     */
    public static byte[] getByteArray()
    {
        return getByteArray(__lastSize);
    }
    
    /* ------------------------------------------------------------ */
    /** Get a byte array from the pool of known size.
     * @param size Size of the byte array.
     * @return Byte array of known size.
     */
    public static byte[] getByteArray(int size)
    {
        __lastSize=size;

        for (int i=0;i<__pools.size();i++)
        {
            Pool pool = (Pool)__pools.get(i);
            if (size==pool._bufSize)
                return pool.getByteArray();
        }

        return new byte[size];
    }

    /* ------------------------------------------------------------ */
    public static synchronized void returnByteArray(byte[] b)
    {
        if (b==null || b.length==0)
            return;
        
        __lastSize=b.length;

        for (int i=0;i<__pools.size();i++)
        {
            Pool pool = (Pool)__pools.get(i);
            if (b.length==pool._bufSize)
            {
                pool.returnByteArray(b);
                return;
            }
        }

        if (__pools.size()<__MAX_POOLS)
        {
            Pool pool=new Pool(b.length);
            pool.returnByteArray(b);
            __pools.add(pool);
        }
    }
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private static class Pool
    {
        int _bufSize;
        byte[][] _pool = new byte[__POOL_SIZE][];
        int _in;
        int _out;
        int _size;

        /* ------------------------------------------------------------ */
        Pool(int size)
        {
            _bufSize=size;
            Code.debug("New byte[] pool. size="+size);
        }
        
        /* ------------------------------------------------------------ */
        synchronized byte[] getByteArray()
        {
            if (_size>0)
            {
                byte[] b = _pool[_out++];
                if (_out>=_pool.length)
                    _out=0;
                _size--;
                
                return b;           
            }
            
            return new byte[_bufSize];
        }
        
        /* ------------------------------------------------------------ */
        synchronized void returnByteArray(byte[] b)
        {
            if (b==null)
                return;
            
            if (_size<_pool.length)
            {
                _pool[_in++]=b;
                if (_in>=_pool.length)
                    _in=0;
                _size++;
            }
        }
    }
}
