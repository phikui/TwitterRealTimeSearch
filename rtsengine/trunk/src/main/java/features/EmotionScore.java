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

                try {
                    // need to save the tweet in the text file to get the emotion score
                    BufferedWriter out = new BufferedWriter(new FileWriter("EmotionFile"));
                    for (int i = 0; i < this.tweetObjectList.size(); i++) {
                        TweetObject tweetObject = tweetObjectList.get(i);
                         //String strTemp=tweetObject.getText();
                         // System.out.println("the value of the tweet is :" + strTemp);
                        out.write(tweetObject.getText());
                        out.newLine();
                    }
                    out.flush();
                    out.close();
                  //  System.out.println("done");
                }catch (IOException e) {
                    e.printStackTrace();
                }
      }

        // Open the file that have been used by the
        public int extractingEmotionScoreFromEmotionFile() {
            // Open the file
            int count =0;
            try {
                FileInputStream fstream = new FileInputStream("EmotionOutputFile");
                BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
                String strLine;
                //Read File Line By Line
                while ((strLine = br.readLine()) != null && strLine.length() >= 2) {
                    String lastTwo = strLine.substring(strLine.length() - 4);
                    if (lastTwo.contains("5") || lastTwo.contains("4") || lastTwo.contains("-4") || lastTwo.contains("-5")) {
                        System.out.println("" + strLine);
                        ++count;
                        System.out.println("" + count);
                    }
                }
                //Close the input stream
                br.close();
                fstream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return count;
        }
}

