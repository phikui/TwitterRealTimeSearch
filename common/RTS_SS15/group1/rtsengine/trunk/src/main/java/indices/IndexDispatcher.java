package indices;

import gui.ConfigurationObject;
import indices.lsii.AppendOnlyIndex;
import indices.lsii.LSIIIndex;
import indices.lsii.TriplePostingList;
import indices.lsii.TriplePostingListIndex;
import model.TransportObject;

import java.util.List;

/**
 * Created by phil on 17.05.2015.
 */
public class IndexDispatcher {

    private static final AppendOnlyIndex aoi_index = new AppendOnlyIndex();
    private static final TriplePostingListIndex tpl_index = new TriplePostingListIndex();
    private static final LSIIIndex lsii_index = new LSIIIndex();

    public static List<Integer> searchTweetIDs(TransportObject transportObjectQuery) {
        return getActiveIndex().searchTweetIDs(transportObjectQuery);
    }

    public static void insertTransportObject(TransportObject transportObjectInsertion) {
        getActiveIndex().insertTransportObject(transportObjectInsertion);
    }

    public static int size() {
        return getActiveIndex().size();
    }

    /**
     * Returns the currently active index
     * with regard to the ConfigurationObject.
     *
     * @return
     */
    private static IRTSIndex getActiveIndex() {
        if (ConfigurationObject.getIndexType() == "aoi") {
            return aoi_index;
        } else if (ConfigurationObject.getIndexType() == "tpl") {
            return tpl_index;
        } else {
            return lsii_index;
        }
    }
}
