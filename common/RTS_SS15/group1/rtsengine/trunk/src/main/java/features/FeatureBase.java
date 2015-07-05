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
abstract public class FeatureBase {

    /**
     * Tweets fetched from index
     */
    protected List<TweetObject> tweetObjectList;

    protected void createAndGetTweetList(String hashtag, int k) {
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
        List<TweetObject> resultTweets = new ArrayList<>();
        for (int index : resultsIndex) {
            resultTweets.add(TweetDictionary.getTransportObject(index).getTweetObject());
        }

        this.tweetObjectList = resultTweets;
    }
}
