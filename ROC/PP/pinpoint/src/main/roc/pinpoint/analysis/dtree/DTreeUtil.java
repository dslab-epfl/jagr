/**
    Copyright (C) 2004 Emre Kiciman and Stanford University

    This file is part of Pinpoint

    Pinpoint is free software; you can distribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 2.1 of the License, or
    (at your option) any later version.

    Pinpoint is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with Pinpoint; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
**/
package roc.pinpoint.analysis.dtree;

public class DTreeUtil {

    private DTreeUtil() {
    }

    public static double CalculateEntropy( int pos, int neg ) {

	if( pos < 0 || neg < 0 ) {
	    System.err.println( "!!!! ACK! calculating entropy on negative value: p=" + pos + "; n=" + neg );
	}

	if (pos == 0 || neg == 0)
	    return 0;
	
        int total = pos + neg;

        double p = (double) pos / total;
        double n = (double) neg / total;

        return (-p * Math.log(p) / Math.log(2.0))
            + (-n * Math.log(n) / Math.log(2.0));
    }

}
