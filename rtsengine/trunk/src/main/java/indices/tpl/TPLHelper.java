package indices.tpl;

import indices.postinglists.*;
import model.TransportObject;
import model.TweetDictionary;
import utilities.HelperFunctions;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chans on 6/1/15.
 */
public class TPLHelper {

    /**
     * Scans PostingList of TPL Index, inserts results into resultList and returns ScoreUpperBound.
     * Uses postingListIteratorMap to fetch next elements from involved PostingLists.
     *
     * @return  float  Highest possible score that any unseen tweet could have in this Index after
     *                 position i ("Score Upper Bound")
     *
     * @throws  IndexOutOfBoundsException  In case all involved PostingLists have reached their end
     */
    public static float examineTPLIndex(
            ConcurrentHashMap<Integer, ITriplePostingList> tplInvertedIndex,
            HashMap<Integer, Iterator<IPostingListElement>> postingListIteratorMap,
            TransportObject transportObjectQuery,
            IPostingList resultList
    ) throws IndexOutOfBoundsException {
        // Initialize three sets for found Tweets in this iteration.
        // Used to gather Tweets for insertion into resultList
        // and for calculation of score upper bound.
        HashSet<Integer> setFreshness = new HashSet<Integer>();
        HashSet<Integer> setSignificance = new HashSet<Integer>();
        HashSet<Integer> setTermSimilarity = new HashSet<Integer>();

        List<Integer> termIDsInQuery = transportObjectQuery.getTermIDs();

        // Throw IndexOutOfBoundsException if all PostingLists have reached their end
        boolean allPostingListsHaveReachedEnd = true;

        for (int termID : termIDsInQuery) {
            ITriplePostingList tplArrayList = tplInvertedIndex.get(termID);

            // Fetch posting lists for this termID
            IPostingList freshnessPostingList = tplArrayList.getFreshnessPostingList();
            IPostingList significancePostingList = tplArrayList.getSignificancePostingList();
            IPostingList termSimilarityPostingList = tplArrayList.getTermSimilarityPostingList();

            // Check for all fetched PostingLists whether Iterator already exists in postingListIteratorMap
            Iterator<IPostingListElement> freshnessPostingListIterator = postingListIteratorMap.get(freshnessPostingList.getPostingListID());
            Iterator<IPostingListElement> significancePostingListIterator = postingListIteratorMap.get(significancePostingList.getPostingListID());
            Iterator<IPostingListElement> termSimilarityPostingListIterator = postingListIteratorMap.get(termSimilarityPostingList.getPostingListID());

            // Create iterators and put into postingListIteratorMap if non-existent
            if (freshnessPostingList == null) {
                freshnessPostingListIterator = freshnessPostingList.iterator();
                postingListIteratorMap.put(freshnessPostingList.getPostingListID(), freshnessPostingListIterator);
            }
            if (significancePostingList == null) {
                significancePostingListIterator = significancePostingList.iterator();
                postingListIteratorMap.put(significancePostingList.getPostingListID(), significancePostingListIterator);
            }
            if (termSimilarityPostingList == null) {
                termSimilarityPostingListIterator = termSimilarityPostingList.iterator();
                postingListIteratorMap.put(termSimilarityPostingList.getPostingListID(), termSimilarityPostingListIterator);
            }

            // Fetch next tweetID from each PostingList Iterator and insert into sets
            if (freshnessPostingListIterator.hasNext()) {
                allPostingListsHaveReachedEnd = false;
                IPostingListElement freshnessPostingListElement = freshnessPostingListIterator.next();
                int freshnessTweetID = freshnessPostingListElement.getTweetID();
                setFreshness.add(freshnessTweetID);
            }
            if (significancePostingListIterator.hasNext()) {
                allPostingListsHaveReachedEnd = false;
                IPostingListElement significancePostingListElement = significancePostingListIterator.next();
                int significanceTweetID = significancePostingListElement.getTweetID();
                setSignificance.add(significanceTweetID);
            }
            if (termSimilarityPostingListIterator.hasNext()) {
                allPostingListsHaveReachedEnd = false;
                IPostingListElement termSimilarityPostingListElement = termSimilarityPostingListIterator.next();
                int termSimilarityTweetID = termSimilarityPostingListElement.getTweetID();
                setTermSimilarity.add(termSimilarityTweetID);
            }
        }

        if (allPostingListsHaveReachedEnd) {
            throw new IndexOutOfBoundsException();
        }

        insertPostingListElementSetsIntoResultList(resultList, transportObjectQuery, setFreshness, setSignificance, setTermSimilarity);

        return calculateUpperBoundScoreF(transportObjectQuery, setFreshness, setSignificance, setTermSimilarity);
    }

