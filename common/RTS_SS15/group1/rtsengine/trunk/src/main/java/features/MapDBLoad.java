package features;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import gui.MainApp;
import iocontroller.IOController;
import model.TweetDictionary;
import model.TweetObject;
import org.apache.commons.lang3.StringEscapeUtils;
import org.mapdb.*;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Maik on 08.07.2015.
 */
public class MapDBLoad {


    public static void createPopularHashtagSet() {

        // set with hashtags
        Multiset<String> hashTagSet = HashMultiset.create();

        TweetObject currentTweetObject;
        String currentTweetText;

        // Pattern for recognizing hashtags
        Pattern hashtagPattern = Pattern.compile("#(\\w+|\\W+)");

        for (int index : TweetDictionary.getTweetDictionary().keySet()) {
            currentTweetObject = TweetDictionary.getTweetDictionary().get(index).getTweetObject();
            currentTweetText = currentTweetObject.getText();

            //currentTweetText = "#test #test #hallo #test #ggg #hallo #test nope #tt #test #i #d #just #write #some #stuff #i #i #can #just #i #more #words #to #have #and #sdf #a #b #c";

            // match the pattern to the current tweet text and add hashtags to set
            Matcher mat = hashtagPattern.matcher(currentTweetText);
            while (mat.find()) {
                hashTagSet.add(mat.group(1));
            }

        }

        // write newly created hashtag set to a file
        writeUniqueHashtagSetFile(hashTagSet);

    }


    /**
     * ordered based on hashtag occurences in descending order
     *
     * @param uniqueHashTagSet
     */
    private static void writeUniqueHashtagSetFile(Multiset<String> uniqueHashTagSet) {

        String newline = System.getProperty("line.separator");


        BufferedWriter writer = null;
        try {
            File result = new File("UniqueHashtagSet");
            writer = new BufferedWriter(new FileWriter(result, false));

            for (String hashtag : Multisets.copyHighestCountFirst(uniqueHashTagSet).elementSet()) {
                writer.write(hashtag);
                writer.write(newline);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert writer != null;
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        BufferedWriter writer2 = null;
        try {
            File result = new File("UniqueHashtagSetCount");
            writer2 = new BufferedWriter(new FileWriter(result, false));
            int count;
            String strCount;

            for (String hashtag : Multisets.copyHighestCountFirst(uniqueHashTagSet).elementSet()) {
                writer2.write(hashtag);
                writer2.write('\t');
                count = uniqueHashTagSet.count(hashtag);
                strCount = ""+count;
                writer2.write(strCount);
                writer2.write(newline);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert writer2 != null;
                writer2.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static List<String> loadHashtagFile() {
        List<String> hashtagList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader("UniqueHashtagSet"))) {

            String line;

            while ((line = br.readLine()) != null) {
                hashtagList.add(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        //System.out.println(hashtagList);
        return hashtagList;
    }

    public static List<String> samplePopularHashtags(List<String> hashtagList, int maximumNumberOfHashtags) {
        List<String> popularHashtagList = new ArrayList<>();

        // top 33% rounded to int
        int topK = (int) (hashtagList.size() * 0.33);

        // sample from top 0.33 quantile
        int sample_increment = (int) ((hashtagList.size() * 0.33) / maximumNumberOfHashtags) - 1;

        // current hashTagList element
        String currentHashtag;

        for (int i = 0; i < topK; i = i + sample_increment) {
            currentHashtag = hashtagList.get(i);
            popularHashtagList.add(currentHashtag);

            if (popularHashtagList.size() >= maximumNumberOfHashtags) {
                break;
            }
        }

        //System.out.println(popularHashtagList);
        return popularHashtagList;
    }

    public static List<String> sampleUnPopularHashtags(List<String> hashtagList, int maximumNumberOfHashtags) {
        List<String> unPopularHashtagList = new ArrayList<>();

        // bottom 33% rounded to int
        int bottomK = (int) (hashtagList.size() * 0.66);

        // sample from bottom 0.33 quantile
        int sample_increment = (int) ((hashtagList.size() * 0.33) / maximumNumberOfHashtags) - 1;

        // current hashTagList element
        String currentHashtag;

        for (int i = hashtagList.size() - 1; i >= bottomK; i = i - sample_increment) {
            currentHashtag = hashtagList.get(i);
            unPopularHashtagList.add(currentHashtag);

            if (unPopularHashtagList.size() >= maximumNumberOfHashtags) {
                break;
            }
        }

        //System.out.println(unPopularHashtagList);
        return unPopularHashtagList;
    }
}
