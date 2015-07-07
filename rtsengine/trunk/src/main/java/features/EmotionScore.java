package features;

import model.TweetObject;

import java.io.*;

/**
 * Created by niharika singhal on 07-07-2015.
 */
public class EmotionScore extends FeatureBase {

    private static int numberOfTweets = 100000;

      public void getTweetForEmotion(String hashTag) {

            this.createAndGetTweetList(hashTag, numberOfTweets);
            // Iterate over all tweets fetched from index ans safe them in a file to run on SentiStrength
            //System.out.println("hi oustside the loop"+tweetObjectList.size());
            for (int i = 0; i < this.tweetObjectList.size(); i++) {
                TweetObject tweetObject = tweetObjectList.get(i);
                try {
                    // need to save the tweet in the text file to get the emotion score
                    BufferedWriter out = new BufferedWriter(new FileWriter("C:\\tweet\\TestFile.txt"));
                    out.write(tweetObject.getText()+"\n");
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // Open the file that have been used by the
        public void extractingTweetFromEmotionFile(){
            FileInputStream fstream = null;
            try  {
                fstream = new FileInputStream("C:\\tweet\\outputfile+results.txt");
                BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
                String strLine;
                //Read File Line By Line
                while ((strLine = br.readLine()) != null) {
                    // Print the content on the console
                    //  System.out.println (strLine);
                    if (strLine.contains("5") || strLine.contains("4") || strLine.contains("-3") || strLine.contains("-4")) {
                        System.out.println("" + strLine);
                        // here i need to safe the result
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}

