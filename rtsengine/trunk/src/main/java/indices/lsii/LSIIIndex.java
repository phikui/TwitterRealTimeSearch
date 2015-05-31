package indices.lsii;

import indices.IRTSIndex;
import indices.postingarraylists.ConcurrentTPLArrayList;
import indices.postinglists.*;
import model.TransportObject;
import utilities.HelperFunctions;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chans on 5/14/15.
 */
public class LSIIIndex implements IRTSIndex {

    // latestTimestamp as a variable to coordinate query and writer threads
    private volatile Date latestTimestamp;

    // I_0 = index_zero, I_1 to I_m = invertedIndex
    private ConcurrentHashMap<Integer, UnsortedPostingList> index_zero;
    private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ConcurrentTriplePostingList>> invertedIndex;

    // starting size threshold
    private int sizeThreshold = 4;

    // HashMap for saving triplet information for TwitterIDs (needed for efficient queries)
    private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, LSIITriplet>> tripletHashMap;

    /*
    testing purpose only
     */
    private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ConcurrentTPLArrayList>> invertedIndex2;

    public LSIIIndex() {
        this.index_zero = new ConcurrentHashMap<Integer, UnsortedPostingList>();
        this.invertedIndex = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ConcurrentTriplePostingList>>();
        this.tripletHashMap = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, LSIITriplet>>();
    }

