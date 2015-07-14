package features;

/**
 * Created by Guerki on 13/07/2015.
 */
public class SimpleFeatures extends FeatureBase{
    public SimpleFeatures(){}

    private static int numberOfTweets = 100000;

    double getAverageTweetlength(String hashtag){
        this.createAndGetTweetList(hashtag, numberOfTweets);
        double average = 0;
        double size = tweetObjectList.size();
        String text = "";
        for (int i = 0; i < size; i++) {
            text = this.tweetObjectList.get(i).getText();
            average += text.length();
        }
        if (size == 0){
            return 1d;
        }
        else{
            return average/size;
        }
    }

    double getAverageFollowers(String hashtag){
        this.createAndGetTweetList(hashtag, numberOfTweets);
        double average = 0;
        double size = tweetObjectList.size();
        for (int i = 0; i < tweetObjectList.size(); i++) {
            average = tweetObjectList.get(i).getNumberOfAuthorFollowers();
        }
        if (size == 0){
            return 1d;
        }
        else{
            return average/size;
        }
    }
}
