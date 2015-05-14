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

    public ArrayList<Integer> searchTweetIDs(TransportObject transportObject, int k) {
        ArrayList<Integer> termIDs = transportObject.getTermIDs();
        // for each term maintaining a posting list for the querry object
        for (int termID: termIDs){
            UnsortedPostingList postingListForEachTermID=  this.invertedIndex.get(termID);
            // traversing the each list and maintaining the top k element needed
            int value;
            value = postingListForEachTermID.getLast();
            for (int i=value; i>=k;i++)
            {
                UnsortedPostingList postingListForEachKTerm=  this.invertedIndex.get(termID);
                // need to compare the top k elements in the list and find the actually result

            }


        }
        // was trying for freshnes score
        //Date dateValue=transportObject.getTimestamp();
        // int hh=dateValue.getHours();
        //int dd=dateValue.getMinutes();
        //int mm=dateValue.getSeconds();
        //String time= hh+":"+mm+":"+ dd;
        //float freshness_score;
        //float w1,w2,w3= (1/3);
        //freshness_score=(0.33*time+0+0);


        return null;
    }

    public void insertTransportObject(TransportObject transportObject) {
        int tweetID = transportObject.getTweetID();
        ArrayList<Integer> termIDs = transportObject.getTermIDs();

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
}
