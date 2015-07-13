package model;

import twitter4j.GeoLocation;
import twitter4j.Place;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Phil on 27.04.2015.
 */
public class TweetObject implements Comparable, Serializable {
    private String username;
    private String text;
    private Date timestamp;
    private float numberOfAuthorFollowers;
    private GeoLocation geoLocation;
    private Place place;
    private TransportObject transportObject;
    public TweetObject(String username, String text, GeoLocation geoLocation, Place place, Date timestamp, float numberOfAuthorFollowers) {
        this.username = username;
        this.text = text;
        this.geoLocation = geoLocation;
        this.place = place;
        this.timestamp = timestamp;
        this.numberOfAuthorFollowers = numberOfAuthorFollowers;
    }

    //Constructur for empty dummy object
    public TweetObject(String query) {
        this("null", "No results for query " + query + " found.", null, null, Calendar.getInstance().getTime(), 0);
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public Place getPlace() {
        return place;
    }

    public String getUsername() {
        return username;
    }

    public String getText() {
        return text;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public float getNumberOfAuthorFollowers() {
        return numberOfAuthorFollowers;
    }

    public int compareTo(Object o) {
        if (!(o instanceof TweetObject))
            throw new ClassCastException("A Tweet object expected.");
        return this.username.toString().compareTo(((TweetObject) o).username.toString());
    }

    public TransportObject getTransportObject() {
        return transportObject;
    }

    public void setTransportObject(TransportObject transportObject) {
        this.transportObject = transportObject;
    }
}