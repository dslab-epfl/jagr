
/*
 * Copyright (c) 1998-2002 by Sun Microsystems, Inc. All Rights Reserved.
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 * $Id: Rounding.java,v 1.2 2003/03/22 04:55:01 emrek Exp $
 *
 */

package com.sun.ecperf.common;

import java.lang.Math;

public class Rounding
{
  public static double round(double d, int place)
  {
    int i = 0;
    int j = 0;
    int s = 1;
    if (place <= 0)
      return (int)(d +((d > 0)? 0.5 : -0.5));
    if (d < 0)
      {
	d = -d;
        s = -1;
      }
    d += 0.5*Math.pow(10,-place);
    if (d > 1)
      {
	i = (int)d;
	d -= i;
      }
      
    if (d > 0)
      {
	j = (int)(d*Math.pow(10,place));
        d = i + (double)(j/Math.pow(10,place));
      }
    d *= s;
    return d;
  }

  public static void main (String[] arguments)
  {
    double d = (new Double(arguments[0])).doubleValue();
    int p = (new Integer(arguments[1])).intValue();
    System.out.println("    internal "+d);
    System.out.println();
    System.out.println("rounded to "+ p + " is " +Rounding.round(d,p));
  }
}


