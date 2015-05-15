package utilities;

import model.TweetObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by chans on 5/15/15.
 */
public class HelperFunctions {

    public static float calculateSignificance(TweetObject tweetObject) {
        return tweetObject.getNumberOfAuthorFollowers();
    }

    public static float calculateTermSimilarity(List<Integer> termIDs1, List<Integer> termIDs2) {
        // TODO: implement
        return 0;
    }

    public static float calculateFreshness(Date timestamp) {
        // TODO: Improve such that a non-linear decay is used
        Date now = new Date();
        return now.getTime() - timestamp.getTime();
    }

}
