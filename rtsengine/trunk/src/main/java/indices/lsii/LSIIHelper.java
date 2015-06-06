package indices.lsii;

import indices.postinglists.*;
import model.TransportObject;
import model.TweetDictionary;
import utilities.HelperFunctions;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Maik on 05.06.2015.
 */
public class LSIIHelper {

    public static void examineAOIndexAtPosition(
            ConcurrentHashMap<Integer, IPostingList> AOInvertedIndex,
            HashMap<Integer, Iterator<IPostingListElement>> postingListIteratorMap,
            TransportObject transportObjectQuery,
            IPostingList resultList,
            Date maxTimestamp
    ) throws IndexOutOfBoundsException {

        List<Integer> termIDsInQuery = transportObjectQuery.getTermIDs();

        for (int termID : termIDsInQuery) {
            Iterator<IPostingListElement> AOIterator = postingListIteratorMap.get(AOInvertedIndex.get(termID));

            // Create iterator and put into postingListIteratorMap if non-existent
            if (AOIterator == null) {
                AOIterator = AOInvertedIndex.get(termID).iterator();
                postingListIteratorMap.put(termID, AOIterator);
            }

            // Make sure the AO postinglist has a next entry
            if (!AOIterator.hasNext()) {
                throw new IndexOutOfBoundsException();
            }

            IPostingListElement dateListElement = AOIterator.next();

            // stop here to avoid reader/writer conflict as maxTimestamp is the newest object, which may not be inserted
            if (dateListElement.getSortKey() == maxTimestamp.getTime()){
                return;
            }

            insertTweetIDIntoResultList(dateListElement.getTweetID(), resultList, transportObjectQuery);

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


}
