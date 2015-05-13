package model;

import twitter4j.GeoLocation;
import twitter4j.Place;

import java.io.Serializable;

/**
 * Created by Phil on 27.04.2015.
 */
public class TweetObject implements Comparable, Serializable {
    private String username;
    private String text;

    public GeoLocation getGeoLocation() { return geoLocation; }

    public Place getPlace() { return place; }

    private GeoLocation geoLocation;
    private Place place;

    public TweetObject(String username, String text, GeoLocation geoLocation, Place place) {
        this.username = username;
        this.text = text;
        this.geoLocation = geoLocation;
        this.place = place;
    }

    public String getUsername() {
        return username;
    }

    public String getText() {
        return text;
    }

    public int compareTo(Object o) {
        if (!(o instanceof TweetObject))
            throw new ClassCastException("A Tweet object expected.");
        return this.username.compareTo( ((TweetObject) o).username);
    }
}