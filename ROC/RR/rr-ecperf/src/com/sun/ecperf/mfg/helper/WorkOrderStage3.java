
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 */
package com.sun.ecperf.mfg.helper;


/*
 * WorkOrderStage3 is the fourth state or stage of a process.
 * Called from WorkOrderCmpEJB.java in method update.
 * @author Agnes Jacob
 * @see WorkOrderState
 * @see WorkOrderCmpEJB
 */

/**
 * Class WorkOrderStage3
 *
 *
 * @author
 * @version %I%, %G%
 */
public class WorkOrderStage3 extends WorkOrderState {

    /**
     * @return the object of the
     * next stage in the process which is WorkOrderFinished
     */
    public WorkOrderState nextState() {
        return (getInstance(COMPLETED));
    }

    /**
     * @return the object of the
     * next stage in the process which is WorkOrderFinished
     */
    public WorkOrderState finish() {
        return (this.nextState());
    }

    /**
     * @return the status of this state which is STAGE3
     */
    public int getStatus() {
        return STAGE3;
    }
}

