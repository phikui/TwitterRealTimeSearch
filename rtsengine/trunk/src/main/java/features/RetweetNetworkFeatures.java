package features;

import indices.IndexDispatcher;
import iocontroller.IOController;
import model.TermDictionary;
import model.TransportObject;
import model.TweetDictionary;
import model.TweetObject;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chans on 7/5/15.
 */
public class RetweetNetworkFeatures extends FeatureBase {

    /**
     * Retweet graph Adjacency list
     *
     * Nodes: User names
     * Edge from user name a to user name b: a retweeted b
     *
     * Maps each node (a user name) to a list of all neighbors
     * in the retweet graph (i.e. all users that have been retweeted
     * by this user)
     */
    protected HashMap<String, List<String>> retweetGraphAdjacencyList;

    /**
     * RegEx pattern used to detect retweets ("RT @retweetedUsername") in tweets
     */
    private static Pattern retweetPattern = Pattern.compile("(?i)\\brt\\s*@(\\w+)");

    /**
     * Number of tweets to consider from Index
     */
    private static int numberOfTweets = 10000;

    /**
     * Returns number of nodes in retweet graph
     *
     * @param hashtag
     * @return
     */
    public int getNumberOfNodes(String hashtag) {
        this.buildRetweetGraph(hashtag);
        return this.retweetGraphAdjacencyList.size();
    }

    /**
     * Returns number of edges in retweet graph
     *
     * @param hashtag
     * @return
     */
    public int getNumberOfEdges(String hashtag) {
        this.buildRetweetGraph(hashtag);

        int numberOfEdges = 0;

        // Iterate over adjacency list and count edges
        for (Map.Entry<String, List<String>> entry : this.retweetGraphAdjacencyList.entrySet()) {
            String fromUsername = entry.getKey();
            List<String> fromUsernameNeighbourList = entry.getValue();
            numberOfEdges += fromUsernameNeighbourList.size();
        }

        return numberOfEdges;
    }

    private void buildRetweetGraph(String hashtag) {
        this.retweetGraphAdjacencyList = new HashMap<>();
        this.createAndGetTweetList(hashtag, numberOfTweets);

        // Iterate over all tweets fetched from index
        for (int i = 0; i < tweetObjectList.size(); i++) {
            TweetObject tweetObject = tweetObjectList.get(i);

            // Look for retweet in current tweet
            String retweetedUser = getRetweetedUserFromTweetText(tweetObject.getText());
            if (retweetedUser != null) {
                String forwardingUser = tweetObject.getUsername();

                // TODO: remove debug output
                System.out.println("#RT: " + forwardingUser + " retweeted " + retweetedUser);

                this.insertEdgeIntoRetweetGraph(forwardingUser, retweetedUser);
            }
        }
    }

    private void insertEdgeIntoRetweetGraph(String fromUsername, String toUsername) {
        List<String> fromUsernameNeighbourList = this.retweetGraphAdjacencyList.get(fromUsername);

        // Init neighbour list if non-existent
        if (fromUsernameNeighbourList == null) {
            fromUsernameNeighbourList = new LinkedList<String>();
            this.retweetGraphAdjacencyList.put(fromUsername, fromUsernameNeighbourList);
        }

        fromUsernameNeighbourList.add(toUsername);
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
}
