
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: SequenceBlock.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */
package com.sun.ecperf.util.helper;


import com.sun.ecperf.util.sequenceent.ejb.*;


/**
 * Class SequenceBlock
 *
 *
 * @author
 * @version %I%, %G%
 */
public class SequenceBlock implements java.io.Serializable {

    public int                   nextNumber;
    public int                   ceiling;
    public transient SequenceEnt sequence;
}

