package indices.tpl;

import indices.IRTSIndex;
import indices.deprecated.ConcurrentSortedDateListElement;
import indices.deprecated.ConcurrentSortedPostingListElement;
import indices.deprecated.ConcurrentTriplePostingList;
import indices.deprecated.SortedPostingList;
import indices.deprecated.ConcurrentTPLArrayList;
import model.TransportObject;
import utilities.HelperFunctions;

import java.util.*;


import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chans on 5/14/15.
 */
public class TPLIndex implements IRTSIndex {

    private ConcurrentHashMap<Integer, ConcurrentTriplePostingList> invertedIndex;
    // Testing purpose only
    private ConcurrentHashMap<Integer, ConcurrentTPLArrayList> invertedIndex2;

    public TPLIndex() {
        this.invertedIndex = new ConcurrentHashMap<Integer, ConcurrentTriplePostingList>();

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
                upperBoundScore = TPLHelper.examineTPLIndexAtPosition(this.invertedIndex2, i, transportObjectQuery, resultList);
                System.out.println("Upper Bound Score: " + upperBoundScore);
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
}
