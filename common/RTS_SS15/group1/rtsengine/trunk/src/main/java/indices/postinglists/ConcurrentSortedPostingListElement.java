package indices.postinglists;

/**
 * Created by Maik on 21.05.2015.
 */
public class ConcurrentSortedPostingListElement implements Comparable<ConcurrentSortedPostingListElement> {

    // Posting List will be sorted according to this key
    // Either used for significance or term similarity
    private float sortKey;
    private int tweetID;

    public ConcurrentSortedPostingListElement(int tweetID, float sortKey) {
        this.sortKey = sortKey;
        this.tweetID = tweetID;
    }

    public float getSortKey() {
        return sortKey;
    }

    public int getTweetID() {
        return tweetID;
    }

    public int compareTo(ConcurrentSortedPostingListElement elem) {
        if (elem.getSortKey() == this.getSortKey()) {
            return 0;
        }
        if (elem.getSortKey() > this.getSortKey()) {
            return 1;
        }
        if (elem.getSortKey() < this.getSortKey()) {
            return -1;
        }
        return -1;

    }

}
