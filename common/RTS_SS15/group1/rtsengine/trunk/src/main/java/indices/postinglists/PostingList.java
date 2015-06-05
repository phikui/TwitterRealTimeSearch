package indices.postinglists;

import model.TweetDictionary;
import utilities.HelperFunctions;

import java.util.*;

/**
 * Created by chans on 6/5/15.
 */
public class PostingList extends LinkedList<Integer> implements IPostingList {

    // Stores the single termID this PostingList is referring to.
    // Used by insertSortedByTermSimilarity() in case this
    // PostingList is sorted by term similarity.
    private List<Integer> referenceTermIDs;

    /**
     * Constructor used in case this is a PostingList sorted by timestamp or significance
     */
    public PostingList() {}

    /**
     * Constructor used in case this is a PostingList sorted by term similarity
     */
    public PostingList(int referenceTermID) {
        this.referenceTermIDs = new ArrayList<Integer>(1);
        this.referenceTermIDs.add(referenceTermID);
    }

    /**
     * Inserts a tweetID according to sorting by timestamp
     *
     * @param  tweetID  TweetID to be inserted
     */
    public void insertSortedByTimestamp(int insertTweetID) {
        ListIterator<Integer> iterator = listIterator();

        // Fetch sort key for insertion object
        Date insertTimestamp = TweetDictionary.getTransportObject(insertTweetID).getTimestamp();

        while (true) {
            if (!iterator.hasNext()) {
                iterator.add(insertTweetID);
                return;
            }

            // Fetch tweetID in list and its sort key
            int listTweetID = iterator.next();
            Date listTimestamp = TweetDictionary.getTransportObject(listTweetID).getTimestamp();

            if (insertTimestamp.after(listTimestamp)) {
                iterator.previous();
                iterator.add(insertTweetID);
                return;
            }
        }
    }

    /**
     * Inserts a tweetID according to sorting by significance
     *
     * @param  tweetID  TweetID to be inserted
     */
    public void insertSortedBySignificance(int insertTweetID) {
        ListIterator<Integer> iterator = listIterator();

        // Fetch sort key for insertion object
        float insertSignificance = TweetDictionary.getTransportObject(insertTweetID).getSignificance();

        while (true) {
            if (!iterator.hasNext()) {
                iterator.add(insertTweetID);
                return;
            }

            // Fetch tweetID in list and its sort key
            int listTweetID = iterator.next();
            float listSignificance = TweetDictionary.getTransportObject(listTweetID).getSignificance();

            if (listSignificance > insertSignificance) {
                iterator.previous();
                iterator.add(insertTweetID);
                return;
            }
        }
    }

    /**
     * Inserts a tweetID according to sorting by term similarity
     *
     * @param  tweetID  TweetID to be inserted
     */
    public void insertSortedByTermSimilarity(int insertTweetID) {
        ListIterator<Integer> iterator = listIterator();

        // Calculate term similarity between termIDs from insertion tweet ID
        // and reference term ID
        List<Integer> insertTermIDs = TweetDictionary.getTransportObject(insertTweetID).getTermIDs();
        float insertTermSimilarity = HelperFunctions.calculateTermSimilarity(insertTermIDs, this.referenceTermIDs);

        while (true) {
            if (!iterator.hasNext()) {
                iterator.add(insertTweetID);
                return;
            }

            // Fetch tweetID in list and its sort key
            int listTweetID = iterator.next();
            List<Integer> listTermIDs = TweetDictionary.getTransportObject(listTweetID).getTermIDs();
            float listTermSimilarity = HelperFunctions.calculateTermSimilarity(listTermIDs, this.referenceTermIDs);

            if (listTermSimilarity > insertTermSimilarity) {
                iterator.previous();
                iterator.add(insertTweetID);
                return;
            }
        }
    }
}
