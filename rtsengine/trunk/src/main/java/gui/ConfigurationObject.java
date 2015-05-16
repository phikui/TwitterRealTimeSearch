package gui;

/**
 * Created by smea on 15/5/15.
 */
public class ConfigurationObject {

    private static int wSignificance = (1/3);
    private static int wSimilarity = (1/3);
    private static int wFreshness = (1/3);

    private static int ratio;
    private static int numberOfThreads;
    private static int k;

    private static boolean streamQueries;
    private static boolean streamTweets;
    private static String indexType;


    /**
     * Getter and Setter
     */
    public static int getK() {
        return ConfigurationObject.k;
    }

    public static void setK(int k) {
        ConfigurationObject.k = k;
    }

    public static int getwSignificance() {
        return ConfigurationObject.wSignificance;
    }

    public static void setwSignificance(int wSignificance) {
        ConfigurationObject.wSignificance = wSignificance;
    }

    public static int getwSimilarity() {
        return ConfigurationObject.wSimilarity;
    }

    public static void setwSimilarity(int wSimilarity) {
        ConfigurationObject.wSimilarity = wSimilarity;
    }

    public static int getwFreshness() {
        return ConfigurationObject.wFreshness;
    }

    public static void setwFreshness(int wFreshness) {
        ConfigurationObject.wFreshness = wFreshness;
    }

    public static int getRatio() {
        return ConfigurationObject.ratio;
    }

    public static void setRatio(int ratio) {
        ConfigurationObject.ratio = ratio;
    }

    public static int getNumberOfThreads() {
        return ConfigurationObject.numberOfThreads;
    }

    public static void setNumberOfThreads(int numberOfThreads) {
        ConfigurationObject.numberOfThreads = numberOfThreads;
    }

    public static boolean isStreamQueries() {
        return ConfigurationObject.streamQueries;
    }

    public static void setStreamQueries(boolean streamQueries) {
        ConfigurationObject.streamQueries = streamQueries;
    }

    public static boolean isStreamTweets() {
        return ConfigurationObject.streamTweets;
    }

    public static void setStreamTweets(boolean streamTweets) {
        ConfigurationObject.streamTweets = streamTweets;
    }

    public static String getIndexType() {
        return ConfigurationObject.indexType;
    }

    public static void setIndexType(String index) {
        ConfigurationObject.indexType = indexType;
    }

}
