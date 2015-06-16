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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by chans on 5/14/15.
 */
public class LSIIIndex implements IRTSIndex {

    // latestTimestamp as a variable to coordinate query and writer threads
    private volatile Date latestTimestamp;

    // I_0 = index_zero, I_1 to I_m = invertedIndex
    private ConcurrentHashMap<Integer, IPostingList> index_zero;
    private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ITriplePostingList>> invertedIndex;

    // starting size threshold
    private int sizeThreshold = 4;

    // read-lock/write-lock stuff for merging
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock read = readWriteLock.readLock();
    private final Lock write = readWriteLock.writeLock();


    public LSIIIndex() {
        this.invertedIndex = new ConcurrentHashMap<>();
        this.index_zero = new ConcurrentHashMap<>();
    }


    public List<Integer> searchTweetIDs(TransportObject transportObjectQuery) {

        // needed for concurrency in AO IndexTypes
        Date tsMax = latestTimestamp;

        // values for stop condition in TPL/TA
        float d;
        float maxThreshold;
        float newUpperBound;

        int index_count = 0;

        IPostingList resultList = new PostingList();

        // Stores Iterator for each PostingList that has already been examined
        HashMap<Integer, Iterator<IPostingListElement>> postingListIteratorMapAO = new HashMap<Integer, Iterator<IPostingListElement>>();

        // AO iteration based on LSII-paper. First find k microblogs in I_0 with the largest score
        read.lock();
        System.out.println("Read-lock given");
        try {
            while (true) {
                try {
                    LSIIHelper.examineAOIndexAtPosition(this.index_zero, postingListIteratorMapAO, transportObjectQuery, resultList, tsMax);

                } catch (IndexOutOfBoundsException e) {
                    break;
                }
            }

            // set d as lowest value in our current candidate pool of k elements
            if(resultList.size() > 0){
                d = resultList.getLast().getSortKey();
            }else {
                d = (float)0.0;
            }

            // initialize m upperbounds with "infinity" (> 1), one upperbound for each index i element of [1,m]
            HashMap<Integer, Float> upperBoundMap = new HashMap<>();
            for (int key_index : invertedIndex.keySet()) {
                //System.out.println("Create upperbound for index: " + key_index);
                upperBoundMap.put(key_index, (float) 1.01);
            }
            // maximum threshold of all m thresholds, initialize as I_1 upperbound (value = 1.01)
            maxThreshold = (float) 1.01;

            // Hashmap of Hashmaps as  we can have the same termID in different Indices, similar to invertedIndex structure
            HashMap<Integer, HashMap<Integer, Iterator<IPostingListElement>>> postingListIteratorMapTPL = new HashMap<>();
            for (int key_index : invertedIndex.keySet()) {
                postingListIteratorMapTPL.put(key_index, new HashMap<>());
            }

            boolean listEmpty = false;

            // condition if all lists in all indices are traversed
            HashMap<Integer, Boolean> listEmptyMap = new HashMap<>();
            for (int key_index : invertedIndex.keySet()) {
                listEmptyMap.put(key_index, false);
            }

            // TPL/TA iteration based on LSII-paper
            while (maxThreshold > d && !listEmpty) {
                index_count++;

                // for each index i get the next element and calculate fValues and thresholds
                for (int i : invertedIndex.keySet()) {


                    if (upperBoundMap.get(i) == null) {
                        upperBoundMap.put(i, (float) 0.0);
                        continue;
                    }

                    if ((upperBoundMap.get(i)) > d) {

                        try {
                            newUpperBound = TPLHelper.examineTPLIndex(invertedIndex.get(i), postingListIteratorMapTPL.get(i), transportObjectQuery, resultList);
                            upperBoundMap.put(i, newUpperBound);
                            //System.out.println("Index: " + i + " UpperBound: " + newUpperBound);
                        } catch (IndexOutOfBoundsException e) {
                            listEmptyMap.put(i, true);
                            if (upperBoundMap.get(i) > 1) {
                                //System.out.println("Remove index upperbound: " + i);
                                upperBoundMap.remove(i);
                            }
                        }

                        if (resultList.size() > 0){
                            d = resultList.getLast().getSortKey();
                        }

                    }
                }

                // get the maximum threshold of all the thresholds just calculated to see if we continue the next step
                // reset max threshold beforehand as it is initially defined as > 1
                maxThreshold = 0;
                for (int bound : upperBoundMap.keySet()) {
                    if (upperBoundMap.get(bound) > maxThreshold) {
                        //System.out.println("Index: " + bound + " " + upperBoundMap.get(bound));
                        maxThreshold = upperBoundMap.get(bound);
                    }
                }

                if (d > maxThreshold) {
                    System.out.println("Break because of threshold: " + maxThreshold + " > " + d);
                    break;
                }


                // check if all lists in all indices are empty
                for (int index : listEmptyMap.keySet()) {
                    if (listEmptyMap.get(index)) {
                        listEmpty = true;
                    } else {
                        listEmpty = false;
                        break;
                    }
                }
                if (listEmpty) {
                    System.out.println("Break because of empty list");
                    break;
                }

                if (index_count > 10000) {
                    break;
                }

            }
        } finally {
            read.unlock();
            System.out.println("Read-lock removed");
            System.out.println("--------------------------------");
        }
        return resultList.getTweetIDs();
    }


    public void insertTransportObject(TransportObject transportObjectInsertion) {
        int tweetID = transportObjectInsertion.getTweetID();
        List<Integer> termIDs = transportObjectInsertion.getTermIDs();

        // Obtain significance and freshness from the transportObject
        float freshness = (float) transportObjectInsertion.getTimestamp().getTime();

        write.lock();
        try {
            for (int termID : termIDs) {
                IPostingList postingListForTermID = this.index_zero.get(termID);

                // Create PostingList for this termID in I_0 if necessary
                if (postingListForTermID == null) {
                    postingListForTermID = new PostingList();
                    this.index_zero.put(termID, postingListForTermID);
                }

                // insert when new TweetID fits into I_0, else check for I_1, ..., I_m and insert there by merging
                if (postingListForTermID.size() < sizeThreshold) {
                    postingListForTermID.addFirst(new PostingListElement(tweetID, freshness));

                    // set latest timestamp to avoid possible reader/writer conflicts
                    latestTimestamp = transportObjectInsertion.getTimestamp();

                } else {
                    // traverse I_i, i = currentIndex
                    while (postingListForTermID.size() > 0) {
                        LSIIHelper.mergeWithNextIndex(termID, sizeThreshold, invertedIndex, index_zero);
                    }

                    // insert now into I_0 which has space
                    postingListForTermID.addFirst(new PostingListElement(tweetID, freshness));
                    latestTimestamp = transportObjectInsertion.getTimestamp();
                }

            }
        } finally {
            write.unlock();
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