package indices.lsii;

import indices.IRTSIndex;
import indices.deprecated.ConcurrentTPLArrayList;
import indices.deprecated.ConcurrentTriplePostingList;
import indices.deprecated.UnsortedPostingList;
import indices.postinglists.*;
import indices.tpl.TPLHelper;
import model.TransportObject;

import java.util.*;
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
    private volatile ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ITriplePostingList>> invertedIndex3;
    private volatile ConcurrentHashMap<Integer, IPostingList> index_zero2;


    public LSIIIndex() {
        this.index_zero = new ConcurrentHashMap<Integer, UnsortedPostingList>();
        this.invertedIndex = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ConcurrentTriplePostingList>>();
        this.tripletHashMap = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, LSIITriplet>>();

        this.invertedIndex3 = new ConcurrentHashMap<>();
        this.index_zero2 = new ConcurrentHashMap<>();

    }


    public List<Integer> searchTweetIDs(TransportObject transportObjectQuery) {

        // needed for concurrency in AO IndexTypes
        Date tsMax = latestTimestamp;

        // values for stop condition in TPL/TA
        float d;
        float maxThreshold;
        float newUpperBound;

        //ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ITriplePostingList>> invertedIndex3 = this.invertedIndex3;

        IPostingList resultList = new PostingList();


        // Stores Iterator for each PostingList that has already been examined
        HashMap<Integer, Iterator<IPostingListElement>> postingListIteratorMapAO = new HashMap<Integer, Iterator<IPostingListElement>>();

        // AO iteration based on LSII-paper. First find k microblogs in I_0 with the largest score
        while (true) {
            try {
                LSIIHelper.examineAOIndexAtPosition(this.index_zero2, postingListIteratorMapAO, transportObjectQuery, resultList, tsMax);

            } catch (IndexOutOfBoundsException e) {
                break;
            }
        }

        // set d as lowest value in our current candidate pool of k elements
        d = resultList.getLast().getSortKey();

        // initialize m upperbounds with "infinity" (> 1), one upperbound for each index i element of [1,m]
        HashMap<Integer, Float> upperBoundMap = new HashMap<>();
        for (int key_index : invertedIndex3.keySet()) {
            upperBoundMap.put(key_index, (float) 1.01);
        }
        // maximum threshold of all m thresholds, initialize as I_1 upperbound (value = 1.01)
        maxThreshold = (float)1.01;

        // Hashmap of Hashmaps as  we can have the same termID in different Indices, similar to invertedIndex structure
        HashMap<Integer, HashMap<Integer, Iterator<IPostingListElement>>> postingListIteratorMapTPL = new HashMap<>();
        for (int key_index : invertedIndex3.keySet()) {
            postingListIteratorMapTPL.put(key_index, new HashMap<>());
        }

        boolean listEmpty = false;

        // condition if all lists in all indices are traversed
        HashMap<Integer, Boolean> listEmptyMap = new HashMap<>();
        for (int key_index : invertedIndex3.keySet()){
            listEmptyMap.put(key_index, false);
        }

        // TPL/TA iteration based on LSII-paper
        System.out.println(maxThreshold +" > "+ d);
        while (maxThreshold > d && !listEmpty) {

            // for each index i get the next element and calculate fValues and thresholds
            for (int i : invertedIndex3.keySet()) {

                if(upperBoundMap.get(i) == null){
                    continue;
                }

                if ((upperBoundMap.get(i)) > d) {

                    try {
                        newUpperBound = TPLHelper.examineTPLIndex(invertedIndex3.get(i), postingListIteratorMapTPL.get(i), transportObjectQuery, resultList);
                        upperBoundMap.put(i, newUpperBound);
                        System.out.println("Index: "+ i + " UpperBound: "+ newUpperBound);
                    } catch (IndexOutOfBoundsException e) {
                        listEmptyMap.put(i, true);
                    }

                    d = resultList.getLast().getSortKey();
                }
            }

            // get the maximum threshold of all the thresholds just calculated to see if we continue the next step
            // reset max threshold beforehand as it is initially defined as > 1
            maxThreshold = 0;
            for (int bound : upperBoundMap.keySet()) {
                if (upperBoundMap.get(bound) > maxThreshold)
                    maxThreshold = upperBoundMap.get(bound);
            }
            if (d > maxThreshold){
                System.out.println("Break because of threshold: " + maxThreshold +" > " + d);
                break;
            }


            // check if all lists in all indices are empty
            for (int index : listEmptyMap.keySet()){
                if (listEmptyMap.get(index)){
                    listEmpty = true;
                }else{
                    listEmpty = false;
                    break;
                }
            }
            if(listEmpty){
                System.out.println("Break because of empty list");
                break;
            }

        }

        return resultList.getTweetIDs();
    }


    public void insertTransportObject(TransportObject transportObjectInsertion) {
        int currentIndex = 0;
        int tweetID = transportObjectInsertion.getTweetID();
        List<Integer> termIDs = transportObjectInsertion.getTermIDs();

        // Obtain significance and freshness from the transportObject
        float freshness = (float) transportObjectInsertion.getTimestamp().getTime();

        for (int termID : termIDs) {
            IPostingList postingListForTermID = this.index_zero2.get(termID);

            // Create PostingList for this termID in I_0 if necessary
            if (postingListForTermID == null) {
                postingListForTermID = new PostingList();
                this.index_zero2.put(termID, postingListForTermID);
            }

            // insert when new TweetID fits into I_0, else check for I_1, ..., I_m and insert there by merging
            if (postingListForTermID.size() < sizeThreshold) {
                postingListForTermID.addFirst(new PostingListElement(tweetID, freshness));

                // set latest timestamp to avoid possible reader/writer conflicts
                latestTimestamp = transportObjectInsertion.getTimestamp();

            } else {
                // traverse I_i, i = currentIndex
                currentIndex++;

                while(postingListForTermID.size() > 0) {

                    LSIIHelper.mergeWithNextIndex(termID, sizeThreshold, invertedIndex3, index_zero2);
                }

                // insert now into I_0 which has space
                postingListForTermID.addFirst(new PostingListElement(tweetID, freshness));
                latestTimestamp = transportObjectInsertion.getTimestamp();
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