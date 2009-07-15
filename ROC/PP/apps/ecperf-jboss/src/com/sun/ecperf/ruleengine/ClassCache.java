
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: ClassCache.java,v 1.1 2004/02/19 14:45:09 emrek Exp $
 *
 */
package com.sun.ecperf.ruleengine;


import java.util.*;


/**
 * Class ClassCache
 *
 *
 * @author
 * @version %I%, %G%
 */
public class ClassCache {

    HashMap    cache;
    TimeoutSet blackList;
    TimeoutRef cacheRef;

    /**
     * Constructor ClassCache
     *
     *
     */
    public ClassCache() {

        cache     = new HashMap();
        blackList = new TimeoutSet(300000);    // 5 minute timeout
        cacheRef  = new TimeoutRef(10000);     // 10 seconds cache timeout
    }

    /**
     * Method forName
     *
     *
     * @param name
     *
     * @return
     *
     * @throws ClassNotFoundException
     *
     */
    public CachedClass forName(String name) throws ClassNotFoundException {

        CachedClass cl = (CachedClass) cache.get(name);

        if (cl == null) {
            if (blackList.contains(name)) {
                throw new ClassNotFoundException(name + "is in black list");
            } else {
                try {
                    cl = new CachedClass(name, cacheRef);

                    cache.put(name, cl);
                } catch (ClassNotFoundException e) {
                    blackList.add(name);

                    throw e;
                }
            }
        }

        return cl;
    }

    /**
     * Method entry to be used if a class instance exists. So
     * it won't need class search.
     *
     * @param clazz Instance of the class
     *
     * @return
     *
     *
     */
    public CachedClass entry(Class clazz) {

        String name = clazz.getName();

        CachedClass cl = (CachedClass) cache.get(name);

        if (cl == null) {
            cl = new CachedClass(cacheRef, clazz);
            cache.put(name, cl);
        }

        return cl;
    }

    /**
     * Method clear
     *
     *
     */
    public void clear() {
        cache.clear();
    }
}

