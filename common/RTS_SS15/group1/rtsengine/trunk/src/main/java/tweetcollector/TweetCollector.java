package tweetcollector;

import model.TweetObject;

import java.io.File;
import java.util.concurrent.ConcurrentNavigableMap;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import org.mapdb.DB;
import org.mapdb.DBMaker;

/**
 * Created by chans on 5/12/15.
 */
public class TweetCollector {

    public static void collectTweets() throws TwitterException {
        // The factory instance is re-useable and thread safe.

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey("5apv3v5WO5i0DxzVu0zVgPzt4")
                .setOAuthConsumerSecret("HVySxTkurjj4cFZT8lmGXELsS4aELZxrVSTbDEiAZl9oodht5d")
                .setOAuthAccessToken("3209655082-AG0xOV3K15RffDQFZL7On3k85CNM1mP7ImTKQXR")
                .setOAuthAccessTokenSecret("Y8DqS6lIAd2fwslcw6xBWm0JW2y6vvQm2TbePESZqfAIW");

        final int[] counter = {0}; //to enable access from inner class

        // configure and open database using builder pattern.
        // all options are available with code auto-completion.
        final DB db = DBMaker.newFileDB(new File("tweetdb"))
                .closeOnJvmShutdown()
                .encryptionEnable("password")
                .make();


        // open existing an collection (or create new)
        final ConcurrentNavigableMap<TweetObject,Long> map = db.getTreeMap("Tweets");

        StatusListener listener = new StatusListener(){
            public void onStatus(Status status) {
                //System.out.println(status.getUser().getName() + " : " + status.getText());
                TweetObject newTweet = new TweetObject(
                        status.getUser().getName(),
                        status.getText(),
                        status.getGeoLocation(),
                        status.getPlace(),
                        status.getCreatedAt(),
                        status.getUser().getFollowersCount());

                map.put(newTweet,status.getId());
                counter[0]++;
                if (counter[0] > 100){ //commit every 100 tweets
                    counter[0] = 0;
                    System.out.println("Saving last 100 tweets");
                    db.commit();
                    System.out.println("database size is now " + map.size() + " tweets.");
                    System.out.println();
                    System.out.println("The last tweet was:");
                    System.out.println(newTweet.getUsername());
                    System.out.println(newTweet.getText());
                    System.out.println();

                }
            }
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}

            public void onScrubGeo(long l, long l1) {

            }

            public void onStallWarning(StallWarning stallWarning) {

            }

            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };

        // init twitter stream with cb as a configurationObject that handles authorization
        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        twitterStream.addListener(listener);

        // sample() method internally creates a thread which manipulates
        // TwitterStream and calls these adequate listener methods continuously.
        twitterStream.sample("en");
    }

}
