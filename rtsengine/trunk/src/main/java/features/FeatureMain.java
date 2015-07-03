package features;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * Created by Guerki on 03/07/2015.
 */
public class FeatureMain {

    public static void analyze(String hashtag) {
        String newline = System.getProperty("line.separator");

        double propagation = TimeSeries.propagation(hashtag);

        BufferedWriter writer = null;
        try{
            File result = new File("result");
            writer = new BufferedWriter(new FileWriter(result,true));
            writer.write(hashtag + newline);
            writer.write("Propagation: " + propagation + newline);
            writer.write("line 2");

        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try{
                writer.close();
            } catch (Exception e){
            }
        }
    }
}
