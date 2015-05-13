import tweetcollector.TweetCollector;
import twitter4j.TwitterException;

/**
 * Created by chans on 5/12/15.
 */
public class RTSEngineRunner {

    public static void main(String[] args) {
        System.out.println("Hello World from RTSEngineRunner!");

        // For now just collect Tweets
        try {
            TweetCollector.collectTweets();
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

}