    private static void insertPostingListElementSetsIntoResultList(
            IPostingList resultList,
            TransportObject transportObjectQuery,
            HashSet<Integer> setFreshness,
            HashSet<Integer> setSignificance,
            HashSet<Integer> setTermSimilarity
    ) {
        Iterator<Integer> setFreshnessIterator = setFreshness.iterator();
        Iterator<Integer> setSignificanceIterator = setSignificance.iterator();
        Iterator<Integer> setTermSimilarityIterator = setTermSimilarity.iterator();

        while (setFreshnessIterator.hasNext()) {
            int tweetID = setFreshnessIterator.next();
            insertTweetIDIntoResultList(tweetID, resultList, transportObjectQuery);
        }

        while (setSignificanceIterator.hasNext()) {
            int tweetID = setSignificanceIterator.next();
            insertTweetIDIntoResultList(tweetID, resultList, transportObjectQuery);
        }

        while (setTermSimilarityIterator.hasNext()) {
            int tweetID = setTermSimilarityIterator.next();
            insertTweetIDIntoResultList(tweetID, resultList, transportObjectQuery);
        }
    }

    private static void insertTweetIDIntoResultList(
            int tweetID,
            IPostingList resultList,
            TransportObject transportObjectQuery
    ) {
        // Fetch TransportObject for this tweetID
        TransportObject transportObject = TweetDictionary.getTransportObject(tweetID);

        // Query variables
        int k = transportObjectQuery.getk();
        List<Integer> termIDsQuery = transportObjectQuery.getTermIDs();
        Date timestampQuery = transportObjectQuery.getTimestamp();

        // Calculate ranking function for this transportObject
        List<Integer> termIDsInPost = transportObject.getTermIDs();
        Date timestampPost = transportObject.getTimestamp();

        float freshness = HelperFunctions.calculateFreshness(timestampPost, timestampQuery);
        float significance = transportObject.getSignificance();
        float similarity = HelperFunctions.calculateTermSimilarity(termIDsQuery, termIDsInPost);

        float fValue = HelperFunctions.calculateRankingFunction(freshness, significance, similarity);

        // Check if to be inserted tweet ID is already in result list
        // Remove and reinsert element if it is already contained and has a lower ranking value
        IPostingListElement elementInResultList = resultList.getPostingListElement(tweetID);
        if (elementInResultList != null && elementInResultList.getSortKey() <= fValue) {
            resultList.remove(elementInResultList);
        }

        // Insert directly in case ResultList has less than k entries
        // In case ResultList already contains k entries, check whether the last entry
        // (with the smallest ranking value) has a smaller ranking value
        if (resultList.size() < k) {
            resultList.insertSorted(tweetID, fValue);
        } else {
            IPostingListElement lastEntryInResultList = resultList.getLast();

            if (lastEntryInResultList.getSortKey() < fValue) {
                resultList.remove(lastEntryInResultList);
                resultList.insertSorted(tweetID, fValue);
            }
        }
    }

    private static float calculateUpperBoundScoreF(
            TransportObject transportObjectQuery,
            HashSet<Integer> setFreshness,
            HashSet<Integer> setSignificance,
            HashSet<Integer> setTermSimilarity)
    {
        Iterator<Integer> setFreshnessIterator = setFreshness.iterator();
        Iterator<Integer> setSignificanceIterator = setSignificance.iterator();
        Iterator<Integer> setTermSimilarityIterator = setTermSimilarity.iterator();

        List<Integer> termIDsQuery = transportObjectQuery.getTermIDs();
        Date timestampQuery = transportObjectQuery.getTimestamp();

        List<Integer> allTermIDs = new LinkedList<Integer>();

        // Determine maximum freshness value from setFreshness
        float maxFreshnessValue = 0;
        while (setFreshnessIterator.hasNext()) {
            Integer tweetID = setFreshnessIterator.next();
            TransportObject transportObject = TweetDictionary.getTransportObject(tweetID);

            // Calculate freshness value for this post
            Date timestampPost = transportObject.getTimestamp();
            float freshnessValue = HelperFunctions.calculateFreshness(timestampPost, timestampQuery);

            if (freshnessValue > maxFreshnessValue) {
                maxFreshnessValue = freshnessValue;
            }

            allTermIDs.addAll(transportObject.getTermIDs());
        }

        // Determine maximum significance value from setSignificance
        float maxSignificanceValue = 0;
        while (setSignificanceIterator.hasNext()) {
            int tweetID = setSignificanceIterator.next();
            TransportObject transportObject = TweetDictionary.getTransportObject(tweetID);

            float significanceValue = transportObject.getSignificance();

            if (significanceValue > maxSignificanceValue) {
                maxSignificanceValue = significanceValue;
            }

            allTermIDs.addAll(transportObject.getTermIDs());
        }

        // Collect termIDs from setTermSimilarity
        while (setTermSimilarityIterator.hasNext()) {
            int tweetID = setTermSimilarityIterator.next();
            TransportObject transportObject = TweetDictionary.getTransportObject(tweetID);

            allTermIDs.addAll(transportObject.getTermIDs());
        }

        // Calculate overallTermSimilarity
        float overallTermSimilarity = HelperFunctions.calculateTermSimilarity(termIDsQuery, allTermIDs);

        return HelperFunctions.calculateRankingFunction(maxFreshnessValue, maxSignificanceValue, overallTermSimilarity);
    }

}