package gui;

/**
 * Created by smea on 15/5/15.
 */
public class ConfigurationObject {

    private int wSignificance, wSimilarity, wFreshness;
    private int ratio;
    private int threads;
    private int k;
    private boolean streamQueries;
    private boolean streamTweets;
    private String index;

    public ConfigurationObject() {}

    /**
     * Getter and Setter
     */
    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }
    public int getwSignificance() {
        return wSignificance;
    }

    public void setwSignificance(int wSignificance) {
        this.wSignificance = wSignificance;
    }

    public int getwSimilarity() {
        return wSimilarity;
    }

    public void setwSimilarity(int wSimilarity) {
        this.wSimilarity = wSimilarity;
    }

    public int getwFreshness() {
        return wFreshness;
    }

    public void setwFreshness(int wFreshness) {
        this.wFreshness = wFreshness;
    }

    public int getRatio() {
        return ratio;
    }

    public void setRatio(int ratio) {
        this.ratio = ratio;
    }

    public int getThreads() {
        return threads;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public boolean isStreamQueries() {
        return streamQueries;
    }

    public void setStreamQueries(boolean streamQueries) {
        this.streamQueries = streamQueries;
    }

    public boolean isStreamTweets() {
        return streamTweets;
    }

    public void setStreamTweets(boolean streamTweets) {
        this.streamTweets = streamTweets;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

}
