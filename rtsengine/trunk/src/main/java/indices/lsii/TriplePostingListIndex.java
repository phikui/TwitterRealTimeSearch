package indices.lsii;

import indices.IRTSIndex;
import model.TransportObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by chans on 5/14/15.
 */
public class TriplePostingListIndex implements IRTSIndex {

    private HashMap<Integer, TriplePostingList> invertedIndex;

    public ArrayList<Integer> searchTweetIDs(TransportObject transportObject, int k) {
        // TODO
        return null;
    }

    public void insertTransportObject(TransportObject transportObject) {

        // extract the important information from the transport object
        int tweetID = transportObject.getTweetID();
        //Date timeStamp = transportObject.getTimestamp();

        float significance = transportObject.getSignificance();
        float freshness = transportObject.getFreshness();
        float similarity = transportObject.getSimilarity();

        ArrayList<Integer> termIDs = transportObject.getTermIDs();


        for (int termID: termIDs) {
            TriplePostingList triplePostingListForTermID = this.invertedIndex.get(termID);

            // Create PostingList for this termID if necessary
            if (triplePostingListForTermID == null) {
                triplePostingListForTermID = new TriplePostingList();
                this.invertedIndex.put(termID, triplePostingListForTermID);
            }

            // Insert tweetID into posting lists for this term sorted on the key
            triplePostingListForTermID.getSignificancePostingList().insertSorted(tweetID, significance);
            triplePostingListForTermID.getFreshnessPostingList().insertSorted(tweetID, freshness);
            triplePostingListForTermID.getTermSimilarityPostingList().insertSorted(tweetID, similarity);
        }
    }
}
