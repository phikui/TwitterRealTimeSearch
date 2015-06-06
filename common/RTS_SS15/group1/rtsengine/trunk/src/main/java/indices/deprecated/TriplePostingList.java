package indices.deprecated;

import indices.postinglists.IPostingList;
import indices.postinglists.PostingList;

/**
 * Linking class for the three linked posting lists in LSII
 */
public class TriplePostingList {

    private IPostingList freshnessPostingList;
    private IPostingList significancePostingList;
    private IPostingList termSimilarityPostingList;

    public TriplePostingList() {
        this.freshnessPostingList = new PostingList();
        this.significancePostingList = new PostingList();
        this.termSimilarityPostingList = new PostingList();
    }

    public IPostingList getFreshnessPostingList() {
        return freshnessPostingList;
    }

    public IPostingList getSignificancePostingList() {
        return significancePostingList;
    }

    public IPostingList getTermSimilarityPostingList() {
        return termSimilarityPostingList;
    }

}
