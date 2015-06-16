package indices.lsii;

import indices.postinglists.*;
import model.TransportObject;
import model.TweetDictionary;
import utilities.HelperFunctions;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

            if(AOInvertedIndex.get(termID) == null){
                continue;
            }

            Iterator<IPostingListElement> AOIterator = postingListIteratorMap.get(termID);

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

            // TODO this breaks the testing because the dates are the same
            // stop here to avoid reader/writer conflict as maxTimestamp is the newest object, which may not be inserted
            /*
            if (dateListElement.getSortKey() == maxTimestamp.getTime()) {
                System.out.println("Terminating list traversal early as newest object is currently not written.");
                return;
            }*/

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


    public static void mergeWithNextIndex(int termID, int i0Size, ConcurrentHashMap<Integer, ConcurrentHashMap<Integer, ITriplePostingList>> invertedIndex, ConcurrentHashMap<Integer, IPostingList> index_zero) {

        int lastIndex = 0;
        int newIndex = 0;
        for (int index : invertedIndex.keySet()) {

            //System.out.println("Index: " + index + " for termID: " + termID);

            if (invertedIndex.get(index).get(termID) == null) {
                ITriplePostingList tpl = new TriplePostingList(termID);
                invertedIndex.get(index).put(termID, tpl);
            }

            if (invertedIndex.get(index).get(termID).getFreshnessPostingList().size() < (Math.pow(2, index) * i0Size)) {

                if (index == 1) {
                    ITriplePostingList triplePostingList = new TriplePostingList(termID);
                    triplePostingList = createMissingLists(termID, triplePostingList, index_zero);

                    ITriplePostingList shadowIndex = HelperFunctions.mergeTriplePostingLists(triplePostingList, invertedIndex.get(index).get(termID), termID);
                    invertedIndex.get(index).put(termID, shadowIndex);

                    // cleanup
                    //System.out.println("clear = "+ index);
                    index_zero.get(termID).clear();
                    return;

                } else {
                    ITriplePostingList shadowIndex = HelperFunctions.mergeTriplePostingLists(invertedIndex.get(index - 1).get(termID), invertedIndex.get(index).get(termID), termID);
                    invertedIndex.get(index).put(termID, shadowIndex);

                    // cleanup
                    //System.out.println("clear = "+ index);
                    invertedIndex.get(index - 1).get(termID).getFreshnessPostingList().clear();
                    invertedIndex.get(index - 1).get(termID).getSignificancePostingList().clear();
                    invertedIndex.get(index - 1).get(termID).getTermSimilarityPostingList().clear();
                    return;
                }

            }
            lastIndex = index;
        }

        newIndex = lastIndex + 1;

        if (invertedIndex.get(newIndex) == null) {
            ConcurrentHashMap termMap = new ConcurrentHashMap<Integer, ITriplePostingList>();
            invertedIndex.put(newIndex, termMap);

            ITriplePostingList tpl = new TriplePostingList(termID);
            invertedIndex.get(newIndex).put(termID, tpl);

            // in case if the new created index is I_1, we first generate the TPL structure, otherwise just merge as both are TPL structures
            if (newIndex == 1) {
                ITriplePostingList triplePostingList = new TriplePostingList(termID);
                triplePostingList = createMissingLists(termID, triplePostingList, index_zero);

                ITriplePostingList shadowIndex = HelperFunctions.mergeTriplePostingLists(triplePostingList, invertedIndex.get(newIndex).get(termID), termID);
                invertedIndex.get(newIndex).put(termID, shadowIndex);

                // cleanup
                index_zero.get(termID).clear();

            } else {
                ITriplePostingList shadowIndex = HelperFunctions.mergeTriplePostingLists(invertedIndex.get(newIndex - 1).get(termID), invertedIndex.get(newIndex).get(termID), termID);
                invertedIndex.get(newIndex).put(termID, shadowIndex);

                // cleanup
                invertedIndex.get(newIndex - 1).get(termID).getFreshnessPostingList().clear();
                invertedIndex.get(newIndex - 1).get(termID).getSignificancePostingList().clear();
                invertedIndex.get(newIndex - 1).get(termID).getTermSimilarityPostingList().clear();

            }

        }

    }


    private static ITriplePostingList createMissingLists(int termID, ITriplePostingList triplePostingList, ConcurrentHashMap<Integer, IPostingList> index_zero) {

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
