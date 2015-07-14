package features;

import iocontroller.preprocessor.SentimentAnalyser;
import model.TransportObject;

import java.util.List;

/**
 * Created by Guerki on 13/07/2015.
 */
public class SimpleFeatures {
    private static int numberOfTweets = 100000;

    public SimpleFeatures() {
    }

    double getAverageTweetlength(List<TransportObject> tweetObjectList) {

        double average = 0;
        double size = tweetObjectList.size();
        String text = "";
        for (int i = 0; i < size; i++) {
            text = tweetObjectList.get(i).getTweetObject().getText();
            average += text.length();
        }
        if (size == 0){
            return 1d;
        }
        else{
            return average/size;
        }
    }

    double getAverageFollowers(List<TransportObject> tweetObjectList) {

        double average = 0;
        double size = tweetObjectList.size();
        for (int i = 0; i < size; i++) {
            average += tweetObjectList.get(i).getTweetObject().getNumberOfAuthorFollowers();
        }
        if (size == 0){
            return 1d;
        }
        else{
            return average/size;
        }
    }

    double getAverageSentiment(List<TransportObject> tweetObjectList) {


        double average = 0;
        double size = tweetObjectList.size();
        for (int i = 0; i < size; i++) {
            TransportObject transport = tweetObjectList.get(i);
            transport.setSentiment(SentimentAnalyser.getSentiment(tweetObjectList.get(i).getText()));
            average += transport.getSentiment();
        }
        if (size == 0){
            return 1d;
        }
        else{
            return average/size;
        }
    }
}
