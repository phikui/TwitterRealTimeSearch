package features;

import model.TweetObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Guerki on 13/07/2015.
 */
public class TweetsOverTime extends FeatureBase {
    public TweetsOverTime(){}

    private static int numberOfTweets = 100000;

    double computeTweetsOverTime(String hashtag){
        // create new QueryObject and query AO for Hashtag
        this.createAndGetTweetList(hashtag, numberOfTweets);

        List<Integer> intervalTweetList = computeIntervalTweetList();
        double totalSlope = computeSlope(intervalTweetList);

        return Math.exp(totalSlope);
    }

    private List<Integer> computeIntervalTweetList(){
        Date timestamp1;
        Date timestamp2;
        int i = 0;
        List<TweetObject> tweetList = new ArrayList<>();
        TweetObject tweet1;
        List<Integer> intervalTweetList = new ArrayList<>();

        while(i <= this.tweetObjectList.size()-1){
            tweet1 = this.tweetObjectList.get(i);
            tweetList.add(tweet1);
            timestamp1 = tweet1.getTimestamp();
            if(i < this.tweetObjectList.size()-1){
                timestamp2 = this.tweetObjectList.get(i+1).getTimestamp();
            }
            else{
                break;
            }
            while(timeDiff(timestamp1, timestamp2) <= 30*60000) {
                i++;
                tweetList.add(this.tweetObjectList.get(i));
                if(i < this.tweetObjectList.size()-1){
                    timestamp2 = this.tweetObjectList.get(i+1).getTimestamp();
                }
                else{
                    break;
                }
            }
            intervalTweetList.add(( tweetList.size()));
            i++;
            tweetList.clear();
        }
        return intervalTweetList;
    }

    private double computeSlope(List<Integer> intervalTweetList){
        double sum = 0;
        for (int i = 0; i<intervalTweetList.size()-1; i++){
            sum+= intervalTweetList.get(i+1)-intervalTweetList.get(i);
        }
        if(sum>0){
            return sum;
        }
        else{
            return 1;
        }
    }

    private double timeDiff(Date ts1, Date ts2){
        double result = ts2.getTime() - ts1.getTime();
        return Math.abs(result);
    }

}
