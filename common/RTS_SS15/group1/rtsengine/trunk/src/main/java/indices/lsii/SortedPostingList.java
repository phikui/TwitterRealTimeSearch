package indices.lsii;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * This posting list is always sorted according to
 * sortKey __descending__ manner
 */

// TODO: Is this thread safe?
public class SortedPostingList extends LinkedList<SortedPostingListElement> {

    /**
     * This function inserts the tweetID sorted according to sortKey should be O(1) on already sorted lists
     *
     * @param tweetID
     * @param sortKey
     */
    public void insertSorted(int tweetID, float sortKey) {
        SortedPostingListElement sortElement = new SortedPostingListElement(tweetID, sortKey);
        ListIterator<SortedPostingListElement> iterator = listIterator();
        while(true) {
            if (!iterator.hasNext()) {
                iterator.add(sortElement);
                return;
            }

            SortedPostingListElement elementInList = iterator.next();
            if (elementInList.getSortKey() > sortElement.getSortKey()) {
                iterator.previous();
                iterator.add(sortElement);
                return;
            }
        }
    }


    /**
     *
     * @param tweetID
     * @param sortKey
     */
    // TODO: Method is never used
    public void removeFirstDuplicate(int tweetID, float sortKey){
        SortedPostingListElement sortElement = new SortedPostingListElement(tweetID, sortKey);
        ListIterator<SortedPostingListElement> iterator = listIterator();
        while(true){
            if (!iterator.hasNext()) {
                return;
            }
            SortedPostingListElement elementInList = iterator.next();
            if ((elementInList.getTweetID() == sortElement.getTweetID()) && (elementInList.getSortKey() < sortElement.getSortKey())) {
                this.remove(elementInList);
            }
        }
    }

    public boolean containsTweetID(int tweetID) {
        ListIterator<SortedPostingListElement> iterator = listIterator();

        while (true){
            if (!iterator.hasNext()) {
                return false;
            }

            SortedPostingListElement elementInList = iterator.next();

            if (elementInList.getTweetID() == tweetID) {
                return true;
            }
        }
    }

    public SortedPostingListElement getSortedPostingListElement(int tweetID) {
        ListIterator<SortedPostingListElement> iterator = listIterator();

        while (true){
            if (!iterator.hasNext()) {
                return null;
            }

            SortedPostingListElement elementInList = iterator.next();

            if (elementInList.getTweetID() == tweetID) {
                return elementInList;
            }
        }
    }

    /**
     * Returns TweetIDs stored in this PostingList as ArrayList (in same order)
     */
    public List<Integer> getTweetIDs() {
        List<Integer> tweetIDList = new ArrayList<Integer>(this.size());

        ListIterator<SortedPostingListElement> iterator = listIterator();

        while(true){
            if (!iterator.hasNext()) {
                break;
            }

            SortedPostingListElement elementInList = iterator.next();
            tweetIDList.add(elementInList.getTweetID());
        }

        return tweetIDList;
    }

}
