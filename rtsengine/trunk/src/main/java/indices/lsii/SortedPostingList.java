package indices.lsii;

import java.util.LinkedList;
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

    public boolean containsTweetID(int tweetID, float sortKey){
        SortedPostingListElement sortElement = new SortedPostingListElement(tweetID, sortKey);
        ListIterator<SortedPostingListElement> iterator = listIterator();
        while(true){
            if (!iterator.hasNext()) {
                return false;
            }
            SortedPostingListElement elementInList = iterator.next();
            if ((elementInList.getTweetID() == sortElement.getTweetID())/* && (elementInList.getSortKey() > sortElement.getSortKey())*/) {
                return true;
            }
        }

    }

}
