package indices.postinglists;

public class TriplePostingList implements ITriplePostingList {
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
