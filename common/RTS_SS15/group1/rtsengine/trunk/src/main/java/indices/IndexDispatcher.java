package indices;

import indices.lsii.AppendOnlyIndex;
import model.TransportObject;

/**
 * Created by phil on 17.05.2015.
 */
public class IndexDispatcher {

    private static final AppendOnlyIndex ao_index = new AppendOnlyIndex();

    public static int size() {
        return ao_index.size();
    }
    public static void insert(TransportObject tweet) {
        ao_index.insertTransportObject(tweet);
    }
}
