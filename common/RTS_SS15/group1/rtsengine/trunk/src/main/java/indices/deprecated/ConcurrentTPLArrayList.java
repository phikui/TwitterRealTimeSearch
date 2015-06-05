package indices.deprecated;

/**
 * Created by Maik on 28.05.2015.
 */
public class ConcurrentTPLArrayList {

    private ConcurrentSortDateArrayList freshnessPostingList;
    private ConcurrentSortedArrayList significancePostingList;
    private ConcurrentSortedArrayList termSimilarityPostingList;

    public ConcurrentTPLArrayList() {
        this.freshnessPostingList = new ConcurrentSortDateArrayList();
        this.significancePostingList = new ConcurrentSortedArrayList();
        this.termSimilarityPostingList = new ConcurrentSortedArrayList();
    }

    public ConcurrentSortDateArrayList getFreshnessPostingList() {
        return freshnessPostingList;
    }

    public void setFreshnessPostingList(ConcurrentSortDateArrayList freshnessPostingList) {
        this.freshnessPostingList = freshnessPostingList;
    }

    public ConcurrentSortedArrayList getSignificancePostingList() {
        return significancePostingList;
    }

    public void setSignificancePostingList(ConcurrentSortedArrayList significancePostingList) {
        this.significancePostingList = significancePostingList;
    }

    public ConcurrentSortedArrayList getTermSimilarityPostingList() {
        return termSimilarityPostingList;
    }

    public void setTermSimilarityPostingList(ConcurrentSortedArrayList termSimilarityPostingList) {
        this.termSimilarityPostingList = termSimilarityPostingList;
    }
}
