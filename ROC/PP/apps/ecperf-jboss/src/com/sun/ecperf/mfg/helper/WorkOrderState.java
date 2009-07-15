
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 */
package com.sun.ecperf.mfg.helper;


/*
 * This class is the abstract classes for the different states in the workOrder
 * process.  If the subclasses do not have some of the methods defined
 * in this class then an IllegalStateException is thrown indicating
 * that the desired state changed is not legal for that particular state.
 * @author Agnes Jacob
 */

/**
 * Class WorkOrderState
 *
 *
 * @author
 * @version %I%, %G%
 */
public abstract class WorkOrderState implements WorkOrderStateConstants {

    public static final int NUMSTATES = 5;

    /**
     * Method getInstance
     *
     *
     * @param status
     *
     * @return
     *
     */
    public static WorkOrderState getInstance(int status) {

        switch (status) {

        case OPEN :
            return (new WorkOrderCreated());

        case STAGE1 :
            return (new WorkOrderStage1());

        case STAGE2 :
            return (new WorkOrderStage2());

        case STAGE3 :
            return (new WorkOrderStage3());

        case COMPLETED :
            return (new WorkOrderFinished());

        case CANCELLED :
            return (new WorkOrderCancelled());

        default :
            throw new IllegalStateException("Unknown State " + status);
        }
    }

    /**
     * Method nextState
     *
     *
     * @return
     *
     */
    public WorkOrderState nextState() {
        throw new IllegalStateException();
    }

    /**
     * Method process
     *
     *
     * @return
     *
     */
    public WorkOrderState process() {
        throw new IllegalStateException();
    }

    /**
     * Method cancel
     *
     *
     * @return
     *
     */
    public WorkOrderState cancel() {
        throw new IllegalStateException();
    }

    /**
     * Method finish
     *
     *
     * @return
     *
     */
    public WorkOrderState finish() {
        throw new IllegalStateException();
    }

    /**
     * Method remove
     *
     *
     */
    public void remove() {
        throw new IllegalStateException();
    }

    /**
     * Method getStatus
     *
     *
     * @return
     *
     */
    public abstract int getStatus();
}

