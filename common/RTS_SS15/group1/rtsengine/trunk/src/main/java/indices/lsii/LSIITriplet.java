package indices.lsii;

import java.util.Date;
import java.util.List;

/**
 * Created by Maik on 26.05.2015.
 */
public class LSIITriplet {

    private Date timestamp;
    private float significance;
    private List<Integer> termIDs;

    public LSIITriplet(Date timestamp, float significance, List<Integer> termIDs) {
        this.timestamp = timestamp;
        this.significance = significance;
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

    public List<Integer> getTermIDs() {
        return this.termIDs;
    }

    public void setTermIDs(List<Integer> termIDs) {
        this.termIDs = termIDs;
    }
}
