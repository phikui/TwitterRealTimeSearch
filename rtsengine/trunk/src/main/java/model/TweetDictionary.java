package model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maps tweetIDs to actual tweetObjects
 */
public class TweetDictionary {

    private static Map<Integer, TweetObject> tweetDictionary;

    private static int tweetIDCounter;

    static {
        tweetIDCounter = 0;
        tweetDictionary = new ConcurrentHashMap<Integer, TweetObject>();
    }

    /**
     * Returns the stored tweetObject for tweetID
     *
     * @param   tweetID
     *
     * @return  TweetObject
     */
    public static TweetObject getTweetObject(int tweetID) {
        return tweetDictionary.get(tweetID);
    }

    /**
     * Inserts tweetObject into the dictionary.
     * A tweetID is chosen automatically and returned.
     *
     * Does not check whether a tweetObject has already been inserted,
     * it will always insert tweets.
     *
     * @param  tweetObject
     *
     * @return tweetID
     */
    public static int insertTweetObject(TweetObject tweetObject) {
        int tweetID = tweetIDCounter;
        tweetIDCounter++;

        tweetDictionary.put(tweetID, tweetObject);

        return tweetID;
    }

    public static int size() {
        return tweetDictionary.size();
    }

}
