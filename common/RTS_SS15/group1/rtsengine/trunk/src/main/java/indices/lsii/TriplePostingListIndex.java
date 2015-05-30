package indices.lsii;

import indices.IRTSIndex;
import indices.postingarraylists.ConcurrentTPLArrayList;
import indices.postinglists.ConcurrentSortedDateListElement;
import indices.postinglists.ConcurrentSortedPostingListElement;
import indices.postinglists.LSIITriplet;
import model.TransportObject;
import utilities.HelperFunctions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import indices.postinglists.ConcurrentTriplePostingList;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chans on 5/14/15.
 */
public class TriplePostingListIndex implements IRTSIndex {

    private ConcurrentHashMap<Integer, ConcurrentTriplePostingList> invertedIndex;

    private ConcurrentHashMap<Integer, LSIITriplet> tripletHashMap;

    /*
        Testing purpose only
     */
    private ConcurrentHashMap<Integer, ConcurrentTPLArrayList> invertedIndex2;

    public TriplePostingListIndex() {
        this.invertedIndex = new ConcurrentHashMap<Integer, ConcurrentTriplePostingList>();
        this.tripletHashMap = new ConcurrentHashMap<Integer, LSIITriplet>();

        /*
            Testing purpose only
         */
        this.invertedIndex2 = new ConcurrentHashMap<>();
    }

    public List<Integer> searchTweetIDs(TransportObject transportObjectQuery) {
        int k = transportObjectQuery.getk();
        List<Integer> topKTweetIDs = new ArrayList<Integer>();

        // TODO
        /*
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

        float fValue;
        float threshold;


        for (int termID: termIDs){
            ConcurrentTriplePostingList triplePostingListForTermID = this.invertedIndex.get(termID);

            // perform Threshold Algorithm
            // list-size of freshness = similarity = significance for the same term
            for(int i = 0; i < triplePostingListForTermID.getSignificancePostingList().size(); i++){
                currentTweetID = triplePostingListForTermID.getFreshnessPostingList().get(i).getTweetID();
                maxFreshness = triplePostingListForTermID.getFreshnessPostingList().get(i).getSortKey();

                // for TA we would need to look up sim and sig for the current tweetID and then calculate f:
                for(int j = 0; j < triplePostingListForTermID.getSignificancePostingList().size(); j++){
                    if (triplePostingListForTermID.getSignificancePostingList().get(j).getTweetID() == currentTweetID)
                        currSignificance = triplePostingListForTermID.getSignificancePostingList().get(j).getSortKey();
                }
                for (int l = 0; l < triplePostingListForTermID.getTermSimilarityPostingList().size(); l++){
                    if (triplePostingListForTermID.getTermSimilarityPostingList().get(l).getTweetID() == currentTweetID){
                        currSimilarity = triplePostingListForTermID.getTermSimilarityPostingList().get(l).getSortKey();
                    }
                }
                // currSignificance = HelperFunctions.calculateSignificance(TweetDictionary.getTweetObject(currentTweetID));
                // currSimilarity = HelperFunctions.calculateTermSimilarity(termIDs, TweetDictionary.getTweetObject(currentTweetID).getText() );
                fValue = HelperFunctions.calculateRankingFunction(maxFreshness, currSignificance, currSimilarity);
                if(!currentTopK.containsTweetID(currentTweetID)){
                    currentTopK.insertSorted(currentTweetID, fValue);
                }

                currentTweetID = triplePostingListForTermID.getSignificancePostingList().get(i).getTweetID();
                maxSignificance = triplePostingListForTermID.getSignificancePostingList().get(i).getSortKey();

                // for TA we would need to look up fresh and sig for the current tweetID and then calculate f:
                for(int j = 0; j < triplePostingListForTermID.getFreshnessPostingList().size(); j++){
                    if (triplePostingListForTermID.getFreshnessPostingList().get(j).getTweetID() == currentTweetID)
                        currFreshness = triplePostingListForTermID.getFreshnessPostingList().get(j).getSortKey();
                }
                for (int l = 0; l < triplePostingListForTermID.getTermSimilarityPostingList().size(); l++){
                    if (triplePostingListForTermID.getTermSimilarityPostingList().get(l).getTweetID() == currentTweetID){
                        currSimilarity = triplePostingListForTermID.getTermSimilarityPostingList().get(l).getSortKey();
                    }
                }

                // currFreshness = TweetDictionary.getTransportObject(currentTweetID).getFreshness();
                // currSimilarity = TweetDictionary.getTransportObject(currentTweetID).getSimilarity();
                fValue = HelperFunctions.calculateRankingFunction(currFreshness, maxSignificance, currSimilarity);
                if(!currentTopK.containsTweetID(currentTweetID)){
                    currentTopK.insertSorted(currentTweetID, fValue);
                }

                currentTweetID = triplePostingListForTermID.getTermSimilarityPostingList().get(i).getTweetID();
                maxSimilarity = triplePostingListForTermID.getTermSimilarityPostingList().get(i).getSortKey();

                // for TA we would need to look up fresh and sim for the current tweetID and then calculate f:
                for(int j = 0; j < triplePostingListForTermID.getFreshnessPostingList().size(); j++){
                    if (triplePostingListForTermID.getFreshnessPostingList().get(j).getTweetID() == currentTweetID)
                        currFreshness = triplePostingListForTermID.getFreshnessPostingList().get(j).getSortKey();
                }
                for (int l = 0; l < triplePostingListForTermID.getSignificancePostingList().size(); l++){
                    if (triplePostingListForTermID.getSignificancePostingList().get(l).getTweetID() == currentTweetID){
                        currSignificance = triplePostingListForTermID.getSignificancePostingList().get(l).getSortKey();
                    }
                }

                // currFreshness = TweetDictionary.getTransportObject(currentTweetID).getFreshness();
                // currSignificance = TweetDictionary.getTransportObject(currentTweetID).getSignificance();
                fValue = HelperFunctions.calculateRankingFunction(currFreshness, currSignificance, maxSimilarity);
                if(!currentTopK.containsTweetID(currentTweetID)){
                    currentTopK.insertSorted(currentTweetID, fValue);
                }

                threshold = HelperFunctions.calculateRankingFunction(maxFreshness, maxSignificance, maxSimilarity);
                if (currentTopK.size() >= k){
                    if (currentTopK.get((k-1)).getSortKey() >= threshold)
                        break;
                }

            }

        }

        // shorten to top-k elements and copy to arrayList, this may be done better as it currently is O(list.size())
        if(currentTopK.size() >= k)
            currentTopK.subList(0, (k-1)).clear();
        for(int j = 0; j < currentTopK.size(); j++){
            topKTweetIDs.add(currentTopK.get(j).getTweetID());
        }
        */
        return topKTweetIDs;
    }

