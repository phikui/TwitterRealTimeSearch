package indices.postingarraylists;

import indices.postinglists.ConcurrentSortedDateListElement;

import java.util.Date;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Maik on 28.05.2015.
 */
public class ConcurrentSortDateArrayList extends CopyOnWriteArrayList<ConcurrentSortedDateListElement>{

    /*
    // TODO pretty sure this method is not threadsafe right now
    public void insertSorted(int tweetID, Date sortKey) {
        ConcurrentSortedDateListElement sortElement = new ConcurrentSortedDateListElement(tweetID, sortKey);
        ListIterator<ConcurrentSortedDateListElement> iterator = listIterator();
        while(true) {
            if (!iterator.hasNext()) {
                iterator.add(sortElement);
                return;
            }

            ConcurrentSortedDateListElement elementInList = iterator.next();
            if (elementInList.getTimestamp().getTime() > sortElement.getTimestamp().getTime()) {
                iterator.previous();
                iterator.add(sortElement);
                return;
            }
        }
    }*/
}
