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
package roc.pinpoint.tracing;

// marked for release 1.0

/**
 *  This interface should be implemented to create a custom protocol
 *  for reporting observations from an observation point in a system
 *  to a Pinpoint analysis engine.  The ObservationSubscriber is used
 *  in the analysis engine (by the
 *  roc.pinpoint.analysisengine.plugins2.ObservationCollector plugin)
 *  to receive observations.
 *  <p> For each protocol, there should also be a matching
 *  implementation of ObservationPublisher.
 *
 * @author emrek@cs.stanford.edu
 *
 */
public interface ObservationSubscriber {

    /**
     * listen for an observation.  this is a blocking call, and might not
     * return if no observation is received.
     * @return Observation the next observation
     * @throws ObservationException an error occurred while reading the next
     * observation
     */
    Observation receive() throws ObservationException;

}