    /**
     *  Works in combination with the insertObjectNew-method. Query-search using the new list structure
     *
     * @param transportObjectQuery
     * @return
     */
    public List<Integer> searchTweetIDsNew(TransportObject transportObjectQuery) {

        SortedPostingList candidatePool = new SortedPostingList();
        int k = transportObjectQuery.getk();
        List<Integer> termIDsInQuery = transportObjectQuery.getTermIDs();

        List<Integer> topKTweetIDs = new ArrayList<Integer>();

        int currentTweetID;

        List<Integer> singleTermIDList = new ArrayList<Integer>(1);

        // term values
        Date termDate;
        float termTermSimilarity;

        // query values
        Date queryDate = transportObjectQuery.getTimestamp();

        // values to calculate new threshold
        float maxFreshness;
        float maxSignificance;
        float maxSimilarity;
        float threshold;

        float queryFreshness;
        float querySignificance;
        float queryTermSimilarity;
        float fValue;

        for (int termID : termIDsInQuery) {
            ConcurrentTPLArrayList triplePostingListForTermID = this.invertedIndex2.get(termID);

            // single term list for TermSimilarity
            singleTermIDList.clear();
            singleTermIDList.add(termID);

            // perform Threshold Algorithm
            // list-size of freshness = similarity = significance for the same term
            for (int i = 0; i < triplePostingListForTermID.getSignificancePostingList().size(); i++) {

                /*
                    FValue computation for TweetID in FreshnessList
                */
                currentTweetID = triplePostingListForTermID.getFreshnessPostingList().get(i).getTweetID();

                // fValue computation
                termDate = tripletHashMap.get(currentTweetID).getDate();
                termTermSimilarity = tripletHashMap.get(currentTweetID).getTermSimilarity();
                queryFreshness = HelperFunctions.calculateFreshness(termDate, queryDate);
                querySignificance = tripletHashMap.get(currentTweetID).getSignificance();
                queryTermSimilarity = HelperFunctions.calculateTermSimilarity(singleTermIDList, termIDsInQuery);
                queryTermSimilarity = queryTermSimilarity * termTermSimilarity;

                fValue = HelperFunctions.calculateRankingFunction(queryFreshness, querySignificance, queryTermSimilarity);
                maxFreshness = queryFreshness;

                if (!candidatePool.containsTweetID(currentTweetID, fValue)) {
                    candidatePool.insertSorted(currentTweetID, fValue);
                    //candidatePool.removeFirstDuplicate(currentTweetID, fValue);
                }

                /*
                    FValue computation for TweetID in SignificanceList
                */
                currentTweetID = triplePostingListForTermID.getSignificancePostingList().get(i).getTweetID();

                // fValue computation
                termDate = tripletHashMap.get(currentTweetID).getDate();
                termTermSimilarity = tripletHashMap.get(currentTweetID).getTermSimilarity();
                queryFreshness = HelperFunctions.calculateFreshness(termDate, queryDate);
                querySignificance = tripletHashMap.get(currentTweetID).getSignificance();
                queryTermSimilarity = HelperFunctions.calculateTermSimilarity(singleTermIDList, termIDsInQuery);
                queryTermSimilarity = queryTermSimilarity * termTermSimilarity;

                fValue = HelperFunctions.calculateRankingFunction(queryFreshness, querySignificance, queryTermSimilarity);
                maxSignificance = querySignificance;

                if (!candidatePool.containsTweetID(currentTweetID, fValue)) {
                    candidatePool.insertSorted(currentTweetID, fValue);
                    //candidatePool.removeFirstDuplicate(currentTweetID, fValue);
                }

                /*
                    FValue computation for TweetID in TermSimilarityList
                */
                currentTweetID = triplePostingListForTermID.getTermSimilarityPostingList().get(i).getTweetID();

                // fValue computation
                termDate = tripletHashMap.get(currentTweetID).getDate();
                termTermSimilarity = tripletHashMap.get(currentTweetID).getTermSimilarity();
                queryFreshness = HelperFunctions.calculateFreshness(termDate, queryDate);
                querySignificance = tripletHashMap.get(currentTweetID).getSignificance();
                queryTermSimilarity = HelperFunctions.calculateTermSimilarity(singleTermIDList, termIDsInQuery);
                queryTermSimilarity = queryTermSimilarity * termTermSimilarity;

                fValue = HelperFunctions.calculateRankingFunction(queryFreshness, querySignificance, queryTermSimilarity);
                maxSimilarity = queryTermSimilarity;

                if (!candidatePool.containsTweetID(currentTweetID, fValue)) {
                    candidatePool.insertSorted(currentTweetID, fValue);
                    //candidatePool.removeFirstDuplicate(currentTweetID, fValue);
                }


                // new threshold computation
                threshold = HelperFunctions.calculateRankingFunction(maxFreshness, maxSignificance, maxSimilarity);

                // check if smallest top-k element is greater than new threshold, if yes we get to the next term, otherwise continue
                if (candidatePool.size() >= k) {
                    if (candidatePool.get((k - 1)).getSortKey() > threshold) {
                        System.out.println("Threshold is too small, terminate early");
                        break;
                    }
                } else if (candidatePool.getLast().getSortKey() > threshold) {
                    System.out.println("Threshold is too small, terminate early2");
                    break;
                }

            }

        }

        // shorten to top-k elements and copy to arrayList, this may be done better as it currently is O(list.size())
        if (candidatePool.size() >= k)
            candidatePool.subList(k, candidatePool.size()).clear();
        for (int l = 0; l < candidatePool.size(); l++) {

            topKTweetIDs.add(candidatePool.get(l).getTweetID());
        }


        return topKTweetIDs;
    }

