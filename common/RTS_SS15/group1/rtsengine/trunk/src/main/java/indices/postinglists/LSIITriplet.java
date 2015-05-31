package indices.postinglists;

import java.util.Date;
import java.util.List;

/**
 * Created by Maik on 26.05.2015.
 */
public class LSIITriplet {

    private Date timestamp;
    private float significance;
    // TODO: is this really needed? termIDs is probably required which makes storing
    //       termSimilarity obsolete
    private float termSimilarity;

    private List<Integer> termIDs;

    public LSIITriplet(Date timestamp, float significance, float termSimilarity, List<Integer> termIDs) {
        this.timestamp = timestamp;
        this.significance = significance;
        this.termSimilarity = termSimilarity;
        this.termIDs = termIDs;
    }

    public float getSignificance() {
        return significance;
    }

    public void setSignificance(float significance) {
        this.significance = significance;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public float getTermSimilarity() {
        return termSimilarity;
    }

    public void setTermSimilarity(float termSimilarity) {
        this.termSimilarity = termSimilarity;
    }

    public List<Integer> getTermIDs() {
        return this.termIDs;
    }

    public void setTermIDs(List<Integer> termIDs) {
        this.termIDs = termIDs;
    }
}
