package utilities;

import indices.aoi.AOIIndex;
import model.TransportObject;
import model.TweetObject;
import org.apache.commons.lang3.RandomStringUtils;
import twitter4j.GeoLocation;
import twitter4j.Place;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Maik on 16.05.2015.
 */
public class TransportObjectCreator {

    private final static int MIN_TERMS = 1;
    private final static int MAX_TERMS = 10;

    private final static int MIN_ID_RANGE = 1;
    private final static int MAX_ID_RANGE = 100;

    private final static int MIN_FOLLOWERS = 0;
    private final static int MAX_FOLLOWERS = 1000000;

    /**
     * creates n many transport objects for testing purposes only
     * call this method to test stuff
     *
     * @param n
     */
    public void createTransportObjects(int n){
        // change index here
        AOIIndex ao = new AOIIndex();

        for(int i = 0; i < n; i++){
            ao.insertTransportObject(createRandomTransportObject(i+1));
        }
    }

    /**
     * creates one random transport object for testing purposes only
     *
     * @param tweetID
     * @return
     */
    private TransportObject createRandomTransportObject(int tweetID){
        TweetObject tweetObject = createTweetObject();
        TransportObject to = new TransportObject(tweetObject);

        // random number of termIDs between MIN and MAX
        int termIDNumber = MIN_TERMS + (int)(Math.random() * ((MAX_TERMS - MIN_TERMS) + 1));
        int randTermID;

        // set tweetID
        to.setTweetID(tweetID);

        // create termIDs
        List <Integer> termIDList = new ArrayList<Integer>();
        for (int i = 0; i < termIDNumber; i++){
            // random termID between
            randTermID = MIN_ID_RANGE + (int)(Math.random() * ((MAX_ID_RANGE - MIN_ID_RANGE) + 1));
            if(!termIDList.contains(randTermID)) {
                termIDList.add(randTermID);
                System.out.println("TermID" + (i+1) + ": " + randTermID);
            }
        }
        to.setTermIDs(termIDList);

        // print information
        System.out.println("TweetID: " + to.getTweetID());
        System.out.println("Freshness: ");
        System.out.println("--------------------------");

        return to;
    }

    /**
     * creates a random tweet object but not completely, currently only relevant for significance and freshness
     *
     * @return
     */
    private TweetObject createTweetObject(){
        String name = RandomStringUtils.random(5);
        String text = RandomStringUtils.random(140);
        GeoLocation geoLocation = null;
        Place place = null;
        Date timestamp = new Date();
        float numberOfAuthorFollowers = MIN_FOLLOWERS + (int)(Math.random() * ((MAX_FOLLOWERS - MIN_FOLLOWERS) + 1));

        // print TweetObject details
        System.out.println("Name: " + name);
        System.out.println("Date: " + timestamp);
        System.out.println("Followers: " + numberOfAuthorFollowers);


        return new TweetObject(name, text, geoLocation, place, timestamp, numberOfAuthorFollowers, false);
    }
}
