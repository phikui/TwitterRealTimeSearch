package features;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * Created by Guerki on 03/07/2015.
 */
public class FeatureMain extends Thread{
    public FeatureMain(){}

    public void analyze(String hashtag) {
        String newline = System.getProperty("line.separator");

        // popular-value, 0 = not popular, 1 = popular
        int isPopular = 0;

        /* add this when we have a mapDB file
        // load all hashtags and get the popular ones
        List<String> allHashtags = MapDBLoad.loadHashtagFile();
        List<String> popularHashtags = MapDBLoad.getMostPopularHashtags(allHashtags);

        if(popularHashtags.contains(hashtag)){
            isPopular = 1;
        }*/

        // extract features for location
        LocationFeature locationFeature = new LocationFeature();
        double propagation = locationFeature.calculateLocationScore(hashtag);

        // extract features for retweet network
        RetweetNetworkFeatures retweetNetworkFeatures = new RetweetNetworkFeatures();
        retweetNetworkFeatures.buildRetweetGraph(hashtag);
        int retweetNetworkNumberOfNodes = retweetNetworkFeatures.getNumberOfNodes(hashtag);
        int retweetNetworkNumberOfEdges = retweetNetworkFeatures.getNumberOfEdges(hashtag);
        int retweetNetworkDiameter = retweetNetworkFeatures.getDiameter(hashtag);
        double retweetNetworkAverageDegree = retweetNetworkFeatures.getAverageDegree(hashtag);

        BufferedWriter writer = null;
        try {
            File result = new File("results");
            writer = new BufferedWriter(new FileWriter(result,true));
            // File contains one line for each hash tag with the features separated by tab
            // character in this order:
            // isPopular, Propagation, Retweet Network Number of Nodes, Retweet Network Number of Edges,
            // Retweet Network Diameter, Retweet Network Average Degree

            writer.write(isPopular);
            writer.write('\t');

            writer.write(Double.toString(propagation));
            writer.write('\t');

            writer.write(Integer.toString(retweetNetworkNumberOfNodes));
            writer.write('\t');

            writer.write(Integer.toString(retweetNetworkNumberOfEdges));
            writer.write('\t');

            writer.write(Integer.toString(retweetNetworkDiameter));
            writer.write('\t');

            writer.write(Double.toString(retweetNetworkAverageDegree));

            writer.write(newline);

        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try{
                writer.close();
            } catch (Exception e) {
            }
        }
    }
}
