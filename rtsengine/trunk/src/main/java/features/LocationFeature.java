package features;

import model.TransportObject;
import model.TweetObject;
import twitter4j.GeoLocation;

import java.util.*;

/**
 * Created by Maik on 03.07.2015.
 */
public class LocationFeature {

    private static int numberOfTweets = 100000;
    Set<String> countrySet = new HashSet<>();


    public LocationFeature() {

    }

    public double calculateLocationScore(List<TransportObject> transportObjectList) {

        double locationScore;
        int numberOfCountries;
        double totalSlope;
        List<GeoLocation> geoLocList = new ArrayList<>();
        List<Double> distanceList = new ArrayList<>();
        List<Double> intervalDistanceList = new ArrayList<>();

        // create new QueryObject and query AO for Hashtag


        // iterate over transportObjectList and gather GeoLocation information
        geoLocList = populateGeoLocationList(transportObjectList);

        // iterate geoLocationList and create distanceList
        distanceList = createDistanceList(geoLocList);

        // calculate the overall distance
        double sumOfDistances = calculateTotalDistance(distanceList);

        // compute what distance the hashtag travelled per 5 minute intervals
        intervalDistanceList = computeIntervalDistanceList(transportObjectList);

        totalSlope = computeSlope(intervalDistanceList);

        // function to determine feature score
        numberOfCountries = countrySet.size();
        locationScore = calculateFeatureScore(sumOfDistances, numberOfCountries, totalSlope);


        return locationScore;
    }

    private double computeSlope(List<Double> intervalDistanceList){
        double sum = 0;
        for (int i = 0; i<intervalDistanceList.size()-1; i++){
            sum+= intervalDistanceList.get(i+1)-intervalDistanceList.get(i);
        }
        if(sum>0){
            return sum;
        }
        else{
            return 1;
        }
    }

    private List<Double> computeIntervalDistanceList(List<TransportObject> transportObjectList) {
        Date timestamp1;
        Date timestamp2;
        int i = 0;
        List<TweetObject> tweetList = new ArrayList<>();
        TweetObject tweet1;
        List<Double> intervalDistanceList = new ArrayList<>();

        while (i <= transportObjectList.size() - 1) {
            tweet1 = transportObjectList.get(i).getTweetObject();
            tweetList.add(tweet1);
            timestamp1 = tweet1.getTimestamp();
            if (i < transportObjectList.size() - 1) {
                timestamp2 = transportObjectList.get(i + 1).getTimestamp();
            }
            else{
                break;
            }
            while(timeDiff(timestamp1, timestamp2) <= 30*60000) {
                i++;
                tweetList.add(transportObjectList.get(i).getTweetObject());
                if (i < transportObjectList.size() - 1) {
                    timestamp2 = transportObjectList.get(i + 1).getTimestamp();
                }
                else{
                    break;
                }
            }
            double sumOfDistances = calculateTotalDistance(createDistanceList(populateGeoLocationList(transportObjectList)));
            intervalDistanceList.add(sumOfDistances);
            i++;
            tweetList.clear();
        }

        return intervalDistanceList;
    }

    private double timeDiff(Date ts1, Date ts2){
        double result = ts2.getTime() - ts1.getTime();
        return Math.abs(result);
    }


    private List<GeoLocation> populateGeoLocationList(List<TransportObject> transportObjectList) {
        List<GeoLocation> geoLocList = new ArrayList<>();

        //System.out.println("GeoLocList Size: " + transportObjectList.size());

        for (int i = 0; i < transportObjectList.size(); i++) {

            // populate geoLocList
            geoLocList.add(transportObjectList.get(i).getTweetObject().getGeoLocation());

            // populate countrySet
            if ((transportObjectList.get(i).getTweetObject().getPlace() != null) && (transportObjectList.get(i).getTweetObject().getPlace().getCountry() != null)) {
                countrySet.add(transportObjectList.get(i).getTweetObject().getPlace().getCountry());
            }
        }

        // just a safety check to guarantee that at least one country is involved if the GeoLocList is not empty
        if (!(geoLocList.isEmpty()) && (countrySet.isEmpty())) {
            countrySet.add("undefined country");
        }
        return geoLocList;
    }


    private List<Double> createDistanceList(List<GeoLocation> geoLocList) {

        double latitude_first;
        double longitude_first;

        double latitude_second;
        double longitude_second;

        double distance;

        List<Double> distanceList = new ArrayList<>();

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
                        //System.out.println("Distance: " + distance);
                        distanceList.add(distance);
                    }
                }

            }
        }
        return distanceList;

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


    private double calculateTotalDistance(List<Double> distanceList){
        double totalDistance = 0.0;

        for (int i = 0; i < distanceList.size(); i++) {
            totalDistance += distanceList.get(i);
        }

        //System.out.println("Total Distance: " + totalDistance);
        return totalDistance;
    }


    private double calculateFeatureScore(double sumOfDistances, int numberOfCountries, double totalSlope) {
        double featureScore;

        featureScore = numberOfCountries * sumOfDistances * Math.exp(totalSlope);

        //System.out.println("Feature Score: " + featureScore);
        return featureScore;
    }

}
