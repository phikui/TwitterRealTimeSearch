package indices.deprecated;

import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Maik on 28.05.2015.
 */
public class ConcurrentSortedArrayList extends CopyOnWriteArrayList<ConcurrentSortedPostingListElement> {


    public void insertSorted(int tweetID, float sortKey) {
        ConcurrentSortedPostingListElement sortElement = new ConcurrentSortedPostingListElement(tweetID, sortKey);
        ListIterator<ConcurrentSortedPostingListElement> iterator = listIterator();
        while(true) {
            if (!iterator.hasNext()) {
                iterator.add(sortElement);
                return;
            }

            ConcurrentSortedPostingListElement elementInList = iterator.next();
            if (elementInList.getSortKey() > sortElement.getSortKey()) {
                iterator.previous();
                iterator.add(sortElement);
                return;
            }
        }
    }

    public boolean containsTweetID(int tweetID, float sortKey){
        ConcurrentSortedPostingListElement sortElement = new ConcurrentSortedPostingListElement(tweetID, sortKey);
        ListIterator<ConcurrentSortedPostingListElement> iterator = listIterator();
        while(true){
            if (!iterator.hasNext()) {
                return false;
            }
            ConcurrentSortedPostingListElement elementInList = iterator.next();
            if ((elementInList.getTweetID() == sortElement.getTweetID()) && (elementInList.getSortKey() > sortElement.getSortKey())) {
                return true;
            }
        }

    }
}
