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
package roc.pinpoint.analysis.timeseries;

/**
 * This class represents a DiscreteTimeSeries.  (Basically a String)
 * and also provides a helper function for generating a SuffixTree
 * from the DiscreteTimeSeries.
 *
 */
public class DiscreteTimeSeries {

    byte[] series;
    int start;
    int length;

    public DiscreteTimeSeries( byte[] series ) {
	this( series, 0, series.length );
    }

    
    public DiscreteTimeSeries( byte[] series, int start, int length ) {
	this.series = series;
	this.start = start;
	this.length = length;
    }

    public byte[] getSeries() {
	return series;
    }

    public int getStart() {
	return start;
    }

    public int getLength() {
	return length;
    }

    public SuffixTree getSuffixTree() {
	return new SuffixTree( series, start, length );
    }

    public String toString( ) {
	byte[] ret = new byte[ length ];
	byte base = (byte)'a';
	for( int i=0; i<length; i++ ) {
	    ret[i] = (byte)(base + series[ start + i ]);
	}

	return new String( ret );
    }

}
