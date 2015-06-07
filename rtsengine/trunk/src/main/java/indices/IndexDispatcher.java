package indices;

import indices.aoi.AOIIndex;
import indices.lsii.LSIIIndex;
import indices.tpl.TPLIndex;
import model.ConfigurationObject;
import model.TransportObject;

import java.util.List;

/**
 * Created by phil on 17.05.2015.
 */
public class IndexDispatcher {

    private static final AOIIndex aoi_index = new AOIIndex();
    private static final TPLIndex tpl_index = new TPLIndex();
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
        if (ConfigurationObject.getIndexType() == ConfigurationObject.IndexTypes.APPEND_ONLY) {
            return aoi_index;
        } else if (ConfigurationObject.getIndexType() == ConfigurationObject.IndexTypes.TRIPLE_POSTING_LIST) {
            return tpl_index;
        } else if (ConfigurationObject.getIndexType() == ConfigurationObject.IndexTypes.LSII) {
            return lsii_index;
        } else {
            return null;
        }
    }
}
