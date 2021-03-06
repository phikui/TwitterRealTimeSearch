package indices.postinglists;

/**
 * Created by chans on 6/5/15.
 */
public interface ITriplePostingList {

    IPostingList getFreshnessPostingList();

    IPostingList getSignificancePostingList();

    IPostingList getTermSimilarityPostingList();

    void setFreshnessPostingList(IPostingList freshnessPostingList);

    void setSignificancePostingList(IPostingList significancePostingList);

    void setTermSimilarityPostingList(IPostingList termSimilarityPostingList);

}
