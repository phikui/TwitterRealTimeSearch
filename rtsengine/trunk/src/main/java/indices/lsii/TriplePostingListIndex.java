package indices.lsii;

import indices.IRTSIndex;
import indices.postingarraylists.ConcurrentTPLArrayList;
import indices.postinglists.*;
import model.TransportObject;
import utilities.HelperFunctions;

import java.util.*;


import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chans on 5/14/15.
 */
public class TriplePostingListIndex implements IRTSIndex {

    private ConcurrentHashMap<Integer, ConcurrentTriplePostingList> invertedIndex;

    // Maps TweetIDs to LSIITriplets containing all the information for score computation
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
        SortedPostingList resultList = new SortedPostingList();

        float upperBoundScore;

        // Threshold algorithm main loop
        int i = 0;
        while (true) {
            // Abort search when i exceeds posting lists' size
            // TODO: is this the correct stop condition?
            try {
                upperBoundScore = examineTPLIndexAtPosition(this.invertedIndex2, this.tripletHashMap, i, transportObjectQuery, resultList);
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
     * Scans PostingList of TPL Index at position, inserts results into resultList and returns
     * ScoreUpperBound
     *
     * @return  float  Highest possible score that any unseen tweet could have in this Index after
     *                 position i ("Score Upper Bound")
     */
    public static float examineTPLIndexAtPosition(
            ConcurrentHashMap<Integer, ConcurrentTPLArrayList> tplInvertedIndex,
            ConcurrentHashMap<Integer, LSIITriplet> tripletHashMap,
            int position,
            TransportObject transportObjectQuery,
            SortedPostingList resultList
    ) throws IndexOutOfBoundsException {
        // Initialize three sets for found Tweets at position i
        // Used to gather Tweets for insertion into resultList
        // and for calculation of score upper bound
        HashSet<ConcurrentSortedDateListElement> setFreshness = new HashSet<ConcurrentSortedDateListElement>();
        HashSet<ConcurrentSortedPostingListElement> setSignificance = new HashSet<ConcurrentSortedPostingListElement>();
        HashSet<ConcurrentSortedPostingListElement> setTermSimilarity = new HashSet<ConcurrentSortedPostingListElement>();

        List<Integer> termIDsInQuery = transportObjectQuery.getTermIDs();

        for (int termID : termIDsInQuery) {
            ConcurrentTPLArrayList tplArrayList =  tplInvertedIndex.get(termID);

            // Make sure all PostingLists have enough entries
            if (tplArrayList.getFreshnessPostingList().size() <= position
                    || tplArrayList.getSignificancePostingList().size() <= position
                    || tplArrayList.getTermSimilarityPostingList().size() <= position) {
                throw new IndexOutOfBoundsException();
            }

            // Fetch posting list elements at position
            ConcurrentSortedDateListElement freshnessPostingListElement = tplArrayList.getFreshnessPostingList().get(position);
            ConcurrentSortedPostingListElement significancePostingListElement = tplArrayList.getSignificancePostingList().get(position);
            ConcurrentSortedPostingListElement termSimilarityPostingListElement = tplArrayList.getTermSimilarityPostingList().get(position);

            // Add fetched posting list elements to sets
            setFreshness.add(freshnessPostingListElement);
            setSignificance.add(significancePostingListElement);
            setTermSimilarity.add(termSimilarityPostingListElement);
        }

        insertPostingListElementSetsIntoResultList(resultList, transportObjectQuery, tripletHashMap, setFreshness, setSignificance, setTermSimilarity);

        return calculateUpperBoundScoreF(transportObjectQuery, tripletHashMap, setFreshness, setSignificance, setTermSimilarity);
    }

    private static void insertPostingListElementSetsIntoResultList(
            SortedPostingList resultList,
            TransportObject transportObjectQuery,
            ConcurrentHashMap<Integer, LSIITriplet> tripletHashMap,
            HashSet<ConcurrentSortedDateListElement> setFreshness,
            HashSet<ConcurrentSortedPostingListElement> setSignificance,
            HashSet<ConcurrentSortedPostingListElement> setTermSimilarity
    ) {
        Iterator<ConcurrentSortedDateListElement> setFreshnessIterator = setFreshness.iterator();
        Iterator<ConcurrentSortedPostingListElement> setSignificanceIterator = setSignificance.iterator();
        Iterator<ConcurrentSortedPostingListElement> setTermSimilarityIterator = setTermSimilarity.iterator();

        while (setFreshnessIterator.hasNext()) {
            ConcurrentSortedDateListElement freshnessPostingListElement = setFreshnessIterator.next();
            int tweetID = freshnessPostingListElement.getTweetID();
            insertTweetIDIntoResultList(tweetID, resultList, transportObjectQuery, tripletHashMap);
        }

        while (setSignificanceIterator.hasNext()) {
            ConcurrentSortedPostingListElement significancePostingListElement = setSignificanceIterator.next();
            int tweetID = significancePostingListElement.getTweetID();
            insertTweetIDIntoResultList(tweetID, resultList, transportObjectQuery, tripletHashMap);
        }

        while (setTermSimilarityIterator.hasNext()) {
            ConcurrentSortedPostingListElement termSimilarityPostingListElement = setTermSimilarityIterator.next();
            int tweetID = termSimilarityPostingListElement.getTweetID();
            insertTweetIDIntoResultList(tweetID, resultList, transportObjectQuery, tripletHashMap);
        }
    }

    private static void insertTweetIDIntoResultList(
            int tweetID,
            SortedPostingList resultList,
            TransportObject transportObjectQuery,
            ConcurrentHashMap<Integer, LSIITriplet> tripletHashMap
    ) {
        // Fetch LSIITriplet for this tweetID
        LSIITriplet lsiiTriplet = tripletHashMap.get(tweetID);

        // Query variables
        int k = transportObjectQuery.getk();
        List<Integer> termIDsQuery = transportObjectQuery.getTermIDs();
        Date timestampQuery = transportObjectQuery.getTimestamp();

        // Calculate ranking function for this triplet
        List<Integer> termIDsInPost = lsiiTriplet.getTermIDs();
        Date timestampPost = lsiiTriplet.getTimestamp();

        float freshness = HelperFunctions.calculateFreshness(timestampPost, timestampQuery);
        float significance = lsiiTriplet.getSignificance();
        float similarity = HelperFunctions.calculateTermSimilarity(termIDsQuery, termIDsInPost);

        float fValue = HelperFunctions.calculateRankingFunction(freshness, significance, similarity);

        // Check if to be inserted tweet ID is already in result list
        // Remove and reinsert element if it is already contained and has a lower ranking value
        SortedPostingListElement elementInResultList = resultList.getSortedPostingListElement(tweetID);
        if (elementInResultList != null && elementInResultList.getSortKey() < fValue) {
            resultList.remove(elementInResultList);
        }

        // Insert directly in case ResultList has less than k entries
        // In case ResultList already contains k entries, check whether the last entry
        // (with the smallest ranking value) has a smaller ranking value
        if (resultList.size() < k) {
            resultList.insertSorted(tweetID, fValue);
        } else {
            SortedPostingListElement lastEntryInResultList = resultList.getLast();

            if (lastEntryInResultList.getSortKey() < fValue) {
                resultList.remove(lastEntryInResultList);
                resultList.insertSorted(tweetID, fValue);
            }
        }
    }

    private static float calculateUpperBoundScoreF(
            TransportObject transportObjectQuery,
            ConcurrentHashMap<Integer, LSIITriplet> tripletHashMap,
            HashSet<ConcurrentSortedDateListElement> setFreshness,
            HashSet<ConcurrentSortedPostingListElement> setSignificance,
            HashSet<ConcurrentSortedPostingListElement> setTermSimilarity)
    {
        Iterator<ConcurrentSortedDateListElement> setFreshnessIterator = setFreshness.iterator();
        Iterator<ConcurrentSortedPostingListElement> setSignificanceIterator = setSignificance.iterator();
        Iterator<ConcurrentSortedPostingListElement> setTermSimilarityIterator = setTermSimilarity.iterator();

        List<Integer> termIDsQuery = transportObjectQuery.getTermIDs();
        Date timestampQuery = transportObjectQuery.getTimestamp();

        List<Integer> allTermIDs = new LinkedList<Integer>();

        // Determine maximum freshness value from setFreshness
        float maxFreshnessValue = 0;
        while (setFreshnessIterator.hasNext()) {
            ConcurrentSortedDateListElement freshnessPostingListElement = setFreshnessIterator.next();
            int tweetID = freshnessPostingListElement.getTweetID();
            LSIITriplet lsiiTriplet = tripletHashMap.get(tweetID);

            // Calculate freshness value for this post
            // TODO: Rename getTimestamp() to getTimestamp()
            Date timestampPost = lsiiTriplet.getTimestamp();
            float freshnessValue = HelperFunctions.calculateFreshness(timestampPost, timestampQuery);

            if (freshnessValue > maxFreshnessValue) {
                maxFreshnessValue = freshnessValue;
            }

            allTermIDs.addAll(lsiiTriplet.getTermIDs());
        }

        // Determine maximum significance value from setSignificance
        float maxSignificanceValue = 0;
        while (setSignificanceIterator.hasNext()) {
            ConcurrentSortedPostingListElement significancePostingListElement = setSignificanceIterator.next();
            int tweetID = significancePostingListElement.getTweetID();
            LSIITriplet lsiiTriplet = tripletHashMap.get(tweetID);

            float significanceValue = lsiiTriplet.getSignificance();

            if (significanceValue > maxSignificanceValue) {
                maxSignificanceValue = significanceValue;
            }

            allTermIDs.addAll(lsiiTriplet.getTermIDs());
        }

        // Collect termIDs from setTermSimilarity
        while (setTermSimilarityIterator.hasNext()) {
            ConcurrentSortedPostingListElement termSimilarityPostingListElement = setTermSimilarityIterator.next();
            int tweetID = termSimilarityPostingListElement.getTweetID();
            LSIITriplet lsiiTriplet = tripletHashMap.get(tweetID);

            allTermIDs.addAll(lsiiTriplet.getTermIDs());
        }

        // Calculate overallTermSimilarity
        float overallTermSimilarity = HelperFunctions.calculateTermSimilarity(termIDsQuery, allTermIDs);

        return HelperFunctions.calculateRankingFunction(maxFreshnessValue, maxSignificanceValue, overallTermSimilarity);
    }

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

    /*
    public List<Integer> searchTweetIDs(TransportObject transportObjectQuery) {
        int k = transportObjectQuery.getk();
        List<Integer> topKTweetIDs = new ArrayList<Integer>();

        // TODO

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
        return topKTweetIDs;
    }
    */

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

                /*
                    FValue computation for TweetID in SignificanceList
                */
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

                /*
                    FValue computation for TweetID in TermSimilarityList
                */
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
            tripletHashMap.put(tweetID, new LSIITriplet(freshness, significance, similarity, termIDs));

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
