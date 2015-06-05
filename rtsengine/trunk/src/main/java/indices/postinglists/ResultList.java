package indices.postinglists;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * This list is always sorted according to sortKey __descending__ manner.
 * Used by the TPL and the LSII Index to store result tweetIDs along with there score value f.
 */
public class ResultList extends LinkedList<ResultListElement> {

    /**
     * This function inserts the tweetID sorted according to sortKey should be O(1) on already sorted lists
     *
     * @param tweetID
     * @param sortKey
     */
    public void insertSorted(int tweetID, float sortKey) {
        ResultListElement sortElement = new ResultListElement(tweetID, sortKey);
        ListIterator<ResultListElement> iterator = listIterator();
        while(true) {
            if (!iterator.hasNext()) {
                iterator.add(sortElement);
                return;
            }

            ResultListElement elementInList = iterator.next();
            if (elementInList.getSortKey() < sortElement.getSortKey()) {
                iterator.previous();
                iterator.add(sortElement);
                return;
            }
        }
    }

    public boolean containsTweetID(int tweetID) {
        ListIterator<ResultListElement> iterator = listIterator();

        while (true){
            if (!iterator.hasNext()) {
                return false;
            }

            ResultListElement elementInList = iterator.next();

            if (elementInList.getTweetID() == tweetID) {
                return true;
            }
        }
    }

    public ResultListElement getResultListElement(int tweetID) {
        ListIterator<ResultListElement> iterator = listIterator();

        while (true){
            if (!iterator.hasNext()) {
                return null;
            }

            ResultListElement elementInList = iterator.next();

            if (elementInList.getTweetID() == tweetID) {
                return elementInList;
            }
        }
    }

    /**
     * Returns TweetIDs stored in this ResultList as ArrayList (in same order)
     */
    public List<Integer> getTweetIDs() {
        List<Integer> tweetIDList = new ArrayList<Integer>(this.size());

        ListIterator<ResultListElement> iterator = listIterator();

        while(true){
            if (!iterator.hasNext()) {
                break;
            }

            ResultListElement elementInList = iterator.next();
            tweetIDList.add(elementInList.getTweetID());
        }

        return tweetIDList;
    }

}