    /*
    public void insertTransportObject(TransportObject transportObjectInsertion) {

        // extract the important information from the transport object
        int tweetID = transportObjectInsertion.getTweetID();

        // Obtain significance and freshness from the transportObject
        float significance = transportObjectInsertion.getSignificance();
        Date freshness = transportObjectInsertion.getTimestamp();

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
    }*/

    public void insertTransportObject(TransportObject transportObjectInsertion) {

        // extract the important information from the transport object
        int tweetID = transportObjectInsertion.getTweetID();

        // Obtain significance and freshness from the transportObject
        float significance = transportObjectInsertion.getSignificance();
        Date freshness = transportObjectInsertion.getTimestamp();

        List<Integer> termIDs = transportObjectInsertion.getTermIDs();

        for (int termID : termIDs) {
            ConcurrentTriplePostingList triplePostingListForTermID = this.invertedIndex.get(termID);

            // Create PostingList for this termID if necessary
            if (triplePostingListForTermID == null) {
                triplePostingListForTermID = new ConcurrentTriplePostingList();
                this.invertedIndex.put(termID, triplePostingListForTermID);
            }

            // Calculate term similarity between this current term and
            // transportObject's termIDs
            List<Integer> singleTermIDList = new ArrayList<Integer>(1);
            singleTermIDList.add(termID);
            float similarity = transportObjectInsertion.calculateTermSimilarity(singleTermIDList);

            // Insert tweetID into posting lists for this term sorted on the key
            triplePostingListForTermID.getSignificancePostingList().add(new ConcurrentSortedPostingListElement(tweetID, significance));
            triplePostingListForTermID.getFreshnessPostingList().add(new ConcurrentSortedDateListElement(tweetID, freshness));
            triplePostingListForTermID.getTermSimilarityPostingList().add(new ConcurrentSortedPostingListElement(tweetID, similarity));
        }
    }

