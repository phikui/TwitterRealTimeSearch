package indices.postinglists;

/**
 * Created by chans on 6/6/15.
 */
public class PostingListElement implements IPostingListElement {
    private float sortKey;
    private int tweetID;

    public PostingListElement(int tweetID, float sortKey) {
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
