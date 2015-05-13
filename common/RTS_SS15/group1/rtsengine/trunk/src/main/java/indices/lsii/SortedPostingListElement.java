package indices.lsii;

/**
 * Created by chans on 5/14/15.
 */
public class SortedPostingListElement {

    // Posting List will be sorted according to this key
    // Either used for significance, term similarity or freshness
    private float sortKey;
    private int tweetID;

    public SortedPostingListElement(int tweetID, float sortKey) {
        this.sortKey = sortKey;
        this.tweetID = tweetID;
    }

    public float getSortKey() {
        return sortKey;
    }

    public int getTweetID() {
        return tweetID;
    }
}
