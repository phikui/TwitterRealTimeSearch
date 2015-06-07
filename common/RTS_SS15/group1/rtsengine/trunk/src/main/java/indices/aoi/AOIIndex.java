package indices.aoi;

import indices.IRTSIndex;
import indices.postinglists.IPostingList;
import indices.postinglists.IPostingListElement;
import indices.postinglists.PostingList;
import indices.postinglists.PostingListElement;
import model.TransportObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Inverted IndexTypes mapping one TermID to one UnsortedPostingList
 * Arriving tweets always have the highest significance/timestamp and
 * are added to the end of the unsorted Posting List. Therefore the
 * posting list will automatically be sorted by ascending
 * significance/timestamp.
 */
public class AOIIndex implements IRTSIndex {
    private ConcurrentHashMap<Integer, IPostingList> invertedIndex;

    public AOIIndex() {
        this.invertedIndex = new ConcurrentHashMap<Integer, IPostingList>();
    }

    public List<Integer> searchTweetIDs(TransportObject transportObjectQuery) {
        int k = transportObjectQuery.getk();
        List<Integer> termIDsInQuery = transportObjectQuery.getTermIDs();

        // Init result tweetID list of size k
        List<Integer> resultTweetIDs = new ArrayList<Integer>(k);

        // Maintain a queue of iterators for each posting list
        LinkedBlockingQueue<Iterator<IPostingListElement>> postingListIteratorQueue = new LinkedBlockingQueue<Iterator<IPostingListElement>>();

        // Add one iterator for each posting list corresponding
        // to a termID in the query
        for (int termIDInQuery : termIDsInQuery) {
            IPostingList postingListForTermIDInQuery = this.invertedIndex.get(termIDInQuery);

            // Posting list for this termID exists?
            if (postingListForTermIDInQuery == null) {
                continue;
            }

            // Fetch list iterator for this posting list
            Iterator<IPostingListElement> postingListIteratorForTermIDInQuery = postingListForTermIDInQuery.iterator();

            // Add iterator to queue if it contains at least one element
            if (postingListIteratorForTermIDInQuery.hasNext()) {
                postingListIteratorQueue.add(postingListIteratorForTermIDInQuery);
            }
        }

        // In each iteration take next element from each iterator
        // either until the queue of iterators is empty (i.e. all
        // posting lists for query terms fully scanned) or until
        // the result list has reached size k.
        while (postingListIteratorQueue.size() > 0 && resultTweetIDs.size() < k) {
            Iterator<IPostingListElement> postingListIterator = postingListIteratorQueue.remove();

            // Iterators in the queue always contain at least one element
            IPostingListElement nextPostingListElement = postingListIterator.next();
            int nextTweetID = nextPostingListElement.getTweetID();

            if (!resultTweetIDs.contains(nextTweetID)) {
                resultTweetIDs.add(nextTweetID);
            }

            // Put the list iterator back to the queue in case it has more elements
            if (postingListIterator.hasNext()) {
                postingListIteratorQueue.add(postingListIterator);
            }
        }

        return resultTweetIDs;
    }

    public void insertTransportObject(TransportObject transportObjectInsertion) {
        int tweetID = transportObjectInsertion.getTweetID();
        List<Integer> termIDs = transportObjectInsertion.getTermIDs();

        // For each termID of the tweet insert the tweetID into
        // the corresponding PostingList
        for (int termID: termIDs) {
            IPostingList postingListForTermID = this.invertedIndex.get(termID);

            // Create PostingList for this termID if necessary
            if (postingListForTermID == null) {
                postingListForTermID = new PostingList();
                this.invertedIndex.put(termID, postingListForTermID);
            }

            // Insert tweetID into posting list for this term at the first position
            // of the posting list, since this is the latest arriving tweet with the
            // highest freshness value. Insertion done in O(1) here.
            IPostingListElement insertElement = new PostingListElement(tweetID, 0);
            postingListForTermID.addFirst(insertElement);
        }
    }

    public int size() {
        return invertedIndex.size();
    }
}
