package indices;

import model.TransportObject;
import java.util.List;


/**
 * RTSIndices index tweetIDs (Integers) according
 * to termIDs (Integers) and ranking factors (timestamp, significance).
 *
 * TermIDs translate to actual terms via TermDictionary in our model
 * TweetIDs translate to actual tweetObjects via TweetDictionary in our model
 *
 * Insertion into indices is only done via already pre-processed TransportObjects,
 * see below.
 */
public interface IRTSIndex {

    /**
     * Returns top k tweetIDs according to ranking function f
     *
     * @param   transportObject     isQuery transport object
     * @param   k            Number of Tweet IDs to return
     *
     * @return  ArrayList<Integer>
     */
    public List<Integer> searchTweetIDs(TransportObject transportObjectQuery);

    /**
     * Inserts the tweetID transported in transportObject into the index.
     *
     * TransportObjects contain all information necessary to index the transported
     * TweetObject
     *
     * @param tweetID
     */
    public void insertTransportObject(TransportObject transportObjectInsertion);

}
