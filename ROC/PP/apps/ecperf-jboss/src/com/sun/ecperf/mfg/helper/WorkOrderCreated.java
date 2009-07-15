
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 */
package com.sun.ecperf.mfg.helper;


/*
 * WorkOrderCreated is the first state or stage of a process.
 * Called from WorkOrderCmpEJB.java when a WorkOrder is created or in process.
 * @author Agnes Jacob
 * @see WorkOrderState
 * @see WorkOrderCmpEJB
 */

/**
 * Class WorkOrderCreated
 *
 *
 * @author
 * @version %I%, %G%
 */
public class WorkOrderCreated extends WorkOrderState {

    /**
     * @return the object of the
     * next stage in the process which is WorkOrderStage1
     */
    public WorkOrderState nextState() {
        return (getInstance(STAGE1));
    }

    /**
     * @return the object of the WorkOrderCancelled object
     */
    public WorkOrderState cancel() {
        return (getInstance(CANCELLED));
    }

    /**
     * @return the object of the
     * next stage in the process which is WorkOrderStage1
     */
    public WorkOrderState process() {
        return (this.nextState());
    }

    /**
     * @return the status of this state which is OPEN.
     */
    public int getStatus() {
        return OPEN;
    }
}

