package features;

import model.TweetObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by chans on 7/5/15.
 */
public class RetweetNetworkFeatures extends FeatureBase {

    /**
     * RegEx pattern used to detect retweets ("RT @retweetedUsername") in tweets
     */
    private static Pattern retweetPattern = Pattern.compile("(?i)\\brt\\s*@(\\w+)");
    /**
     * Number of tweets to consider from Index
     */
    private static int numberOfTweets = 10000;
    /**
     * Retweet graph
     *
     * Nodes: User names
     * Edge from user name a to user name b: a retweeted b
     *
     * Maps each node (a user name) to a list of all neighbors
     * in the retweet graph (i.e. all users that have been retweeted
     * by this user)
     */
    protected RetweetGraph retweetGraph;

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

    /**
     * Returns number of nodes in retweet graph
     *
     * @param hashtag
     * @return
     */
    public int getNumberOfNodes(String hashtag) {
        this.buildRetweetGraph(hashtag);
        return this.retweetGraph.getNumberOfNodes();
    }

    /**
     * Returns diameter of retweet graph
     *
     * @param hashtag
     * @return
     */
    public int getDiameter(String hashtag) {
        this.buildRetweetGraph(hashtag);
        return this.retweetGraph.getDiameter();
    }

    /**
     * Returns number of edges in retweet graph
     *
     * @param hashtag
     * @return
     */
    public int getNumberOfEdges(String hashtag) {
        this.buildRetweetGraph(hashtag);

        return this.retweetGraph.getNumberOfEdges();
    }

    /**
     * Returns average degree of retweet graph
     *
     * @param hashtag
     * @return
     */
    public double getAverageDegree(String hashtag) {
        this.buildRetweetGraph(hashtag);

        return this.retweetGraph.getAverageDegree();
    }

    private void buildRetweetGraph(String hashtag) {
        this.retweetGraph = new RetweetGraph();
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

                this.insertEdgeIntoRetweetGraph(retweetGraph.getNodeFromName(forwardingUser),
                        retweetGraph.getNodeFromName(retweetedUser));
            }
        }
    }

    private void insertEdgeIntoRetweetGraph(RetweetGraphNode fromUsername, RetweetGraphNode toUsername) {
        this.retweetGraph.addDirectedEdge(fromUsername, toUsername);
    }


}
