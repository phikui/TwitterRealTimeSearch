package indices.tpl;

import indices.lsii.LSIITriplet;
import indices.postingarraylists.ConcurrentTPLArrayList;
import indices.postinglists.ConcurrentSortedDateListElement;
import indices.postinglists.ConcurrentSortedPostingListElement;
import indices.postinglists.SortedPostingList;
import indices.postinglists.SortedPostingListElement;
import model.TransportObject;
import utilities.HelperFunctions;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chans on 6/1/15.
 */
public class TPLHelper {

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
        if (elementInResultList != null && elementInResultList.getSortKey() <= fValue) {
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

}