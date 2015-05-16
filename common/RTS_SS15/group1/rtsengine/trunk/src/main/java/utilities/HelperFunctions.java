package utilities;

import edu.stanford.nlp.util.Sets;
import model.TweetObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by chans on 5/15/15.
 */
public class HelperFunctions {

    private final static float ONE_HOUR = 3600000;
    private final static float THREE_HOURS = 10800000;
    private final static float SIX_HOURS = 21600000;
    private final static float TWELVE_HOURS = 43200000;
    private final static float ONE_DAY = 86400000;
    private final static float TWO_DAYS = 172800000;
    private final static float THREE_DAYS = 259200000;
    private final static float ONE_WEEK = 604800000;

    public static float calculateSignificance(TweetObject tweetObject) {
        return tweetObject.getNumberOfAuthorFollowers();
    }

    public static float calculateTermSimilarity(List<Integer> termIDs1, List<Integer> termIDs2) {
        // For now calculate cosine similarity (i.e. number of terms in both lists)
        List<Integer> retainList = new ArrayList<Integer>(termIDs1);
        retainList.retainAll(termIDs2);
        return retainList.size();
    }

    /**
     * Use milliseconds for determining freshness:
     *   3 600 000 =  1 hour
     *  10 800 000 =  3 hours
     *  21 600 000 =  6 hours
     *  43 200 000 = 12 hours
     *  86 400 000 = 1 day
     * 172 800 000 = 2 days
     * 259 200 000 = 3 days
     * 604 800 000 = 1 week
     *
     * freshness based on used granularity, if timeStamp is older than granularity freshness is 0
     *
     * @param timestamp
     * @return
     */
    public static float calculateFreshness(Date timestamp) {
        // TODO: Improve such that a non-linear decay is used
        Date now = new Date();
        float freshness = (1 - ((now.getTime() - timestamp.getTime()) / ONE_HOUR));
        if (freshness < 0)
            freshness = 0;

        return freshness;
    }

}
