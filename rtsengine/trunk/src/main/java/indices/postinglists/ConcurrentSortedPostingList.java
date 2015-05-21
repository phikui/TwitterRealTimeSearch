package indices.postinglists;


import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by Maik on 21.05.2015.
 *
 * ConcurrentSkipListSet
 * A scalable concurrent NavigableSet implementation based on a ConcurrentSkipListMap.
 * The elements of the set are kept sorted according to their natural ordering,
 * or by a Comparator provided at set creation time, depending on which constructor is used.
 */
public class ConcurrentSortedPostingList extends ConcurrentSkipListSet<ConcurrentSortedPostingListElement> {
}
