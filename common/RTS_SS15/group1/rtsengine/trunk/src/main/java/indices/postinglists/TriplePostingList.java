package indices.postinglists;

import indices.postinglists.SortedPostingList;

/**
 * Linking class for the three linked posting lists in LSII
 */
public class TriplePostingList {

    private SortedPostingList freshnessPostingList;
    private SortedPostingList significancePostingList;
    private SortedPostingList termSimilarityPostingList;

    public TriplePostingList() {
        this.freshnessPostingList = new SortedPostingList();
        this.significancePostingList = new SortedPostingList();
        this.termSimilarityPostingList = new SortedPostingList();
    }

    public SortedPostingList getFreshnessPostingList() {
        return freshnessPostingList;
    }

    public SortedPostingList getSignificancePostingList() {
        return significancePostingList;
    }

    public SortedPostingList getTermSimilarityPostingList() {
        return termSimilarityPostingList;
    }

}
