package indices.lsii;

import indices.IRTSIndex;
import indices.postinglists.*;
import model.TransportObject;

import java.util.ArrayList;
import java.util.Date;
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

    public LSIIIndex() {
        this.index_zero = new ConcurrentHashMap<Integer, UnsortedPostingList>();
        this.invertedIndex = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ConcurrentTriplePostingList>>();
        this.tripletHashMap = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, LSIITriplet>>();
    }

    public List<Integer> searchTweetIDs(TransportObject transportObjectQuery) {
        Date tsMax = latestTimestamp;
        // TODO: implement
        return null;
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
                LSIITriplet triplet = new LSIITriplet(freshness, significance, transportObjectInsertion.calculateTermSimilarity(singleTermIDList));

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
                LSIITriplet triplet = new LSIITriplet(freshness, significance, transportObjectInsertion.calculateTermSimilarity(singleTermIDList));

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

    public int getSizeThreshold(){
        return this.sizeThreshold;
    }

    public void setSizeThreshold(int sizeThreshold){
        this.sizeThreshold = sizeThreshold;
    }

}