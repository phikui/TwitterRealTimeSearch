package model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maps tweetIDs to transportObjects
 */
public class TweetDictionary {


    private static Map<Integer, TransportObject> tweetDictionary = new ConcurrentHashMap<Integer, TransportObject>();

    private static int tweetIDCounter;


    /**
     * Returns the stored transportObject for tweetID
     *
     * @param   tweetID
     *
     * @return  TransportObject
     */
    public static TransportObject getTransportObject(int tweetID) {
        return tweetDictionary.get(tweetID);
    }


    public static Map<Integer, TransportObject> getTweetDictionary() {
        return tweetDictionary;
    }

    /**
     * Inserts transportObject into the dictionary.
     * A tweetID is chosen automatically and returned.
     *
     * Does not check whether a transportObject has already been inserted,
     * it will always insert tweets.
     *
     * @param  transportObject
     *
     * @return tweetID
     */
    public static int insertTransportObject(TransportObject transportObject) {
        int tweetID = tweetIDCounter;
        tweetIDCounter++;

        tweetDictionary.put(tweetID, transportObject);


        return tweetID;
    }



    public static int size() {
        return tweetDictionary.size();
    }
}
