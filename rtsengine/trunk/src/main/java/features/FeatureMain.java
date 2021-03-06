package features;

import iocontroller.preprocessor.SentimentAnalyser;
import model.TransportObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

/**
 * Created by Guerki on 03/07/2015.
 */
public class FeatureMain extends Thread {

    private static int numberOfTweets = 100000;
    public FeatureMain() {
        SentimentAnalyser.init();
    }

    // function to perform analyze for each hashtag in our mapDB-file/tweetDictionary
    public void analyzeForEachHashtag() {

        MapDBLoad.createPopularHashtagSet();

        // Obtain 500 popular and 500 non-popular hashtags via sampling from allHashtags
        List<String> allHashtags = MapDBLoad.loadHashtagFile();
        List<String> popularHashtags = MapDBLoad.samplePopularHashtags(allHashtags, 100);
        List<String> unPopularHashtags = MapDBLoad.sampleUnPopularHashtags(allHashtags, 100);

        System.out.println("Found " + allHashtags.size() + " hashtags in total.");
        System.out.println("Identified " + popularHashtags.size() + " popular hashtags.");
        System.out.println("Identified " + unPopularHashtags.size() + " unpopular hashtags.");

        // Analyze popular hashtags
        boolean isPopular = true;
        System.out.println("Started analyzing popular hashtags...");
        for (int i = 0; i < popularHashtags.size(); i++) {
            analyze(popularHashtags.get(i), isPopular);
            if (i % 100 == 0) {
                System.out.println(i + " popular hashtags done, still computing...");
            }
        }

        // Analyze unpopular hashtags
        isPopular = false;
        System.out.println("Started analyzing unpopular hashtags...");
        for (int i = 0; i < unPopularHashtags.size(); i++) {
            analyze(unPopularHashtags.get(i), isPopular);
            if (i % 100 == 0) {
                System.out.println(i + " unpopular hashtags done, still computing...");
            }
        }

        System.out.println("All hashtags done!");
    }

    public void analyze(String hashtag, boolean isPopular) {
        String newline = System.getProperty("line.separator");

        List<TransportObject> tweetList = TweetListFromHashtag.createAndGetTweetList(hashtag, numberOfTweets);
        // extract features for location
        LocationFeature locationFeature = new LocationFeature();
        double locationScore = locationFeature.calculateLocationScore(tweetList);

        // extract tweets over time
        TweetsOverTime tweetsOverTime = new TweetsOverTime();
        double tweetSlope = tweetsOverTime.computeTweetsOverTime(tweetList);

        // extract simple average features
        SimpleFeatures simpleFeatures = new SimpleFeatures();
        double averageTweetLength = simpleFeatures.getAverageTweetlength(tweetList);
        double averageFollowers = simpleFeatures.getAverageFollowers(tweetList);
        double averageSentiment = simpleFeatures.getAverageSentiment(tweetList);
//        double averageSentiment = 0;
        // extract features for retweet network
        RetweetNetworkFeatures retweetNetworkFeatures = new RetweetNetworkFeatures();
        retweetNetworkFeatures.buildRetweetGraph(tweetList);
        int retweetNetworkNumberOfNodes = retweetNetworkFeatures.getNumberOfNodes();
        int retweetNetworkNumberOfEdges = retweetNetworkFeatures.getNumberOfEdges();
        int retweetNetworkDiameter = retweetNetworkFeatures.getDiameter();
        double retweetNetworkAverageDegree = retweetNetworkFeatures.getAverageDegree();

        BufferedWriter writer = null;
        try {
            File result = new File("trainingSet");
            writer = new BufferedWriter(new FileWriter(result, true));
            // File contains one line for each hash tag with the features separated by tab
            // character in this order:
            // Label, isPopular, Propagation, Average Tweetlength, Average number of followers, Tweets over Time
            // Average Sentiment, Retweet Network Number of Nodes, Retweet Network Number of Edges,
            // Retweet Network Diameter, Retweet Network Average Degree
            writer.write(hashtag);
            writer.write('\t');

            writer.write(isPopular ? "P" : "N");
            writer.write('\t');

            writer.write(Double.toString(locationScore));
            writer.write('\t');

            writer.write(Double.toString(tweetSlope));
            writer.write('\t');

            writer.write(Double.toString(averageTweetLength));
            writer.write('\t');

            writer.write(Double.toString(averageFollowers));
            writer.write('\t');

            writer.write(Double.toString(averageSentiment));
            writer.write('\t');

            writer.write(Integer.toString(retweetNetworkNumberOfNodes));
            writer.write('\t');

            writer.write(Integer.toString(retweetNetworkNumberOfEdges));
            writer.write('\t');

            writer.write(Integer.toString(retweetNetworkDiameter));
            writer.write('\t');

            writer.write(Double.toString(retweetNetworkAverageDegree));

            writer.write(newline);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
            }
        }
    }
}
