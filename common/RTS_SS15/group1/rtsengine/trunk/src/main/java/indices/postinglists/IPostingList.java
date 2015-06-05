package indices.postinglists;

import java.util.Iterator;

/**
 * Created by chans on 6/5/15.
 */
public interface IPostingList {

    /**
     * Inserts a tweetID according to sorting by timestamp
     *
     * @param  tweetID  TweetID to be inserted
     */
    void insertSortedByTimestamp(int insertTweetID);

    /**
     * Inserts a tweetID according to sorting by significance
     *
     * @param  tweetID  TweetID to be inserted
     */
    void insertSortedBySignificance(int insertTweetID);

    /**
     * Inserts a tweetID according to sorting by term similarity
     *
     * @param  tweetID  TweetID to be inserted
     */
    void insertSortedByTermSimilarity(int insertTweetID);

    Iterator<Integer> iterator();

    int size();

}
