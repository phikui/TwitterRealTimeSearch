package indices;

import model.TransportObject;
import model.TweetObject;

import java.util.ArrayList;
import java.util.Date;

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
     * @param   terms        Query terms
     * @param   timestamp    Query timestamp
     * @param   k            Number of Tweet IDs to return
     *
     * @return  ArrayList<Integer>
     */
    public ArrayList<Integer> searchTweetIDs(String terms, Date timestamp, int k);

    /**
     * Inserts the tweetID transported in transportObject into the index.
     *
     * TransportObjects contain all information necessary to index the transported
     * TweetObject
     *
     * @param tweetID
     */
    public void insertTransportObject(TransportObject transportObject);

}
