package indices.lsii;

import indices.IRTSIndex;
import model.TransportObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Inverted Index mapping one TermID to one UnsortedPostingList
 * Arriving tweets always have the highest significance/timestamp and
 * are added to the end of the unsorted Posting List. Therefore the
 * posting list will automatically be sorted by ascending
 * significance/timestamp.
 */
public class AppendOnlyIndex implements IRTSIndex {
    private HashMap<Integer, UnsortedPostingList> invertedIndex;

    public AppendOnlyIndex() {
        this.invertedIndex = new HashMap<Integer, UnsortedPostingList>();
    }

    public ArrayList<Integer> searchTweetIDs(String terms, Date timestamp, int k) {
        // TODO
        return null;
    }

    public void insertTransportObject(TransportObject transportObject) {
        int tweetID = transportObject.getTweetID();
        ArrayList<Integer> termIDs = transportObject.getTermIDs();

        // For each termID of the tweet insert the tweetID into
        // the corresponding PostingList
        for (int termID: termIDs) {
            UnsortedPostingList postingListForTermID = this.invertedIndex.get(termID);

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
}
