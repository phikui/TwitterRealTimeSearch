package indices.deprecated;

import indices.postinglists.ResultList;

/**
 * Linking class for the three linked posting lists in LSII
 */
public class TriplePostingList {

    private ResultList freshnessPostingList;
    private ResultList significancePostingList;
    private ResultList termSimilarityPostingList;

    public TriplePostingList() {
        this.freshnessPostingList = new ResultList();
        this.significancePostingList = new ResultList();
        this.termSimilarityPostingList = new ResultList();
    }

    public ResultList getFreshnessPostingList() {
        return freshnessPostingList;
    }

    public ResultList getSignificancePostingList() {
        return significancePostingList;
    }

    public ResultList getTermSimilarityPostingList() {
        return termSimilarityPostingList;
    }

}
