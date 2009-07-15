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
package roc.pinpoint.analysis;

// marked for release 1.0

import java.util.List;

/**
 * This interface should be implemented to receive callbacks when a record
 * collection is modified.  Implementing classes must also register themselves
 * with the record collection they are interested in.
 * @author emrek
 *
 */
public interface RecordCollectionListener {

    /**
     * called when records have been added to a collection
     * @param collectionName the name of the modified collection
     * @param item the record which was added to the collection
     */
    void addedRecord(String collectionName, Record item);

    /**
     * called when records are removed from a collection
     * @param collectionName the name of the modified collection
     * @param items the records which were removed from the collection
     */
    void removedRecords(String collectionName, List items);

}
