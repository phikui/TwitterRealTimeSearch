package indices.deprecated;

import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by Maik on 21.05.2015.
 *
 * needed as we can not store a fValue for the freshness score. Date used in query function in freshness calculation
 */
public class ConcurrentSortedDateList extends ConcurrentSkipListSet<ConcurrentSortedDateListElement> {
}
