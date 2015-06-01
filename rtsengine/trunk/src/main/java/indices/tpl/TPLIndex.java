package indices.tpl;

import indices.IRTSIndex;
import indices.lsii.LSIITriplet;
import indices.postingarraylists.ConcurrentTPLArrayList;
import indices.postinglists.*;
import model.TransportObject;
import utilities.HelperFunctions;

import java.util.*;


import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chans on 5/14/15.
 */
public class TPLIndex implements IRTSIndex {

    private ConcurrentHashMap<Integer, ConcurrentTriplePostingList> invertedIndex;

    // Maps TweetIDs to LSIITriplets containing all the information for score computation
    private ConcurrentHashMap<Integer, LSIITriplet> tripletHashMap;

    /*
        Testing purpose only
     */
    private ConcurrentHashMap<Integer, ConcurrentTPLArrayList> invertedIndex2;

    public TPLIndex() {
        this.invertedIndex = new ConcurrentHashMap<Integer, ConcurrentTriplePostingList>();
        this.tripletHashMap = new ConcurrentHashMap<Integer, LSIITriplet>();

        /*
            Testing purpose only
         */
        this.invertedIndex2 = new ConcurrentHashMap<Integer, ConcurrentTPLArrayList>();
    }

    public List<Integer> searchTweetIDs(TransportObject transportObjectQuery) {
        SortedPostingList resultList = new SortedPostingList();

        float upperBoundScore;

        // Threshold algorithm main loop
        int i = 0;
        while (true) {
            // Abort search when i exceeds posting lists' size
            // TODO: is this the correct stop condition?
            try {
                upperBoundScore = TPLHelper.examineTPLIndexAtPosition(this.invertedIndex2, this.tripletHashMap, i, transportObjectQuery, resultList);
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

            i++;
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
        float significance = transportObjectInsertion.getSignificance();
        Date timestamp = transportObjectInsertion.getTimestamp();
        List<Integer> termIDs = transportObjectInsertion.getTermIDs();

        for (int termID : termIDs) {
            ConcurrentTPLArrayList triplePostingListForTermID = this.invertedIndex2.get(termID);

            // Create PostingList for this termID if necessary
            if (triplePostingListForTermID == null) {
                triplePostingListForTermID = new ConcurrentTPLArrayList();
                this.invertedIndex2.put(termID, triplePostingListForTermID);
            }

            // insert into tripletHashmap
            tripletHashMap.put(tweetID, new LSIITriplet(timestamp, significance, termIDs));

            // Calculate term similarity between term used as key and terms in inserted TransportObject
            float termSimilarity = transportObjectInsertion.calculateTermSimilarity(termIDs);

            // insert tweetID sorted on float values into index posting lists
            HelperFunctions.insertSorted(triplePostingListForTermID.getFreshnessPostingList(), new ConcurrentSortedDateListElement(tweetID, timestamp));
            HelperFunctions.insertSorted(triplePostingListForTermID.getSignificancePostingList(), new ConcurrentSortedPostingListElement(tweetID, significance));
            HelperFunctions.insertSorted(triplePostingListForTermID.getTermSimilarityPostingList(), new ConcurrentSortedPostingListElement(tweetID, termSimilarity));
            //triplePostingListForTermID.getTermSimilarityPostingList().insertSorted(tweetID, termSimilarity);
        }
    }

    public int size() {
        return this.invertedIndex.size();
    }


    /*

        Deprecated code below

     */

    /* Approach using IteratorQueue - complicated... we should rather use normal get(i) approach
       in combination with B-Tree for the PostingLists
    public List<Integer> searchTweetIDs(TransportObject transportObjectQuery) {
        int k = transportObjectQuery.getk();
        Date timestampQuery = transportObjectQuery.getTimestamp();
        List<Integer> termIDsInQuery = transportObjectQuery.getTermIDs();

        // This list has to be explicitly limited to result size k
        SortedPostingList resultList = new SortedPostingList();

        // Maintain three queues of iterators.
        // One for freshness posting list iterators,
        // one for significance posting list iterators and
        // one for term similarity posting list iterators.
        LinkedBlockingQueue<Iterator<ConcurrentSortedDateListElement>> freshnessPostingListIteratorQueue = new LinkedBlockingQueue<Iterator<ConcurrentSortedDateListElement>>();
        LinkedBlockingQueue<Iterator<ConcurrentSortedPostingListElement>> significancePostingListIteratorQueue = new LinkedBlockingQueue<Iterator<ConcurrentSortedPostingListElement>>();
        LinkedBlockingQueue<Iterator<ConcurrentSortedPostingListElement>> termSimilarityPostingListIteratorQueue = new LinkedBlockingQueue<Iterator<ConcurrentSortedPostingListElement>>();

        // Add one iterator for each of the three posting lists
        // corresponding to a termID in the query
        for (int termIDInQuery : termIDsInQuery) {
            ConcurrentTriplePostingList tplPostingListForTermIDInQuery = this.invertedIndex.get(termIDInQuery);

            // Posting list for this termID exists?
            if (tplPostingListForTermIDInQuery == null) {
                continue;
            }

            // Fetch three list iterator for this posting list

            // Freshness posting list
            Iterator<ConcurrentSortedDateListElement> freshnessPostingListIteratorForTermIDInQuery = tplPostingListForTermIDInQuery.getFreshnessPostingList().descendingIterator();
            // Add iterator to queue if it contains at least one element
            if (freshnessPostingListIteratorForTermIDInQuery.hasNext()) {
                freshnessPostingListIteratorQueue.add(freshnessPostingListIteratorForTermIDInQuery);
            }

            // Significance posting list
            Iterator<ConcurrentSortedPostingListElement> significancePostingListIteratorForTermIDInQuery = tplPostingListForTermIDInQuery.getSignificancePostingList().descendingIterator();
            // Add iterator to queue if it contains at least one element
            if (significancePostingListIteratorForTermIDInQuery.hasNext()) {
                significancePostingListIteratorQueue.add(significancePostingListIteratorForTermIDInQuery);
            }

            // Term similarity posting list
            Iterator<ConcurrentSortedPostingListElement> termSimilarityPostingListIteratorForTermIDInQuery = tplPostingListForTermIDInQuery.getTermSimilarityPostingList().descendingIterator();
            // Add iterator to queue if it contains at least one element
            if (termSimilarityPostingListIteratorForTermIDInQuery.hasNext()) {
                termSimilarityPostingListIteratorQueue.add(termSimilarityPostingListIteratorForTermIDInQuery);
            }
        }

        // Init variables used in Threshold algorithm's main loop
        // Current position in posting lists, i.e. i
        int currentPositionInPostingLists = 0;
        // Initialize three sets for found Tweets at position i
        // Used to gather Tweets for insertion into resultList
        // and for calculation of score upper bound
        HashSet<ConcurrentSortedDateListElement> setFreshness = new HashSet<ConcurrentSortedDateListElement>();
        HashSet<ConcurrentSortedPostingListElement> setSignificance = new HashSet<ConcurrentSortedPostingListElement>();
        HashSet<ConcurrentSortedPostingListElement> setTermSimilarity = new HashSet<ConcurrentSortedPostingListElement>();

        // Threshold algorithm's main loop
        while (true) {
            // Fetch iterators from PostingLists
            Iterator<ConcurrentSortedDateListElement> freshnessPostingListIterator = freshnessPostingListIteratorQueue.remove();
            Iterator<ConcurrentSortedPostingListElement> significancePostingListIterator = significancePostingListIteratorQueue.remove();
            Iterator<ConcurrentSortedPostingListElement> termSimilarityPostingListIterator = termSimilarityPostingListIteratorQueue.remove();

            // ListIterators in the queue always contain at least one element
            ConcurrentSortedDateListElement freshnessPostingListElement = freshnessPostingListIterator.next();
            ConcurrentSortedPostingListElement significancePostingListElement = significancePostingListIterator.next();
            ConcurrentSortedPostingListElement termSimilarityPostingListElement = termSimilarityPostingListIterator.next();

            // Approach: Gather all i-th elements
            // TODO: How to check whether we have all i-th elements?

            // Idea: Implement own PostingListIterator that knows list position i and also the Index
            //       to which the PostingList belongs

            // Did we move one position ahead in any Posting List (i.e. from i to i+1)?
            // If that's the case, insert sets into resultList, calculate upper bound score and
            // empty the three sets
            if (TODO) {
                this.insertPostingListElementSetsIntoResultList(resultList, setFreshness, setSignificance, setTermSimilarity);

                // TODO: Do something with this value
                //this.calculateUpperBoundScoreF(setFreshness, setSignificance, setTermSimilarity);

                setFreshness.clear();
                setSignificance.clear();
                setTermSimilarity.clear();
            }

            // TODO: Could it be that the PostingLists have different sizes?
            // TODO: Probably

            // Insert Elements (i.e. Tweets with their scores into the three sets)
            setFreshness.add(freshnessPostingListElement);
            setSignificance.add(significancePostingListElement);
            setTermSimilarity.add(termSimilarityPostingListElement);

            // Put the list iterator back to the queue in case it has more elements
            if (freshnessPostingListIterator.hasNext()) {
                freshnessPostingListIteratorQueue.add(freshnessPostingListIterator);
            }

            if (significancePostingListIterator.hasNext()) {
                significancePostingListIteratorQueue.add(significancePostingListIterator);
            }

            if (termSimilarityPostingListIterator.hasNext()) {
                termSimilarityPostingListIteratorQueue.add(termSimilarityPostingListIterator);
            }

            // Check stop condition
        }


        return resultList;
    }
    */

    /**
     *  Works in combination with the insertObjectNew-method. Query-search using the new list structure
     *
     * @param transportObjectQuery
     * @return
     */
    /*
    public List<Integer> searchTweetIDs(TransportObject transportObjectQuery) {

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

                 // FValue computation for TweetID in FreshnessList
                currentTweetID = triplePostingListForTermID.getFreshnessPostingList().get(i).getTweetID();

                // fValue computation
                termDate = tripletHashMap.get(currentTweetID).getTimestamp();
                termTermSimilarity = tripletHashMap.get(currentTweetID).getTermSimilarity();
                queryFreshness = HelperFunctions.calculateFreshness(termDate, queryDate);
                querySignificance = tripletHashMap.get(currentTweetID).getSignificance();
                queryTermSimilarity = HelperFunctions.calculateTermSimilarity(singleTermIDList, termIDsInQuery);
                queryTermSimilarity = queryTermSimilarity * termTermSimilarity;

                fValue = HelperFunctions.calculateRankingFunction(queryFreshness, querySignificance, queryTermSimilarity);
                maxFreshness = queryFreshness;

                if (!candidatePool.containsTweetID(currentTweetID)) {
                    candidatePool.insertSorted(currentTweetID, fValue);
                    //candidatePool.removeFirstDuplicate(currentTweetID, fValue);
                }


                // FValue computation for TweetID in SignificanceList
                currentTweetID = triplePostingListForTermID.getSignificancePostingList().get(i).getTweetID();

                // fValue computation
                termDate = tripletHashMap.get(currentTweetID).getTimestamp();
                termTermSimilarity = tripletHashMap.get(currentTweetID).getTermSimilarity();
                queryFreshness = HelperFunctions.calculateFreshness(termDate, queryDate);
                querySignificance = tripletHashMap.get(currentTweetID).getSignificance();
                queryTermSimilarity = HelperFunctions.calculateTermSimilarity(singleTermIDList, termIDsInQuery);
                queryTermSimilarity = queryTermSimilarity * termTermSimilarity;

                fValue = HelperFunctions.calculateRankingFunction(queryFreshness, querySignificance, queryTermSimilarity);
                maxSignificance = querySignificance;

                if (!candidatePool.containsTweetID(currentTweetID)) {
                    candidatePool.insertSorted(currentTweetID, fValue);
                    //candidatePool.removeFirstDuplicate(currentTweetID, fValue);
                }

                // FValue computation for TweetID in TermSimilarityList
                currentTweetID = triplePostingListForTermID.getTermSimilarityPostingList().get(i).getTweetID();

                // fValue computation
                termDate = tripletHashMap.get(currentTweetID).getTimestamp();
                termTermSimilarity = tripletHashMap.get(currentTweetID).getTermSimilarity();
                queryFreshness = HelperFunctions.calculateFreshness(termDate, queryDate);
                querySignificance = tripletHashMap.get(currentTweetID).getSignificance();
                queryTermSimilarity = HelperFunctions.calculateTermSimilarity(singleTermIDList, termIDsInQuery);
                queryTermSimilarity = queryTermSimilarity * termTermSimilarity;

                fValue = HelperFunctions.calculateRankingFunction(queryFreshness, querySignificance, queryTermSimilarity);
                maxSimilarity = queryTermSimilarity;

                if (!candidatePool.containsTweetID(currentTweetID)) {
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
    */

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

    /*
    public void insertTransportObject(TransportObject transportObjectInsertion) {

        // extract the important information from the transport object
        int tweetID = transportObjectInsertion.getTweetID();

        // Obtain significance and freshness from the transportObject
        float significance = transportObjectInsertion.getSignificance();
        Date timestamp = transportObjectInsertion.getTimestamp();

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
            triplePostingListForTermID.getFreshnessPostingList().add(new ConcurrentSortedDateListElement(tweetID, timestamp));
            triplePostingListForTermID.getTermSimilarityPostingList().add(new ConcurrentSortedPostingListElement(tweetID, similarity));
        }
    }
    */
}
