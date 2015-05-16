package indices.lsii;

import indices.IRTSIndex;
import model.TransportObject;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

/**
 * Created by chans on 5/14/15.
 */
public class TriplePostingListIndex implements IRTSIndex {

    private HashMap<Integer, TriplePostingList> invertedIndex;

    public TriplePostingListIndex() {
        this.invertedIndex = new HashMap<Integer, TriplePostingList>();
    }

    public List<Integer> searchTweetIDs(TransportObject transportObjectQuery) {
        int k = transportObjectQuery.getk();

        // TODO
        List<Integer> topKTweetIDs = new ArrayList<Integer>();
        SortedPostingList currentTopK = new SortedPostingList();
        List<Integer> termIDs = transportObjectQuery.getTermIDs();
        int currentTweetID;

        // max values for TA to calculate threshold
        float maxFreshness;
        float maxSignificance;
        float maxSimilarity;

        float currFreshness = 0;
        float currSignificance = 0;
        float currSimilarity = 0;

        float fValue = 0;
        float threshold;


        for (int termID: termIDs){
            TriplePostingList triplePostingListForTermID = this.invertedIndex.get(termID);

            // perform Threshold Algorithm
            // list-size of freshness = similarity = significance for the same term
            for(int i = 0; i < triplePostingListForTermID.getSignificancePostingList().size(); i++){
                currentTweetID = triplePostingListForTermID.getFreshnessPostingList().get(i).getTweetID();
                maxFreshness = triplePostingListForTermID.getFreshnessPostingList().get(i).getSortKey();

                // for TA we would need to look up sim and sig for the current tweetID and then calculate f:
                // currSignificance = TweetDictionary.getTransportObject(currentTweetID).getSignificance();
                // currSimilarity = TweetDictionary.getTransportObject(currentTweetID).getSimilarity();
                // fValue = calculateRankingFunction(maxFreshness, currSignificance, currSimilarity);
                if(!currentTopK.contains(currentTweetID)){
                    currentTopK.insertSorted(currentTweetID, fValue);
                }

                currentTweetID = triplePostingListForTermID.getSignificancePostingList().get(i).getTweetID();
                maxSignificance = triplePostingListForTermID.getSignificancePostingList().get(i).getSortKey();

                // for TA we would need to look up fresh and sig for the current tweetID and then calculate f:
                // currFreshness = TweetDictionary.getTransportObject(currentTweetID).getFreshness();
                // currSimilarity = TweetDictionary.getTransportObject(currentTweetID).getSimilarity();
                // fValue = calculateRankingFunction(currFreshness, maxSignificance, currSimilarity);
                if(!currentTopK.contains(currentTweetID)){
                    currentTopK.insertSorted(currentTweetID, fValue);
                }

                currentTweetID = triplePostingListForTermID.getTermSimilarityPostingList().get(i).getTweetID();
                maxSimilarity = triplePostingListForTermID.getTermSimilarityPostingList().get(i).getSortKey();

                // for TA we would need to look up fresh and sim for the current tweetID and then calculate f:
                // currFreshness = TweetDictionary.getTransportObject(currentTweetID).getFreshness();
                // currSignificance = TweetDictionary.getTransportObject(currentTweetID).getSignificance();
                // fValue = calculateRankingFunction(currFreshness, currSignificance, maxSimilarity);
                if(!currentTopK.contains(currentTweetID)){
                    currentTopK.insertSorted(currentTweetID, fValue);
                }

                threshold = calculateRankingFunction(maxFreshness, maxSignificance, maxSimilarity);
                if((currentTopK.get((k-1)).getSortKey() >= threshold) && (currentTopK.get((k-1)) != null)){
                    break;
                }

            }

        }

        // shorten to top-k elements and copy to arrayList, this may be done better as it currently is O(list.size())
        currentTopK.subList((k-1), currentTopK.size()).clear();
        for(int j = 0; j < k; j++){
            topKTweetIDs.add(currentTopK.get(j).getTweetID());
        }

        return topKTweetIDs;
    }

    public void insertTransportObject(TransportObject transportObjectInsertion) {

        // extract the important information from the transport object
        int tweetID = transportObjectInsertion.getTweetID();

        // Obtain significance and freshness from the transportObject
        float significance = transportObjectInsertion.getSignificance();
        float freshness = transportObjectInsertion.calculateFreshness();

        List<Integer> termIDs = transportObjectInsertion.getTermIDs();

        for (int termID: termIDs) {
            TriplePostingList triplePostingListForTermID = this.invertedIndex.get(termID);

            // Create PostingList for this termID if necessary
            if (triplePostingListForTermID == null) {
                triplePostingListForTermID = new TriplePostingList();
                this.invertedIndex.put(termID, triplePostingListForTermID);
            }

            // Calculate term similarity between this current term and
            // transportObject's termIDs
            List<Integer> singleTermIDList = new ArrayList<Integer>(1);
            singleTermIDList.add(termID);
            float similarity = transportObjectInsertion.calculateTermSimilarity(singleTermIDList);

            // Insert tweetID into posting lists for this term sorted on the key
            triplePostingListForTermID.getSignificancePostingList().insertSorted(tweetID, significance);
            triplePostingListForTermID.getFreshnessPostingList().insertSorted(tweetID, freshness);
            triplePostingListForTermID.getTermSimilarityPostingList().insertSorted(tweetID, similarity);
        }
    }

    /**
     * calculates the ranking function f using same weights of 1/3 to get values in range [0;1]
     *
     * @param freshness
     * @param significance
     * @param similarity
     * @return
     */
    public float calculateRankingFunction(float freshness, float significance, float similarity){
        float f;
        float weight_fresh = (1/3);
        float weight_sig = (1/3);
        float weight_sim = (1/3);

        f = weight_fresh * freshness + weight_sig * significance + weight_sim * similarity;

        return f;
    }

}
