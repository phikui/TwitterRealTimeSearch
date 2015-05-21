package indices.postinglists;


/**
 * Created by Maik on 21.05.2015.
 */
public class ConcurrentTriplePostingList {

    private ConcurrentSortedDateList freshnessPostingList;
    private ConcurrentSortedPostingList significancePostingList;
    private ConcurrentSortedPostingList termSimilarityPostingList;

    public ConcurrentTriplePostingList() {
        this.freshnessPostingList = new ConcurrentSortedDateList();
        this.significancePostingList = new ConcurrentSortedPostingList();
        this.termSimilarityPostingList = new ConcurrentSortedPostingList();
    }

    public ConcurrentSortedDateList getFreshnessPostingList() {
        return freshnessPostingList;
    }

    public ConcurrentSortedPostingList getSignificancePostingList() {
        return significancePostingList;
    }

    public ConcurrentSortedPostingList getTermSimilarityPostingList() {
        return termSimilarityPostingList;
    }

}
