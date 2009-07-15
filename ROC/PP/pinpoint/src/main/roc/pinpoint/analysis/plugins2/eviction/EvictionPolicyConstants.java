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
package roc.pinpoint.analysis.plugins2.eviction;

// marked for release 1.0

/**
 * common constants for eviction policies
 * @author emrek
 *
 */
public interface EvictionPolicyConstants {

    /**
     * attribute name for the eviction policy of a record collection.
     */
    static final String EVICTION_POLICY_ARG = "evictionPolicy";

    /**
     * argument name for whether a given eviction plugin is the default (if so,
     * this plugin should apply itself to record collections that don't
     * explicitly choose another eviction policy.
     */
    static final String IS_DEFAULT_EVICTION_POLICY_ARG =
        "isDefaultEvictionPolicy";

}
