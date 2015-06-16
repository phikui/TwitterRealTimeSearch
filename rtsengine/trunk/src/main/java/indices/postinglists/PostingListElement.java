package indices.postinglists;

/**
 * Created by chans on 6/6/15.
 */
public class PostingListElement implements IPostingListElement, Comparable<PostingListElement> {
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

    public int compareTo(PostingListElement elem) {
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

    @Override
    public String toString(){
        String s = "<"+tweetID+","+sortKey+">";
        return s;
    }
}
