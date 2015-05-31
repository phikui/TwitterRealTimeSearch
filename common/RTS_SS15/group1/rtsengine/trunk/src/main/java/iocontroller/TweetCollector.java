package iocontroller;

import model.TransportObject;
import model.TweetObject;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;


/**
 * Created by phil on 31.05.2015.
 */
public class TweetCollector {
    private final IOController parent;
    private StatusListener listener;
    private TwitterStream twitterStream;

    public TweetCollector(IOController parent) {
        this.parent = parent;

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey("5apv3v5WO5i0DxzVu0zVgPzt4")
                .setOAuthConsumerSecret("HVySxTkurjj4cFZT8lmGXELsS4aELZxrVSTbDEiAZl9oodht5d")
                .setOAuthAccessToken("3209655082-AG0xOV3K15RffDQFZL7On3k85CNM1mP7ImTKQXR")
                .setOAuthAccessTokenSecret("Y8DqS6lIAd2fwslcw6xBWm0JW2y6vvQm2TbePESZqfAIW");


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
