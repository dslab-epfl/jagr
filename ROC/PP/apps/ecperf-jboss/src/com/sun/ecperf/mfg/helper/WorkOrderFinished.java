
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 */
package com.sun.ecperf.mfg.helper;


/*
 * WorkOrderFinish is the final state or stage of a process.
 * Called from WorkOrderCmpEJB.java in method update/finish.
 * @author Agnes Jacob
 * @see WorkOrderState
 * @see WorkOrderCmpEJB
 */

/**
 * Class WorkOrderFinished
 *
 *
 * @author
 * @version %I%, %G%
 */
public class WorkOrderFinished extends WorkOrderState {

    /**
     * Indication that a remove is possible from this state.
     */
    public void remove() {}

    /**
     * @return the status of this state which is STAGE3
     */
    public int getStatus() {
        return COMPLETED;
    }
}

