package iocontroller;

import model.TransportObject;
import model.TweetObject;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by phil on 16.05.15.
 */
public class PreprocessorRawObject implements Callable<TransportObject> {
    private final Date timestamp;

    private final int k;

    // for determining if the object is a query or not
    private final boolean isQuery;


    //The tweet Content if it is a tweet
    private final TweetObject tweet;


    //The query if it is a query
    private final String query;

    //Tweet constructor
    public PreprocessorRawObject(TweetObject tweet) {
        isQuery = false;
        this.tweet = tweet;
        timestamp = tweet.getTimestamp();

        //non relevant fields
        k = 0;
        query = null;

    }

    //query constructor
    public PreprocessorRawObject(String queryString, int k, Date timestamp) {
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
        //TODO change trivial stem
        //distinguish between query and tweet
        if (!isQuery) {
            result = new TransportObject(tweet);
            List<String> stems;
            if (IOController.useStandfordStemmer) {
                //
            } else {
                stems = Stemmer.trivial_stem(tweet.getText());
            }
            result.setTerms(stems);


        } else {
            result = new TransportObject(query, timestamp, k);
            List<String> stems;
            if (IOController.useStandfordStemmer) {
                //
            } else {
                stems = Stemmer.trivial_stem(query);
            }
            result.setTerms(stems);


        }


        return result;
    }

    public TweetObject getTweet() {
        return tweet;
    }

}
