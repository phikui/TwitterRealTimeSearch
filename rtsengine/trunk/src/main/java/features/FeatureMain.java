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

            writer.write(hashtag + newline);
            writer.write("Propagation: " + propagation + newline);
            writer.write("Retweet Network Number of Nodes: " + retweetNetworkNumberOfNodes + newline);
            writer.write("Retweet Network Number of Edges: " + retweetNetworkNumberOfEdges + newline);
            writer.write("line 2");

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
