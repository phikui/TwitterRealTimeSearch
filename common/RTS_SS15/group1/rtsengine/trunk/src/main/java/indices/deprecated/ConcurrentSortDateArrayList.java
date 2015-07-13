package indices.deprecated;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Maik on 28.05.2015.
 */
public class ConcurrentSortDateArrayList extends CopyOnWriteArrayList<ConcurrentSortedDateListElement>{

    /*

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