    public List<Integer> searchTweetIDs(TransportObject transportObjectQuery) {
        Date tsMax = latestTimestamp;
        List<Integer> topKTweetIDs = new ArrayList<Integer>();
        // TODO: test it, completely untested, also may need to adress/change the postinglist structure and improve performance

        Date queryDate = transportObjectQuery.getTimestamp();
        Date termDate;
        List<Integer> singleTermIDList = new ArrayList<Integer>(1);
        float termTermSimilarity;

        int tweetID;

        // value at position k in the candidatepool, needed for threshold comparison
        float d;
        float maxThreshold;

        // values to calculate new threshold
        float threshFreshness;
        float threshSignificance;
        float threshSimilarity;
        float newThreshold;

        float queryFreshness;
        float querySignificance;
        float queryTermSimilarity;
        float fValue;

        SortedPostingList candidatePool = new SortedPostingList();
        int k = transportObjectQuery.getk();
        List<Integer> termIDsInQuery = transportObjectQuery.getTermIDs();

        // TODO: refactor to own method
        // AO iteration based on LSII-paper. First find k microblogs in I_0 with the largest score
        for (int termID : termIDsInQuery) {
            UnsortedPostingList index_zero_iterator = this.index_zero.get(termID);

            // single term list for TermSimilarity
            singleTermIDList.clear();
            singleTermIDList.add(termID);

            // iterate through I_0
            for (int i = 0; i < index_zero_iterator.size(); i++) {
                // TODO: should we use an real iterator instead of get() method? (CH)
                tweetID = index_zero_iterator.get(i);
                termDate = tripletHashMap.get(i).get(tweetID).getTimestamp();

                // stop if the date of the microblog equals the largest timestamp (prevent reader/writer conflict)
                if (termDate == tsMax) break;

                // f-Value calculations
                termTermSimilarity = tripletHashMap.get(i).get(tweetID).getTermSimilarity();

                queryFreshness = HelperFunctions.calculateFreshness(termDate, queryDate);
                querySignificance = tripletHashMap.get(i).get(tweetID).getSignificance();
                queryTermSimilarity = HelperFunctions.calculateTermSimilarity(singleTermIDList, termIDsInQuery);
                queryTermSimilarity = queryTermSimilarity * termTermSimilarity;

                fValue = HelperFunctions.calculateRankingFunction(queryFreshness, querySignificance, queryTermSimilarity);
                if (!candidatePool.containsTweetID(tweetID)) {
                    candidatePool.insertSorted(tweetID, fValue);
                }

            }
        }

        // set d to smallest value in top-k
        // TODO: Why can the candidate pool contain more than k entries?
        if (candidatePool.size() >= k) {
            d = candidatePool.get(k).getSortKey();
        } else {
            d = candidatePool.getLast().getSortKey();
        }

        // initialize m upperbounds with "infinity" (> 1), one upperbound for each index i element of [1,m]
        Hashtable<Integer, Float> upperBounds = new Hashtable<Integer, Float>();
        for (int key_index : invertedIndex2.keySet()) upperBounds.put(key_index, (float) 1.01);
        maxThreshold = upperBounds.get(1);
        int j = 0;

        // perform TPL/TA
        while (maxThreshold > d) {

            // Iterate over TPL indices 1 to m
            // TODO: List position j is incremented after all indices i_1, ..., i_m have been scanned
            //       Shouldn't we traverse one index after another?
            //       I.e. first scan index i_1 completely, then index i_2 completely and so on
            //       Because index i_2 for example always contains older entries compared to index i_1
            for (int i : invertedIndex2.keySet()) {

                if ((upperBounds.get(i)) > d) {

                    for (int termID : termIDsInQuery) {

                        // single term list for TermSimilarity
                        singleTermIDList.clear();
                        singleTermIDList.add(termID);

                        /*
                            FValue computation for TweetID in FreshnessList
                         */
                        tweetID = invertedIndex2.get(i).get(termID).getFreshnessPostingList().get(j).getTweetID();

                        // TODO: refactor fValue computations
                        // fValue computation
                        termDate = tripletHashMap.get(i).get(tweetID).getTimestamp();
                        termTermSimilarity = tripletHashMap.get(i).get(tweetID).getTermSimilarity();
                        queryFreshness = HelperFunctions.calculateFreshness(termDate, queryDate);
                        querySignificance = tripletHashMap.get(i).get(tweetID).getSignificance();
                        queryTermSimilarity = HelperFunctions.calculateTermSimilarity(singleTermIDList, termIDsInQuery);
                        // TODO: Why do we do this? is this the correct way of determining the term similarity between
                        //       query terms and term in index?
                        queryTermSimilarity = queryTermSimilarity * termTermSimilarity;

                        fValue = HelperFunctions.calculateRankingFunction(queryFreshness, querySignificance, queryTermSimilarity);
                        threshFreshness = queryFreshness;
                        if (!candidatePool.containsTweetID(tweetID)) {
                            candidatePool.insertSorted(tweetID, fValue);
                        }

                        /*
                            FValue computation for TweetID in SignificanceList
                         */
                        tweetID = invertedIndex2.get(i).get(termID).getSignificancePostingList().get(j).getTweetID();

                        // fValue computation
                        // TODO: would it make sense to store tripletHashMap.get(i).get(tweetID) in variable?
                        termDate = tripletHashMap.get(i).get(tweetID).getTimestamp();
                        termTermSimilarity = tripletHashMap.get(i).get(tweetID).getTermSimilarity();
                        queryFreshness = HelperFunctions.calculateFreshness(termDate, queryDate);
                        querySignificance = tripletHashMap.get(i).get(tweetID).getSignificance();
                        // TODO: code duplication with above
                        queryTermSimilarity = HelperFunctions.calculateTermSimilarity(singleTermIDList, termIDsInQuery);
                        queryTermSimilarity = queryTermSimilarity * termTermSimilarity;

                        fValue = HelperFunctions.calculateRankingFunction(queryFreshness, querySignificance, queryTermSimilarity);
                        threshSignificance = querySignificance;
                        if (!candidatePool.containsTweetID(tweetID)) {
                            candidatePool.insertSorted(tweetID, fValue);
                        }

                        /*
                            FValue computation for TweetID in TermSimilarityList
                         */
                        tweetID = invertedIndex2.get(i).get(termID).getTermSimilarityPostingList().get(j).getTweetID();

                        // fValue computation
                        termDate = tripletHashMap.get(i).get(tweetID).getTimestamp();
                        termTermSimilarity = tripletHashMap.get(i).get(tweetID).getTermSimilarity();
                        queryFreshness = HelperFunctions.calculateFreshness(termDate, queryDate);
                        querySignificance = tripletHashMap.get(i).get(tweetID).getSignificance();
                        queryTermSimilarity = HelperFunctions.calculateTermSimilarity(singleTermIDList, termIDsInQuery);
                        queryTermSimilarity = queryTermSimilarity * termTermSimilarity;

                        fValue = HelperFunctions.calculateRankingFunction(queryFreshness, querySignificance, queryTermSimilarity);
                        threshSimilarity = queryTermSimilarity;
                        if (!candidatePool.containsTweetID(tweetID)) {
                            candidatePool.insertSorted(tweetID, fValue);
                        }

                        // get smallest element in top-k elements
                        // TODO: refactor with code above
                        if (candidatePool.size() >= k) {
                            d = candidatePool.get(k).getSortKey();
                        } else {
                            d = candidatePool.getLast().getSortKey();
                        }

                        // set new Threshold for index i
                        // TODO I dont know where to put this exactly in the algorithm
                        newThreshold = HelperFunctions.calculateRankingFunction(threshFreshness, threshSignificance, threshSimilarity);
                        upperBounds.remove(i);
                        upperBounds.put(i, newThreshold);


                    }
                }
            }

            // get the maximum upperbound of the overall
            for (int bound : upperBounds.keySet()) {
                if (upperBounds.get(bound) > maxThreshold)
                    maxThreshold = upperBounds.get(bound);
            }

            // increase which list-element position to access next
            j++;

        }

        // shorten to top-k elements and copy to arrayList, this may be done better as it currently is O(list.size())
        if (candidatePool.size() >= k)
            candidatePool.subList(0, (k - 1)).clear();
        for (int l = 0; l < candidatePool.size(); l++) {

            topKTweetIDs.add(candidatePool.get(l).getTweetID());
        }

        return topKTweetIDs;
    }

