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
            if (dateListElement.getSortKey() == maxTimestamp.getTime()) {
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


    public static void mergeWithNextIndex(int currentIndex, int termID, int i0Size, TransportObject transportObject, ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ITriplePostingList>> invertedIndex, ConcurrentHashMap<Integer, IPostingList> index_zero) {

        boolean performedMerging = false;

        // create new index if necessary, if created then we can directly merge here
        if (invertedIndex.get(currentIndex) == null) {
            ConcurrentHashMap termMap = new ConcurrentHashMap<Integer, ITriplePostingList>();
            invertedIndex.put(currentIndex, termMap);

            // in case if the new created index is I_1, we first generate the TPL structure, otherwise just merge as both are TPL structures
            if (currentIndex == 1) {
                ITriplePostingList triplePostingList = new TriplePostingList(termID);
                triplePostingList = createMissingLists(termID, transportObject, triplePostingList, index_zero);

                ITriplePostingList shadowIndex = HelperFunctions.mergeTriplePostingLists(triplePostingList, invertedIndex.get(currentIndex).get(termID), termID);
                invertedIndex.get(currentIndex).put(termID, shadowIndex);
                performedMerging = true;
            } else {
                ITriplePostingList shadowIndex = HelperFunctions.mergeTriplePostingLists(invertedIndex.get(currentIndex - 1).get(termID), invertedIndex.get(currentIndex).get(termID), termID);
                invertedIndex.get(currentIndex).put(termID, shadowIndex);
                performedMerging = true;
            }

        } else if (invertedIndex.get(currentIndex).get(termID).getFreshnessPostingList().size() < (currentIndex * 2 * i0Size)) {

            // same as above, if index is I_1, first create lists, else just merge
            if (currentIndex == 1) {
                ITriplePostingList triplePostingList = new TriplePostingList(termID);
                triplePostingList = createMissingLists(termID, transportObject, triplePostingList, index_zero);

                ITriplePostingList shadowIndex = HelperFunctions.mergeTriplePostingLists(triplePostingList, invertedIndex.get(currentIndex).get(termID), termID);
                invertedIndex.get(currentIndex).put(termID, shadowIndex);
                performedMerging = true;
            } else {
                ITriplePostingList shadowIndex = HelperFunctions.mergeTriplePostingLists(invertedIndex.get(currentIndex - 1).get(termID), invertedIndex.get(currentIndex).get(termID), termID);
                invertedIndex.get(currentIndex).put(termID, shadowIndex);
                performedMerging = true;
            }

        } else {
            // recursive searching for the next free index to merge with
            mergeWithNextIndex(currentIndex++, termID, i0Size, transportObject, invertedIndex, index_zero);
        }

        // cleanup: clear I_i-1
        // TODO need some locking here: delete old indices if no query is working on them
        if ((currentIndex == 1) && performedMerging) {
            index_zero.get(termID).clear();

        } else if ((currentIndex > 1) && performedMerging) {
            invertedIndex.get(currentIndex - 1).get(termID).getFreshnessPostingList().clear();
            invertedIndex.get(currentIndex - 1).get(termID).getSignificancePostingList().clear();
            invertedIndex.get(currentIndex - 1).get(termID).getTermSimilarityPostingList().clear();
        }

    }

    private static ITriplePostingList createMissingLists(int termID, TransportObject transportObject, ITriplePostingList triplePostingList, ConcurrentHashMap<Integer, IPostingList> index_zero) {

        // iterate through I_0 to calculate the missing values
        Iterator<IPostingListElement> AOIterator = index_zero.get(termID).iterator();

        while (AOIterator.hasNext()) {
            IPostingListElement tweetIDElement = AOIterator.next();

            // calculate all the missing values and insert them sorted into the new TPL structure
            triplePostingList.getFreshnessPostingList().insertSortedByTimestamp(tweetIDElement.getTweetID());
            triplePostingList.getSignificancePostingList().insertSortedBySignificance(tweetIDElement.getTweetID());
            triplePostingList.getTermSimilarityPostingList().insertSortedByTermSimilarity(tweetIDElement.getTweetID());
        }

        return triplePostingList;
    }


}
