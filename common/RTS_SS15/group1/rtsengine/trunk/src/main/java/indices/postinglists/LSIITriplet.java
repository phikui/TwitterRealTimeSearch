package indices.postinglists;

import java.util.Date;

/**
 * Created by Maik on 26.05.2015.
 */
public class LSIITriplet {

    private Date date;
    private float significance;
    private float termSimilarity;

    public LSIITriplet(Date date, float significance, float termSimilarity) {
        this.date = date;
        this.significance = significance;
        this.termSimilarity = termSimilarity;
    }

    public float getSignificance() {
        return significance;
    }

    public void setSignificance(float significance) {
        this.significance = significance;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public float getTermSimilarity() {
        return termSimilarity;
    }

    public void setTermSimilarity(float termSimilarity) {
        this.termSimilarity = termSimilarity;
    }
}
