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

        // extract features for location
        LocationFeature locationFeature = new LocationFeature();
        double propagation = locationFeature.calculateLocationScore(hashtag);

        // extract features for retweet network
        RetweetNetworkFeatures retweetNetworkFeatures = new RetweetNetworkFeatures();
        int retweetNetworkNumberOfNodes = retweetNetworkFeatures.getNumberOfNodes(hashtag);
        int retweetNetworkNumberOfEdges = retweetNetworkFeatures.getNumberOfEdges(hashtag);

        BufferedWriter writer = null;
        try {
            File result = new File("results");
            writer = new BufferedWriter(new FileWriter(result,true));

            // File contains one line for each hash tag with the features separated by tab
            // character in this order:
            // Propagation, Retweet Network Number of Nodes, Retweet Network Number of Edges

            writer.write(Double.toString(propagation));
            writer.write('\t');

            writer.write(Integer.toString(retweetNetworkNumberOfNodes));
            writer.write('\t');

            writer.write(Integer.toString(retweetNetworkNumberOfEdges));
            writer.write('\t');

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
