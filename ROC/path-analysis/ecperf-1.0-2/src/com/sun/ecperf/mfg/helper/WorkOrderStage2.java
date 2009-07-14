
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 */
package com.sun.ecperf.mfg.helper;


/*
 * WorkOrderStage2 is the third state or stage of a process.
 * Called from WorkOrderCmpEJB.java in update() function
 * @author Agnes Jacob
 * @see WorkOrderState
 * @see WorkOrderCmpEJB
 */

/**
 * Class WorkOrderStage2
 *
 *
 * @author
 * @version %I%, %G%
 */
public class WorkOrderStage2 extends WorkOrderState {

    /**
     * @return the object of the
     * next stage in the process which is WorkOrderStage3
     */
    public WorkOrderState nextState() {
        return (getInstance(STAGE3));
    }

    /**
     * @return the status of this state which is STAGE2
     */
    public int getStatus() {
        return STAGE2;
    }
}