    /**
     *  Works with new list structure using Concurrent Array lists / CopyOnWriteArrayList
     *
     * @param transportObjectInsertion
     */
    public void insertTransportObjectNew(TransportObject transportObjectInsertion) {
        int tweetID = transportObjectInsertion.getTweetID();

        // Obtain significance and freshness from the transportObject
        float significance = transportObjectInsertion.getSignificance();
        Date freshness = transportObjectInsertion.getTimestamp();
        float similarity;
        List<Integer> singleTermIDList = new ArrayList<Integer>(1);

        List<Integer> termIDs = transportObjectInsertion.getTermIDs();

        for (int termID : termIDs) {
            ConcurrentTPLArrayList triplePostingListForTermID = this.invertedIndex2.get(termID);

            // Create PostingList for this termID if necessary
            if (triplePostingListForTermID == null) {
                triplePostingListForTermID = new ConcurrentTPLArrayList();
                this.invertedIndex2.put(termID, triplePostingListForTermID);
            }

            singleTermIDList.clear();
            singleTermIDList.add(termID);
            similarity = transportObjectInsertion.calculateTermSimilarity(singleTermIDList);

            // insert into tripletHashmap
            tripletHashMap.put(tweetID, new LSIITriplet(freshness, significance, similarity));

            // insert tweetID sorted on float values into index postinglits
            HelperFunctions.insertSorted(triplePostingListForTermID.getFreshnessPostingList(), new ConcurrentSortedDateListElement(tweetID, freshness));
            HelperFunctions.insertSorted(triplePostingListForTermID.getSignificancePostingList(), new ConcurrentSortedPostingListElement(tweetID, significance));
            HelperFunctions.insertSorted(triplePostingListForTermID.getTermSimilarityPostingList(), new ConcurrentSortedPostingListElement(tweetID, similarity));
            //triplePostingListForTermID.getTermSimilarityPostingList().insertSorted(tweetID,similarity);
        }
    }

    public int size() {
        return this.invertedIndex.size();
    }

}
