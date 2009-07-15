
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 */
package com.sun.ecperf.mfg.helper;


/*
 * WorkOrderStage1 is the second state or stage of a process.
 * Called from WorkOrderCmpEJB.java in update() function
 * @author Agnes Jacob
 * @see WorkOrderState
 * @see WorkOrderCmpEJB
 */

/**
 * Class WorkOrderStage1
 *
 *
 * @author
 * @version %I%, %G%
 */
public class WorkOrderStage1 extends WorkOrderState {

    /**
     * @return the object of the
     * next stage in the process which is WorkOrderStage2
     */
    public WorkOrderState nextState() {
        return (getInstance(STAGE2));
    }

    /**
     * @return the object of the WorkOrderCancelled object
     */
    public WorkOrderState cancel() {
        return (getInstance(CANCELLED));
    }

    /**
     * @return the status of this state which is STAGE1
     */
    public int getStatus() {
        return STAGE1;
    }
}

