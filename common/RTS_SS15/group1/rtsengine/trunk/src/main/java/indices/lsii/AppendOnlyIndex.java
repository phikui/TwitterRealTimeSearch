package indices.lsii;

import indices.IRTSIndex;
import model.TransportObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Inverted Index mapping one TermID to one UnsortedPostingList
 * Arriving tweets always have the highest significance/timestamp and
 * are added to the end of the unsorted Posting List. Therefore the
 * posting list will automatically be sorted by ascending
 * significance/timestamp.
 */
public class AppendOnlyIndex implements IRTSIndex {
    private HashMap<Integer, UnsortedPostingList> invertedIndex;

    // TODO: Use concurrent HashMap? Yes!
    public AppendOnlyIndex() {
        this.invertedIndex = new HashMap<Integer, UnsortedPostingList>();
    }

    // TODO: currently it returns the tweetIDs sorted by ascending timestamp
    // TODO: clarify how result tweetID lists should be sorted (ascending or descending)
    public List<Integer> searchTweetIDs(TransportObject transportObjectQuery) {
        int k = transportObjectQuery.getk();
        List<Integer> termIDsInQuery = transportObjectQuery.getTermIDs();

        // Init result tweetID list of size k
        List<Integer> resultTweetIDs = new ArrayList<Integer>(k);

        // Maintain a queue of iterators for each posting list
        LinkedBlockingQueue<ListIterator<Integer>> postingListIteratorQueue = new LinkedBlockingQueue<ListIterator<Integer>>();

        // Add one iterator for each posting list corresponding
        // to a termID in the query
        for (int termIDInQuery : termIDsInQuery) {
            UnsortedPostingList postingListForTermIDInQuery = this.invertedIndex.get(termIDInQuery);

            // Posting list for this termID exists?
            if (postingListForTermIDInQuery == null) {
                continue;
            }

            // Fetch list iterator for this posting list
            ListIterator<Integer> postingListIteratorForTermIDInQuery = postingListForTermIDInQuery.listIterator();

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
            ListIterator<Integer> postingListIterator = postingListIteratorQueue.remove();

            // ListIterators in the queue always contain at least one element
            int foundTweetID = postingListIterator.next();
            resultTweetIDs.add(foundTweetID);

            // Put the list iterator back to the queue in case it has more elements
            if (postingListIterator.hasNext()) {
                postingListIteratorQueue.add(postingListIterator);
            }
        }

        return resultTweetIDs;
    }

//    public List<Integer> searchTweetIDs(TransportObject transportObject) {
//        int k = transportObject.getk();
//        List<Integer> termIDs = transportObject.getTermIDs();
//        // for each term maintaining a posting list for the querry object
//        for (int termID: termIDs) {
//            UnsortedPostingList postingListForEachTermID=  this.invertedIndex.get(termID);
//            // traversing the each list and maintaining the top k element needed
//            int value;
//            int count=0;
//            value = postingListForEachTermID.getLast();
//            for (int i=value;i>=postingListForEachTermID.getFirst();i--)
//            {
//                // using a counter to get the K term
//                count++;
//                UnsortedPostingList postingListForEachKTerm= this.invertedIndex.get(termID);
//                if(count==k)
//                    break;
//                // also need to write condition for freshness score
//
//                // need to compare the top k elements in the list and find the actual result
//                //needed freshness score
//               float freshness= transportObject.calculateFreshness(); //To Make it verify
//                //Date dateValue=transportObject.getTimestamp();
//                // int hh=dateValue.getHours();
//                //int dd=dateValue.getMinutes();
//                //int mm=dateValue.getSeconds();
//                //String time= hh+":"+mm+":"+ dd;
//                float w1_fresh = ConfigurationObject.getwFreshness();
//                float w1_significance= ConfigurationObject.getwSignificance();
//                float w1_similarity = ConfigurationObject.getwSimilarity();
//                float freshness_score;
//                freshness_score=(w1_fresh*freshness+w1_significance*0+w1_similarity*0);
//            }
//
//
//        }
//        // was trying for freshnes score
//
//        return null;
//    }

    public void insertTransportObject(TransportObject transportObjectInsertion) {
        int tweetID = transportObjectInsertion.getTweetID();
        List<Integer> termIDs = transportObjectInsertion.getTermIDs();

        // For each termID of the tweet insert the tweetID into
        // the corresponding PostingList
        for (int termID: termIDs) {
            UnsortedPostingList postingListForTermID = this.invertedIndex.get(termID);
            //caluculate the time of each posting list


            // Create PostingList for this termID if necessary
            if (postingListForTermID == null) {
                postingListForTermID = new UnsortedPostingList();
                this.invertedIndex.put(termID, postingListForTermID);
            }



            // Insert tweetID into posting list for this term at the last position
            // of the posting list, since this is the latest arriving tweet with the
            // highest freshness value. Insertion done in O(1) here.
            postingListForTermID.add(tweetID);
        }
    }


    public int size() {
        return invertedIndex.size();
    }
}
