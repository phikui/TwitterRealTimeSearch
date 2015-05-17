package iocontroller;

import model.TransportObject;
import model.TweetObject;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by phil on 16.05.15.
 */
public class PreprocessingRawObject implements Callable<TransportObject> {
    private final Date timestamp;

    private final int k;

    // for determining if the object is a query or not
    private final boolean isQuery;

    //The tweet Content if it is a tweet
    private final TweetObject tweet;


    //The query if it is a query
    private final String query;

    //Tweet constructor
    public PreprocessingRawObject(TweetObject tweet) {
        isQuery = false;
        this.tweet = tweet;
        timestamp = tweet.getTimestamp();

        //non relevant fields
        k = 0;
        query = null;

    }

    //query constructor
    public PreprocessingRawObject(String queryString, int k, Date timestamp) {
        isQuery = true;
        query = queryString;
        this.k = k;
        this.timestamp = timestamp;

        //non relevant fields
        tweet = null;
    }


    //Code for prepossessing
    public TransportObject call() throws Exception {
        TransportObject result;

        //distinguish betweeen query and tweet
        if (!isQuery) {
            result = new TransportObject(tweet);
            /*
            Code for stemming
             */
            List<String> stems = null;
            result.setTerms(stems);


        } else {
            result = new TransportObject(query, timestamp, k);
             /*
            Code for stemming
             */
            List<String> stems = null;
            result.setTerms(stems);


        }


        return result;
    }
}
