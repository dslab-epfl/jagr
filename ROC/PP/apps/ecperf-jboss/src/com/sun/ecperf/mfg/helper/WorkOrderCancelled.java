
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 */
package com.sun.ecperf.mfg.helper;


/*
 * WorkOrderCancelled marks the WorkOrder as cancelled. Only can be done
 * during WorkOrderCreated and WorkOrderStage1. Afterwards cancel is not
 * allowed. An IllegalStateException will be thrown in the other stages.
 * Called from WorkOrderCmpEJB.java in method update/finish.
 * @author Agnes Jacob
 * @see WorkOrderState
 * @see WorkOrderCmpEJB
 */

/**
 * Class WorkOrderCancelled
 *
 *
 * @author
 * @version %I%, %G%
 */
public class WorkOrderCancelled extends WorkOrderState {

    /**
     * Method remove
     *
     *
     */
    public void remove() {}

    /**
     * Method getStatus
     *
     *
     * @return
     *
     */
    public int getStatus() {
        return CANCELLED;
    }
}

