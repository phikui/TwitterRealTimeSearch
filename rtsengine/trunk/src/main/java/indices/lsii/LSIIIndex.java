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
    private ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ITriplePostingList>> invertedIndex3;
    private ConcurrentHashMap<Integer, IPostingList> index_zero2;


    public LSIIIndex() {
        this.index_zero = new ConcurrentHashMap<Integer, UnsortedPostingList>();
        this.invertedIndex = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ConcurrentTriplePostingList>>();
        this.tripletHashMap = new ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, LSIITriplet>>();

        this.invertedIndex3 = new ConcurrentHashMap<>();
        this.index_zero2 = new ConcurrentHashMap<>();

    }

    // This method will take the  ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ITriplePostingList>> invertedIndex3;
    // and return array list of ITriplePostingList items that were inside invertedIndex3.
    private LinkedList<ITriplePostingList> extractITripple(ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ITriplePostingList>> invertedIndex3) {
        LinkedList<ITriplePostingList> iTriplePostingLists = new LinkedList<>();
        for (Map.Entry<Integer, ConcurrentHashMap<Integer, ITriplePostingList>> entry : invertedIndex3.entrySet()) {

            ConcurrentHashMap<Integer, ITriplePostingList> insideEntry = entry.getValue();
            for (Map.Entry<Integer, ITriplePostingList> integerITriplePostingListEntry : insideEntry.entrySet()) {
                iTriplePostingLists.add(integerITriplePostingListEntry.getValue());
            }
        }
        return iTriplePostingLists;
    }

    // This method will take the  ConcurrentHashMap<Integer, ITriplePostingList> index_zero2;
    // and return array list of IPostingList items that were inside index_zero2.
    private synchronized LinkedList<IPostingList> extractIPostingList(ConcurrentHashMap<Integer, IPostingList> iPostingListConcurrentHashMap) {

        LinkedList<IPostingList> iPostingLists = new LinkedList<>();
        for (Map.Entry<Integer, IPostingList> integerITriplePostingListEntry : iPostingListConcurrentHashMap.entrySet()) {
            iPostingLists.add(integerITriplePostingListEntry.getValue());
        }

        return iPostingLists;
    }

    // this method will add all the three triple posting list into one posting list
    public synchronized LinkedList<IPostingList> mergeSinglePostingLists(ITriplePostingList iTriplePostingList) {
        LinkedList<IPostingList> temp = new LinkedList<>();
        temp.add(iTriplePostingList.getFreshnessPostingList());
        temp.add(iTriplePostingList.getSignificancePostingList());
        temp.add(iTriplePostingList.getTermSimilarityPostingList());
        return temp;
    }

    //in this method first we will have the ipostinglist value and then the tripleposting list value
    public synchronized LinkedList<IPostingList> mergePostingLists(LinkedList<ITriplePostingList> iTriplePostingList, LinkedList<IPostingList> iPostingList) {
        LinkedList<IPostingList> temp = new LinkedList<>();
        temp.addAll(iPostingList);

        for (ITriplePostingList iTriplePostingList1 : iTriplePostingList) {
            temp.addAll(mergeSinglePostingLists(iTriplePostingList1));
        }

        return temp;
    }

    public List<Integer> searchTweetIDs(TransportObject transportObjectQuery) {

        // needed for concurrency in AO IndexTypes
        Date tsMax = latestTimestamp;

        // values for stop condition in TPL/TA
        float d;
        float maxThreshold;
        float newUpperBound;

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

        /*
        // condition if all lists in all indices are traversed
        HashMap<Integer, Boolean> listEmptyMap = new HashMap<>();
        for (int key_index : invertedIndex3.keySet()){
            listEmptyMap.put(key_index, false);
        }*/

        // TPL/TA iteration based on LSII-paper
        while (maxThreshold > d && !listEmpty) {

            // for each index i get the next element and calculate fValues and thresholds
            for (int i : invertedIndex3.keySet()) {

                if ((upperBoundMap.get(i)) > d) {

                    try {
                        newUpperBound = TPLHelper.examineTPLIndex(this.invertedIndex3.get(i), postingListIteratorMapTPL.get(i), transportObjectQuery, resultList);
                        upperBoundMap.put(i, newUpperBound);
                    } catch (IndexOutOfBoundsException e) {
                        listEmpty = true;
                        //listEmptyMap.put(i, true);
                        break;
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

            /*
            // check if all lists in all indices are empty
            for (int index : listEmptyMap.keySet()){
                if (listEmptyMap.get(index)){
                    listEmpty = true;
                }else{
                    listEmpty = false;
                    break;
                }
            }*/

        }

        return resultList.getTweetIDs();
    }

    /*
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

        IPostingList candidatePool = new PostingList();
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
                // TODO: fixme
                termTermSimilarity = 0;//tripletHashMap.get(i).get(tweetID).getTermSimilarity();

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

                                                   // FValue computation for TweetID in FreshnessList

                        tweetID = invertedIndex2.get(i).get(termID).getFreshnessPostingList().get(j).getTweetID();

                        // TODO: refactor fValue computations
                        // fValue computation
                        termDate = tripletHashMap.get(i).get(tweetID).getTimestamp();
                        // TODO: fixme
                        termTermSimilarity = 0;//tripletHashMap.get(i).get(tweetID).getTermSimilarity();
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

                                               //     FValue computation for TweetID in SignificanceList

                        tweetID = invertedIndex2.get(i).get(termID).getSignificancePostingList().get(j).getTweetID();

                        // fValue computation
                        // TODO: would it make sense to store tripletHashMap.get(i).get(tweetID) in variable?
                        termDate = tripletHashMap.get(i).get(tweetID).getTimestamp();
                        // TODO: fixme
                        termTermSimilarity = 0;//tripletHashMap.get(i).get(tweetID).getTermSimilarity();
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


                          //  FValue computation for TweetID in TermSimilarityList

                        tweetID = invertedIndex2.get(i).get(termID).getTermSimilarityPostingList().get(j).getTweetID();

                        // fValue computation
                        termDate = tripletHashMap.get(i).get(tweetID).getTimestamp();
                        // TODO: fixme
                        termTermSimilarity = 0;//tripletHashMap.get(i).get(tweetID).getTermSimilarity();
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
    }*/


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

                    LSIIHelper.mergeWithNextIndex(currentIndex, termID, sizeThreshold, transportObjectInsertion, invertedIndex3, index_zero2);
                }

                // insert now into I_0 which has space
                postingListForTermID.addFirst(new PostingListElement(tweetID, freshness));
                latestTimestamp = transportObjectInsertion.getTimestamp();
            }

        }

    }


    public void insertTransportObject2(TransportObject transportObjectInsertion) {
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
                LSIITriplet triplet = new LSIITriplet(freshness, significance, termIDs);

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
                //TODO about the (Timestamp concept, next mergers overlap )in the paper
                // (I_0 and I_1 merging here )
                if (this.invertedIndex.get(currentIndex).get(termID).getFreshnessPostingList().size() < ((currentIndex + 1) * sizeThreshold)) {
                    // merge I_0 and I_1 -> first sort entries and then merge
                    //creating a merge thread and all the functionality on creation of a thread
                    LinkedList<IPostingList> I0 = extractIPostingList(index_zero2);
                    LinkedList<IPostingList> I0_;
                    LinkedList<ITriplePostingList> I1 = extractITripple(invertedIndex3);

                    Thread mergerThread = new Thread(new Runnable() {
                        public void run() {
                            //first threads create a  new list of inverted index I1
                            LinkedList<IPostingList> I1_;// to ask
                            // shared lock on I0 and I1
                            // shared lock on I0 and I1
                            synchronized (I0) {
                            }
                            ;
                            synchronized (I1) {
                            }
                            ;
                            //merge the content of I0 and I1 into I1'
                            I1_ = mergePostingLists(I1, I0);
                            // struck here as the paper suggest to use I1 which is triple posting list
                            //but getting the three list from the triple posting list and merging with positing list
                            // then we cannot move back to the triple positing list???


                        }
                    });
                    // TODO sort, then merge here (I_0 and I_1 merging)
                } else {
                    // TODO perform merges with I_0, I_1, I_2, I_3, etc until we find sufficient posting list space
                    //  for (LinkedList<type>:object){}
                }

                // insert now into I_0 which has space
                postingListForTermID.addFirst(tweetID);
                latestTimestamp = transportObjectInsertion.getTimestamp();

                // add information for merging/sorting

                List<Integer> singleTermIDList = new ArrayList<Integer>(1);
                singleTermIDList.add(termID);
                LSIITriplet triplet = new LSIITriplet(freshness, significance, termIDs);

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