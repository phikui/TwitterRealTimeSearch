package features;

import indices.IndexDispatcher;
import iocontroller.IOController;
import iocontroller.preprocessor.Stemmer;
import model.TermDictionary;
import model.TransportObject;
import model.TweetDictionary;
import model.TweetObject;
import twitter4j.GeoLocation;

import java.util.*;

/**
 * Created by Maik on 03.07.2015.
 */
public class LocationFeature {

    private List<GeoLocation> geoLocList;
    private Set<String> countrySet;
    private List<Double> distanceList;

    public LocationFeature() {
        this.geoLocList = new ArrayList<>();
        this.countrySet = new HashSet<>();
        this.distanceList = new ArrayList<>();
    }

    public double calculateLocationScore(String hashtag) {

        double locationScore;
        int numberOfTweets = 100000;
        List<TweetObject> tweetObjectList;

        double sumOfDistances;
        int numberOfCountries;

        // create new QueryObject and query AO for Hashtag
        tweetObjectList = createAndGetTweetList(hashtag, numberOfTweets);

        // iterate tweetObjectList and gather GeoLocation information
        populateGeoLocationList(tweetObjectList);

        // iterate geoLocationList and create distanceList
        createDistanceList();

        // calculate the overall distance
        sumOfDistances = calculateTotalDistance();

        // function to determine feature score
        numberOfCountries = countrySet.size();
        locationScore = calculateFeatureScore(sumOfDistances, numberOfCountries);


        return locationScore;
    }


    private List<TweetObject> createAndGetTweetList(String hashtag, int k) {
        TransportObject queryObject = new TransportObject(hashtag, new Date(), k);

        // stem/preprocess hashtag
        List<String> stems;
        stems = IOController.stemmer.get().stem(queryObject.getText());
        queryObject.setTerms(stems);

        // write term list
        List<Integer> termIDs = new ArrayList<Integer>();
        for (String term : queryObject.getTerms()) {
            int id = TermDictionary.insertTerm(term);
            termIDs.add(id);
        }
        queryObject.setTermIDs(termIDs);

        // start the AO index query
        List<Integer> resultsIndex = IndexDispatcher.searchTweetIDsAO(queryObject);

        // create the tweetObject list
        List<TweetObject> resultTweets = new ArrayList<>();
        for (int index : resultsIndex) {
            resultTweets.add(TweetDictionary.getTransportObject(index).getTweetObject());
        }

        return resultTweets;
    }


    private void populateGeoLocationList(List<TweetObject> tweetObjectList) {

        System.out.println("GeoLocList Size: " + tweetObjectList.size());

        for (int i = 0; i < tweetObjectList.size(); i++) {

            // populate geoLocList
            geoLocList.add(tweetObjectList.get(i).getGeoLocation());

            // populate countrySet
            if ((tweetObjectList.get(i).getPlace() != null) && (tweetObjectList.get(i).getPlace().getCountry() != null)) {
                countrySet.add(tweetObjectList.get(i).getPlace().getCountry());
            }
        }

        // just a safety check to guarantee that at least one country is involved if the GeoLocList is not empty
        if (!(geoLocList.isEmpty()) && (countrySet.isEmpty())) {
            countrySet.add("undefined country");
        }

    }


    private void createDistanceList() {

        double latitude_first;
        double longitude_first;

        double latitude_second;
        double longitude_second;

        double distance;

        // compare each distance with each other and get distance in km between them
        for (int i = 0; i < geoLocList.size(); i++) {

            for (int j = 0; j < geoLocList.size(); j++) {

                if (i == j) {
                    continue;
                } else if (j < i) {
                    continue;
                } else if (i < j) {
                    if ((geoLocList.get(i) != null) && (geoLocList.get(j) != null)) {
                        latitude_first = geoLocList.get(i).getLatitude();
                        longitude_first = geoLocList.get(i).getLongitude();

                        latitude_second = geoLocList.get(j).getLatitude();
                        longitude_second = geoLocList.get(j).getLongitude();

                        // calculate distance in km
                        distance = calculateLatLongDistance(latitude_first, longitude_first, latitude_second, longitude_second);
                        System.out.println("Distance: " + distance);
                        distanceList.add(distance);
                    }
                }

            }
        }

    }


    private double calculateLatLongDistance(double latitude_first, double longitude_first, double latitude_second, double longitude_second) {
        double theta = longitude_first - longitude_second;
        double dist = Math.sin(deg2rad(latitude_first)) * Math.sin(deg2rad(latitude_second)) + Math.cos(deg2rad(latitude_first)) * Math.cos(deg2rad(latitude_second)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515 * 1.609344;

        return (dist);
    }


    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts decimal degrees to radians             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }


    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
    /*::  This function converts radians to decimal degrees             :*/
    /*:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/

    private double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }


    private double calculateTotalDistance() {
        double totalDistance = 0.0;

        for (int i = 0; i < distanceList.size(); i++) {
            totalDistance += distanceList.get(i);
        }

        System.out.println("Total Distance: " + totalDistance);
        return totalDistance;
    }


    private double calculateFeatureScore(double sumOfDistances, int numberOfCountries) {
        double featureScore;

        featureScore = numberOfCountries * sumOfDistances;

        System.out.println("Feature Score: " + featureScore);
        return featureScore;
    }

}
