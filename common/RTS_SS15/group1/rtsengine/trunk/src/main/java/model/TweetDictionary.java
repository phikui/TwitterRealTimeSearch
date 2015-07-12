package model;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import sun.plugin2.message.transport.Transport;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maps tweetIDs to transportObjects
 */
public class TweetDictionary {

    private static Map<Integer, TransportObject> tweetDictionary;

    private static int tweetIDCounter;

    // Objects for storing inserted tweet directly in MapDB
    private static DB mapDB;
    private static HTreeMap<Integer, TweetObject> tweetObjectsMapDB;

    static {
        tweetIDCounter = 0;
        tweetDictionary = new ConcurrentHashMap<Integer, TransportObject>();

        // Init and load MapDB for storing of TweetObjects
        mapDB = DBMaker.newFileDB(new File("tweetdb"))
                .closeOnJvmShutdown()
                .make();
        tweetObjectsMapDB = mapDB.getHashMap("tweetObjects");
    }

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

        // Also store inserted tweet Object in MapDB
        storeTweetObjectInMapDB(transportObject.getTweetObject(), tweetID);

        return tweetID;
    }

    private static void storeTweetObjectInMapDB(TweetObject tweetObject, int tweetID) {
        // Also store tweet Object in MapDB
        tweetObjectsMapDB.put(tweetID, tweetObject);

//        System.out.println("Inserted new tweet into MapDB after collecting " + tweetID + " tweets");

        // Commit MapDB every 100 tweets
        if (tweetID % 100 == 0) {
            mapDB.commit();
            System.out.println("Commited MapDB after collecting " + tweetID + " tweets");
        }
    }

    public static int size() {
        return tweetDictionary.size();
    }
}
