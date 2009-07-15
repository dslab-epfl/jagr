
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: TimeoutSet.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.ruleengine;


import java.util.*;


/**
 * Class TimeoutSet
 *
 *
 * @author
 * @version %I%, %G%
 */
public class TimeoutSet {

    HashMap list;
    long    timeout;

    /**
     * Constructor TimeoutSet
     *
     *
     * @param timeout
     *
     */
    public TimeoutSet(long timeout) {
        this.timeout = timeout;
        list         = new HashMap();
    }

    /**
     * Method add
     *
     *
     * @param x
     *
     * @return
     *
     */
    public Object add(Object x) {
        return list.put(x, new Long(System.currentTimeMillis()));
    }

    /**
     * Method contains
     *
     *
     * @param x
     *
     * @return
     *
     */
    public boolean contains(Object x) {

        boolean ret     = true;
        Long    insTime = (Long) list.get(x);

        if (insTime == null) {
            ret = false;
        } else if (System.currentTimeMillis() - insTime.longValue()
                   > timeout) {
            ret = false;

            list.remove(x);
        }

        return ret;
    }
}

