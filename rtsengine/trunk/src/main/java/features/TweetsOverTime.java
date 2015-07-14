package features;

import model.TransportObject;
import model.TweetObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Guerki on 13/07/2015.
 */
public class TweetsOverTime {
    private static int numberOfTweets = 100000;

    public TweetsOverTime() {
    }

    double computeTweetsOverTime(List<TransportObject> tweetObjectList) {
        // create new QueryObject and query AO for Hashtag


        List<Integer> intervalTweetList = computeIntervalTweetList(tweetObjectList);
        double totalSlope = computeSlope(intervalTweetList);

        return Math.exp(totalSlope);
    }

    private List<Integer> computeIntervalTweetList(List<TransportObject> tweetObjectList) {
        Date timestamp1;
        Date timestamp2;
        int i = 0;
        List<TweetObject> tweetList = new ArrayList<>();
        TweetObject tweet1;
        List<Integer> intervalTweetList = new ArrayList<>();

        while (i <= tweetObjectList.size() - 1) {
            tweet1 = tweetObjectList.get(i).getTweetObject();
            tweetList.add(tweet1);
            timestamp1 = tweet1.getTimestamp();
            if (i < tweetObjectList.size() - 1) {
                timestamp2 = tweetObjectList.get(i + 1).getTimestamp();
            }
            else{
                break;
            }
            while(timeDiff(timestamp1, timestamp2) <= 30*60000) {
                i++;
                tweetList.add(tweetObjectList.get(i).getTweetObject());
                if (i < tweetObjectList.size() - 1) {
                    timestamp2 = tweetObjectList.get(i + 1).getTweetObject().getTimestamp();
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
