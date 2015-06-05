package indices.postinglists;

/**
 * Created by chans on 5/14/15.
 */
public class ResultListElement {

    private int tweetID;

    // ResultList will be sorted according to this key
    // Either used for significance, term similarity or freshness
    private float sortKey;

    public ResultListElement(int tweetID, float sortKey) {
        this.tweetID = tweetID;
        this.sortKey = sortKey;
    }

    public int getTweetID() {
        return tweetID;
    }

    public float getSortKey() {
        return sortKey;
    }
}
