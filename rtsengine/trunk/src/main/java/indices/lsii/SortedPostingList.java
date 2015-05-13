package indices.lsii;

import java.util.LinkedList;

/**
 * This posting list is always sorted according to
 * sortKey __descending__ manner
 */

// TODO: Is this thread safe?
public class SortedPostingList extends LinkedList<SortedPostingListElement> {

    /**
     * This function inserts the tweetID sorted according to sortKey
     *
     * @param tweetID
     * @param sortKey
     */
    public void insertSorted(int tweetID, int sortKey) {
        // TODO: implement
    }

}
