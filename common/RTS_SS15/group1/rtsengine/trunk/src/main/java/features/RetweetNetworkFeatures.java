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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chans on 7/5/15.
 */
public class RetweetNetworkFeatures {

    protected List<TweetObject> tweetObjectList;

    private static Pattern retweetPattern = Pattern.compile("(?i)\\brt\\s*@(\\w+)");

    public int getNumberOfNodes(String hashtag) {
        int numberOfTweets = 10000;
        this.createAndGetTweetList(hashtag, numberOfTweets);

        for (int i = 0; i < tweetObjectList.size(); i++) {
            TweetObject to = tweetObjectList.get(i);

            // Look for retweet in current tweet
            String retweetedUser = getRetweetedUserFromTweetText(to.getText());
            if (retweetedUser != null) {
                String forwardingUser = to.getUsername();

                System.out.println(forwardingUser + " retweeted " + retweetedUser);
            }
        }

        return 0;
    }

    public int getNumberOfEdges(String hashtag) {
        return 0;
    }

    /**
     * Returns the user that was retweeted in tweetText
     * Returns null if tweetText contains no retweet
     *
     * @param tweetText
     * @return
     */
    private static String getRetweetedUserFromTweetText(String tweetText) {
        Matcher retweetMatcher = retweetPattern.matcher(tweetText);

        if (retweetMatcher.find()) {
            return retweetMatcher.group(1);
        }

        return null;
    }

    // TODO: refactor
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