    public void insertTransportObject(TransportObject transportObjectInsertion) {
        // TODO: implement the rest, make performance better
        int currentIndex = 0;
        int tweetID = transportObjectInsertion.getTweetID();
        List<Integer> termIDs = transportObjectInsertion.getTermIDs();

        // Obtain significance and freshness from the transportObject
        float significance = transportObjectInsertion.getSignificance();
        Date freshness = transportObjectInsertion.getTimestamp();

        for (int termID : termIDs) {
            UnsortedPostingList postingListForTermID = this.index_zero.get(termID);

            // Create PostingList for this termID if necessary
            if (postingListForTermID == null) {
                postingListForTermID = new UnsortedPostingList();
                this.index_zero.put(termID, postingListForTermID);
            }

            // insert when new TweetID fits into I_i, else check if I_i+1 exists and insert there by merging
            if (postingListForTermID.size() < sizeThreshold) {
                postingListForTermID.addFirst(tweetID);
                latestTimestamp = transportObjectInsertion.getTimestamp();

                // add information for merging/sorting
                List<Integer> singleTermIDList = new ArrayList<Integer>(1);
                singleTermIDList.add(termID);
                LSIITriplet triplet = new LSIITriplet(freshness, significance, transportObjectInsertion.calculateTermSimilarity(singleTermIDList), termIDs);

                // create Triplet Hash Table for I_i if necessary
                if (tripletHashMap.get(currentIndex) == null) {
                    ConcurrentHashMap newTripletTable = new ConcurrentHashMap<Integer, LSIITriplet>();
                    tripletHashMap.put(currentIndex, newTripletTable);
                }

                tripletHashMap.get(currentIndex).put(tweetID, triplet);

            } else {
                // traverse I_i, i = currentIndex
                currentIndex++;

                // check if I_i exists and create it if necessary
                if (this.invertedIndex.get(currentIndex) == null) {

                    // this is the new I_i, TPL-style and respective HashTable for triplet lookup (needed for performance in queries)
                    ConcurrentHashMap indexMap = new ConcurrentHashMap<Integer, ConcurrentTriplePostingList>();
                    this.invertedIndex.put(currentIndex, indexMap);
                }

                ConcurrentTriplePostingList triplePostingListForTermID = this.invertedIndex.get(currentIndex).get(termID);

                // Create PostingList for this termID if necessary
                if (triplePostingListForTermID == null) {
                    triplePostingListForTermID = new ConcurrentTriplePostingList();
                    this.invertedIndex.get(currentIndex).put(termID, triplePostingListForTermID);
                }

                // check if there is space in posting list for I_i
                // TODO merging needs to be done in parallel, see LSII paper pages 8,9
                if (this.invertedIndex.get(currentIndex).get(termID).getFreshnessPostingList().size() < ((currentIndex + 1) * sizeThreshold)) {
                    // merge I_0 and I_1 -> first sort entries and then merge
                    // TODO sort, then merge here (I_0 and I_1 merging)
                } else {
                    // TODO perform merges with I_0, I_1, I_2, I_3, etc until we find sufficient posting list space
                }

                // insert now into I_0 which has space
                postingListForTermID.addFirst(tweetID);
                latestTimestamp = transportObjectInsertion.getTimestamp();

                // add information for merging/sorting
                List<Integer> singleTermIDList = new ArrayList<Integer>(1);
                singleTermIDList.add(termID);
                LSIITriplet triplet = new LSIITriplet(freshness, significance, transportObjectInsertion.calculateTermSimilarity(singleTermIDList), termIDs);

                // create Triplet Hash Table for I_i if necessary
                if (tripletHashMap.get(currentIndex) == null) {
                    ConcurrentHashMap newTripletTable = new ConcurrentHashMap<Integer, LSIITriplet>();
                    tripletHashMap.put(currentIndex, newTripletTable);
                }

                tripletHashMap.get(currentIndex).put(tweetID, triplet);

            }
        }
    }

    public int size() {
        // TODO: implement
        return 0;
    }

    public int getSizeThreshold() {
        return this.sizeThreshold;
    }

    public void setSizeThreshold(int sizeThreshold) {
        this.sizeThreshold = sizeThreshold;
    }

}