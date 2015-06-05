package indices.deprecated;

import java.util.Date;

/**
 * Created by Maik on 21.05.2015.
 */
public class ConcurrentSortedDateListElement implements Comparable<ConcurrentSortedDateListElement> {

    // Posting List will be sorted according to the date
    private Date date;
    private int tweetID;

    public ConcurrentSortedDateListElement(int tweetID, Date date) {
        this.date = date;
        this.tweetID = tweetID;
    }

    public Date getDate() {
        return date;
    }

    public int getTweetID() {
        return tweetID;
    }

    public int compareTo(ConcurrentSortedDateListElement elem) {
        if (elem.getDate().getTime() == this.getDate().getTime()) {
            return 0;
        }
        if (elem.getDate().getTime() > this.getDate().getTime()) {
            return 1;
        }
        if (elem.getDate().getTime() < this.getDate().getTime()) {
            return -1;
        }
        return -1;
    }
}
