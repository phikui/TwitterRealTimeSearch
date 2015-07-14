package features;

import model.TweetDictionary;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

/**
 * Created by Guerki on 03/07/2015.
 */
public class FeatureMain extends Thread {
    public FeatureMain() {
    }

    // function to perform analyze for each hashtag in our mapDB-file/tweetDictionary
    public void analyzeForEachHashtag() {

        MapDBLoad.createPopularHashtagSet();

        List<String> allHashtags = MapDBLoad.loadHashtagFile();
        List<String> popularHashtags = MapDBLoad.getMostPopularHashtags(allHashtags);

        for (int i = 0; i < allHashtags.size(); i++) {
            analyze(allHashtags.get(i)/*, popularHashtags*/);
            if (i % 500 == 0) {
                System.out.println(i + " hashtags done, still computing...");
            }
        }
        System.out.println("All hashtags done!");

    }

    public void analyze(String hashtag/*, List<String> popularHashtags*/) {
        String newline = System.getProperty("line.separator");

        boolean isPopular = false;

        /*
        if(popularHashtags.contains(hashtag)){
            isPopular = true;
        }*/

        // extract features for location
        LocationFeature locationFeature = new LocationFeature();
        double propagation = locationFeature.calculateLocationScore(hashtag);

        // extract tweets over time
        TweetsOverTime tweetsOverTime = new TweetsOverTime();
        double tweetSlope = tweetsOverTime.computeTweetsOverTime(hashtag);

        // extract simple average features
        SimpleFeatures simpleFeatures = new SimpleFeatures();
        double averageTweetlength = simpleFeatures.getAverageTweetlength(hashtag);
        double averageFollowers = simpleFeatures.getAverageFollowers(hashtag);

        // extract features for retweet network
        RetweetNetworkFeatures retweetNetworkFeatures = new RetweetNetworkFeatures();
        retweetNetworkFeatures.buildRetweetGraph(hashtag);
        int retweetNetworkNumberOfNodes = retweetNetworkFeatures.getNumberOfNodes();
        int retweetNetworkNumberOfEdges = retweetNetworkFeatures.getNumberOfEdges();
//        int retweetNetworkDiameter = retweetNetworkFeatures.getDiameter();
//        double retweetNetworkAverageDegree = retweetNetworkFeatures.getAverageDegree();

        BufferedWriter writer = null;
        try {
            File result = new File("trainingSet");
            writer = new BufferedWriter(new FileWriter(result, true));
            // File contains one line for each hash tag with the features separated by tab
            // character in this order:
            // Label, isPopular, Propagation, Average Tweetlength, Average number of followers, Tweets over Time
            // Retweet Network Number of Nodes, Retweet Network Number of Edges,
            // Retweet Network Diameter, Retweet Network Average Degree
            System.out.println("test");
            writer.write(hashtag);
            writer.write('\t');

            writer.write(hashtag);
            writer.write('\t');

            writer.write(isPopular ? "P" : "N");
            writer.write('\t');

            writer.write(Double.toString(propagation));
            writer.write('\t');

            writer.write(Double.toString(averageTweetlength));
            writer.write('\t');

            writer.write(Double.toString(averageFollowers));
            writer.write('\t');

            writer.write(Double.toString(tweetSlope));
            writer.write('\t');

            writer.write(Integer.toString(retweetNetworkNumberOfNodes));
            writer.write('\t');

            writer.write(Integer.toString(retweetNetworkNumberOfEdges));
//            writer.write('\t');
//
//            writer.write(Integer.toString(retweetNetworkDiameter));
//            writer.write('\t');
//
//            writer.write(Double.toString(retweetNetworkAverageDegree));

            writer.write(newline);
//            writer.write(newline);

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
