package features;

import indices.IndexDispatcher;
import iocontroller.IOController;
import model.TermDictionary;
import model.TransportObject;
import model.TweetDictionary;
import model.TweetObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by chans on 7/5/15.
 */
public class TweetListFromHashtag {

    /**
     * Tweets fetched from index
     */
    protected List<TweetObject> tweetObjectList;

    public static List<TransportObject> createAndGetTweetList(String hashtag, int k) {
        TransportObject queryObject = new TransportObject(hashtag, new Date(), k);

        // stem/preprocess hashtag
        List<String> stems;
        stems = IOController.stemmer.get().stem(queryObject.getText());
        queryObject.setTerms(stems);

        // write term list
        List<Integer> termIDs = new ArrayList<Integer>();
        for (String term : queryObject.getTerms()) {
            int id = TermDictionary.insertTerm(term);
            termIDs.add(id);
        }
        queryObject.setTermIDs(termIDs);

        // start the AO index query
        List<Integer> resultsIndex = IndexDispatcher.searchTweetIDsAO(queryObject);

        // create the tweetObject list
        List<TransportObject> resultTweets = new ArrayList<>();


        for (int index : resultsIndex) {
            TransportObject transportObject = TweetDictionary.getTransportObject(index);

            resultTweets.add(transportObject);
        }

        return resultTweets;
    }
}
