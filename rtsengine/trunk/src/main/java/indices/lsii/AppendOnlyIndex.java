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
            int count=0;
            value = postingListForEachTermID.getLast();
            for (int i=value;i>=postingListForEachTermID.getFirst();i--)
            {
                // using a counter to get the K term
                count++;
                UnsortedPostingList postingListForEachKTerm= this.invertedIndex.get(termID);
                if(count==k)
                    break;
                // also need to write condition for freshness score

                // need to compare the top k elements in the list and find the actual result
                //needed freshness score
               float freshness= transportObject.getFreshness();//To Make it verify
                //Date dateValue=transportObject.getTimestamp();
                // int hh=dateValue.getHours();
                //int dd=dateValue.getMinutes();
                //int mm=dateValue.getSeconds();
                //String time= hh+":"+mm+":"+ dd;
                float w1_fresh=(1/3);
                float w1_significance=(1/3);
                float w1_similarity=(1/3);
                float freshness_score;
                freshness_score=(w1_fresh*freshness+w1_significance*0+w1_similarity*0);


            }


        }
        // was trying for freshnes score



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
