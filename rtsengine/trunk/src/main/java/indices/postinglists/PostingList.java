package indices.postinglists;

import model.TweetDictionary;
import utilities.HelperFunctions;

import java.util.*;

/**
 * Created by chans on 6/5/15.
 */
public class PostingList extends LinkedList<IPostingListElement> implements IPostingList {
    // Stores the single termID this PostingList is referring to.
    // Used by insertSortedByTermSimilarity() in case this
    // PostingList is sorted by term similarity.
    private List<Integer> referenceTermIDs;

    private static int postingListIDCounter = 0;

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
     * Returns a unique ID for this PostingList
     */
    public int getPostingListID() {
        int postingListID = postingListIDCounter;
        postingListIDCounter++;
        return postingListID;
    }

    /**
     * Inserts a tweetID according to sorting by timestamp
     *
     * @param  insertTweetID  TweetID to be inserted
     */
    public void insertSortedByTimestamp(int insertTweetID) {
        Date insertTimestamp = TweetDictionary.getTransportObject(insertTweetID).getTimestamp();
        float insertSortKey = (float) insertTimestamp.getTime();
        this.insertSorted(insertTweetID, insertSortKey);
    }

    /**
     * Inserts a tweetID according to sorting by significance
     *
     * @param  insertTweetID  TweetID to be inserted
     */
    public void insertSortedBySignificance(int insertTweetID) {
        float insertSortKey = TweetDictionary.getTransportObject(insertTweetID).getSignificance();
        this.insertSorted(insertTweetID, insertSortKey);
    }

    /**
     * Inserts a tweetID according to sorting by term similarity
     *
     * @param  insertTweetID  TweetID to be inserted
     */
    public void insertSortedByTermSimilarity(int insertTweetID) {
        // Calculate term similarity between termIDs from insertion tweet ID
        // and reference term ID
        List<Integer> insertTermIDs = TweetDictionary.getTransportObject(insertTweetID).getTermIDs();
        float insertSortKey = HelperFunctions.calculateTermSimilarity(insertTermIDs, this.referenceTermIDs);

        this.insertSorted(insertTweetID, insertSortKey);
    }

    /**
     * This function inserts the tweetID sorted according to sortKey should be O(1) on already sorted lists
     *
     * @param tweetID
     * @param sortKey
     */
    public void insertSorted(int tweetID, float sortKey) {
        IPostingListElement sortElement = new PostingListElement(tweetID, sortKey);
        ListIterator<IPostingListElement> iterator = listIterator();
        while(true) {
            if (!iterator.hasNext()) {
                iterator.add(sortElement);
                return;
            }

            IPostingListElement elementInList = iterator.next();
            if (elementInList.getSortKey() < sortElement.getSortKey()) {
                iterator.previous();
                iterator.add(sortElement);
                return;
            }
        }
    }

    public boolean containsTweetID(int tweetID) {
        ListIterator<IPostingListElement> iterator = listIterator();

        while (true){
            if (!iterator.hasNext()) {
                return false;
            }

            IPostingListElement elementInList = iterator.next();

            if (elementInList.getTweetID() == tweetID) {
                return true;
            }
        }
    }

    public IPostingListElement getPostingListElement(int tweetID) {
        ListIterator<IPostingListElement> iterator = listIterator();

        while (true){
            if (!iterator.hasNext()) {
                return null;
            }

            IPostingListElement elementInList = iterator.next();

            if (elementInList.getTweetID() == tweetID) {
                return elementInList;
            }
        }
    }

    /**
     * Returns TweetIDs stored in this ResultList as ArrayList (in same order)
     */
    public List<Integer> getTweetIDs() {
        List<Integer> tweetIDList = new ArrayList<Integer>(this.size());

        ListIterator<IPostingListElement> iterator = listIterator();

        while(true){
            if (!iterator.hasNext()) {
                break;
            }

            IPostingListElement elementInList = iterator.next();
            tweetIDList.add(elementInList.getTweetID());
        }

        return tweetIDList;
    }
}
