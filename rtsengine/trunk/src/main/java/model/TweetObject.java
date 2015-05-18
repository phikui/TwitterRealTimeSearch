package model;

import twitter4j.GeoLocation;
import twitter4j.Place;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Phil on 27.04.2015.
 */
public class TweetObject implements Comparable, Serializable {
    private String username;
    private String text;
    private Date timestamp;
    private float numberOfAuthorFollowers;

    public GeoLocation getGeoLocation() { return geoLocation; }

    public Place getPlace() { return place; }

    private GeoLocation geoLocation;
    private Place place;

    public TweetObject(String username, String text, GeoLocation geoLocation, Place place, Date timestamp, float numberOfAuthorFollowers) {
        this.username = username;
        this.text = text;
        this.geoLocation = geoLocation;
        this.place = place;
        this.timestamp = timestamp;
        this.numberOfAuthorFollowers = numberOfAuthorFollowers;
    }

    public TweetObject(String username, String text){
        this.username = username;
        this.text = text;
    }

    public String getUsername() {
        return this.username;
    }

    public String getText() {
        return this.text;
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    public float getNumberOfAuthorFollowers() {
        return this.numberOfAuthorFollowers;
    }

    public int compareTo(Object o) {
        if (!(o instanceof TweetObject))
            throw new ClassCastException("A Tweet object expected.");
        return this.username.compareTo( ((TweetObject) o).username);
    }
}