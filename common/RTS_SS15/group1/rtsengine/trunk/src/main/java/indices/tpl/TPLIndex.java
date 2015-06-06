package indices.tpl;

import indices.IRTSIndex;
import indices.postinglists.*;
import model.TransportObject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by chans on 5/14/15.
 */
public class TPLIndex implements IRTSIndex {

    private ConcurrentHashMap<Integer, ITriplePostingList> invertedIndex;

    public TPLIndex() {
        this.invertedIndex = new ConcurrentHashMap<Integer, ITriplePostingList>();
    }

    public List<Integer> searchTweetIDs(TransportObject transportObjectQuery) {
        // Stores result tweetIDs along with their calculated f score value
        IPostingList resultList = new PostingList();

        // Stores Iterator for each PostingList that has already been examined
        HashMap<Integer, Iterator<IPostingListElement>> postingListIteratorMap = new HashMap<Integer, Iterator<IPostingListElement>>();

        float upperBoundScore;

        // Threshold algorithm main loop
        while (true) {
            // Abort search when all involved PostingLists have reached the end
            try {
                upperBoundScore = TPLHelper.examineTPLIndex(this.invertedIndex, postingListIteratorMap, transportObjectQuery, resultList);
                System.out.println("Upper Bound Score: " + upperBoundScore);
            } catch (IndexOutOfBoundsException e) {
                break;
            }

            // Abort search early in case upper bound score falls below score of last entry in ResultList
            if (resultList.size() > 0) {
                float smallestScoreInResultList  = resultList.getLast().getSortKey();
                if (upperBoundScore < smallestScoreInResultList) {
                    break;
                }
            }
        }

        return resultList.getTweetIDs();
    }

    /**
     *  Works with new list structure using Concurrent Array lists / CopyOnWriteArrayList
     *
     * @param transportObjectInsertion
     */
    public void insertTransportObject(TransportObject transportObjectInsertion) {
        // Obtain transportObject properties
        int tweetID = transportObjectInsertion.getTweetID();
        List<Integer> termIDs = transportObjectInsertion.getTermIDs();

        for (int referenceTermID : termIDs) {
            ITriplePostingList triplePostingListForTermID = this.invertedIndex.get(referenceTermID);

            // Create PostingList for this termID if necessary
            if (triplePostingListForTermID == null) {
                // Pass reference termID to TriplePostingList, which in turn
                // passes it to the PostingList for term similarity
                triplePostingListForTermID = new TriplePostingList(referenceTermID);
                this.invertedIndex.put(referenceTermID, triplePostingListForTermID);
            }

            // insert tweetID sorted on float values into index posting lists
            triplePostingListForTermID.getFreshnessPostingList().insertSortedByTimestamp(tweetID);
            triplePostingListForTermID.getSignificancePostingList().insertSortedBySignificance(tweetID);
            triplePostingListForTermID.getTermSimilarityPostingList().insertSortedByTermSimilarity(tweetID);
        }
    }

    public int size() {
        return this.invertedIndex.size();
    }
}
