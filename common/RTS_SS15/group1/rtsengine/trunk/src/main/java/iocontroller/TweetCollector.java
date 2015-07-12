package iocontroller;

import model.TransportObject;
import model.TweetObject;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.InputStream;
import java.util.Properties;


/**
 * Created by phil on 31.05.2015.
 */
public class TweetCollector {
    private final IOController parent;
    private StatusListener listener;
    private TwitterStream twitterStream;

    public TweetCollector(IOController parent) {
        this.parent = parent;

        //read config file
        Properties twitterProp = new Properties();
        try {

            String propFileName = "TwitterConfig.properties";
            InputStream input = getClass().getClassLoader().getResourceAsStream(propFileName);
            if (input != null) {
                twitterProp.load(input);
            } else {
                throw new Exception("Input null");
            }
        } catch (Exception e) {
            System.err.println("COULD NOT GET TWITTER PROPERTY FILE: TwitterConfig.properties");
            e.printStackTrace();
            System.exit(0);
        }

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(twitterProp.getProperty("ConsumerKey"))
                .setOAuthConsumerSecret(twitterProp.getProperty("ConsumerSecret"))
                .setOAuthAccessToken(twitterProp.getProperty("AuthAccessToken"))
                .setOAuthAccessTokenSecret(twitterProp.getProperty("AuthAccessTokenSecret"));


        listener = new StatusListener() {
            public void onStatus(Status status) {

                TweetObject newTweet = new TweetObject(
                        status.getUser().getName(),
                        status.getText(),
                        status.getGeoLocation(),
                        status.getPlace(),
                        status.getCreatedAt(),
                        status.getUser().getFollowersCount());
                parent.addTransportObject(new TransportObject(newTweet));

            }

            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            }

            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
            }

            public void onScrubGeo(long l, long l1) {

            }

            public void onStallWarning(StallWarning stallWarning) {

            }

            public void onException(Exception ex) {
                ex.printStackTrace();
            }
        };

        // init twitter stream with cb as a configurationObject that handles authorization
        twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
        twitterStream.addListener(listener);
    }

    public void startCollecting() {
        System.out.println("Collector has started");

        /*
        FilterQuery filter = new FilterQuery();
        String[] lang = {"en"};
        double[][] locations = { { -180d, -90d }, { 180d, 90d } };

        filter.language(lang);
        //filter.locations(locations);

        twitterStream.filter(filter);
        */
        // sample() method internally creates a thread which manipulates
        // TwitterStream and calls these adequate listener methods continuously.
        twitterStream.sample("en");
    }

    public void stopCollecting() {
        //Shutdown
        twitterStream.shutdown();
        twitterStream.clearListeners();
    }



}
