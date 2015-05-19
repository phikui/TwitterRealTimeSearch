package model;

import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import twitter4j.GeoLocation;
import twitter4j.Place;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Phil on 27.04.2015.
 */
public class TweetObject implements Comparable, Serializable {
    private SimpleStringProperty username;
    private SimpleStringProperty text;
    private SimpleObjectProperty<Date> timestamp;
    private SimpleFloatProperty numberOfAuthorFollowers;

    public GeoLocation getGeoLocation() { return geoLocation; }

    public Place getPlace() { return place; }

    private GeoLocation geoLocation;
    private Place place;

    public TweetObject(String username, String text, GeoLocation geoLocation, Place place, Date timestamp, float numberOfAuthorFollowers) {
        this.username =  new SimpleStringProperty(username);
        this.text = new SimpleStringProperty(text);
        this.geoLocation = geoLocation;
        this.place = place;
        this.timestamp = new SimpleObjectProperty<Date>(timestamp);
        this.numberOfAuthorFollowers = new SimpleFloatProperty(numberOfAuthorFollowers);
    }

    public String getUsername() {
        return username.get();
    }

    public String getText() {
        return text.get();
    }

    public Date getTimestamp() {
        return timestamp.get();
    }

    public float getNumberOfAuthorFollowers() {
        return numberOfAuthorFollowers.get();
    }

    public int compareTo(Object o) {
        if (!(o instanceof TweetObject))
            throw new ClassCastException("A Tweet object expected.");
        return this.username.toString().compareTo(((TweetObject) o).username.toString());
    }
}