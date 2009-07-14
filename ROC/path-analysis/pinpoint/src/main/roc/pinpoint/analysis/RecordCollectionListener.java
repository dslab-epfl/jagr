package roc.pinpoint.analysis;

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
     * @param items the records which were added to the collection
     */
    void addedRecords(String collectionName, List items);

    /**
     * called when records are removed from a collection
     * @param collectionName the name of the modified collection
     * @param items the records which were removed from the collection
     */
    void removedRecords(String collectionName, List items);

}
