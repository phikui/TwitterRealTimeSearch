package indices.postinglists;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by chans on 6/5/15.
 */
public interface IPostingList {

    /**
     * Returns a unique ID for this PostingList
     */
    int getPostingListID();

    /**
     * Inserts a tweetID according to sorting by timestamp
     *
     * @param  insertTweetID  TweetID to be inserted
     */
    void insertSortedByTimestamp(int insertTweetID);

    /**
     * Inserts a tweetID according to sorting by significance
     *
     * @param  insertTweetID  TweetID to be inserted
     */
    void insertSortedBySignificance(int insertTweetID);

    /**
     * Inserts a tweetID according to sorting by term similarity
     *
     * @param  insertTweetID  TweetID to be inserted
     */
    void insertSortedByTermSimilarity(int insertTweetID);

    /**
     * This function inserts the tweetID sorted according to sortKey
     *
     * @param tweetID
     * @param sortKey
     */
    void insertSorted(int tweetID, float sortKey);

    boolean containsTweetID(int tweetID);

    IPostingListElement getPostingListElement(int tweetID);

    /**
     * Returns TweetIDs stored in this PostingList as ArrayList (in same order)
     */
    List<Integer> getTweetIDs();

    void addFirst(IPostingListElement element);

    boolean remove(Object o);

    IPostingListElement get(int index);

    void clear();

    IPostingListElement getLast();

    Iterator<IPostingListElement> iterator();

    int size();
}
