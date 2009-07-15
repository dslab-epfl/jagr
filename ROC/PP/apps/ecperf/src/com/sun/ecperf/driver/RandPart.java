/**
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: RandPart.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 * This class generates a randum part number
 *
 * @author Shanti Subramanyam
 */
package com.sun.ecperf.driver;
import java.lang.*;
import java.util.*;


public class RandPart {
	static final int A = 63;
	RandNum r;
	int PGS = 1;
	int numItems;
/*
 * Constructor
 */
	RandPart(RandNum r, int numItems, int scale) {
		this.r = r;
		this.numItems = numItems;
		this.PGS = scale;
	}

	/**
	 * This method generates a random part id to use for access
	 * to the item table in the orders database
	 *
	 * @return String part id
	 */
	public String getPart() {
	/*
	 * part_id is a combination of 5 numeric, 5 alpha,5 numeric
     * For manufactured parts, the part_id is of the form
     * <scale>"MITEM"<n> where n ranges from 1-10. (There are
     * 10 manufactured parts per PG)
	 */

		int p3 = r.NURand(A, 1, numItems);
		String p_id = prt(1) + "MITEM" + prt(p3);
		return(p_id);
	}


	String prt(int i)
	{
		Integer j = new Integer(i);
		if (i < 10)
			return("0000" + j.toString());
		else if ( i < 100)
			return("000" + j.toString());
		else if (i < 1000)
			return("00" + j.toString());
		else if (i < 10000)
			return("0" + j.toString());
		else
			return(j.toString());
	}
}


