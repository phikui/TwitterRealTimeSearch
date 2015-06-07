package model;

/**
 * Created by smea on 15/5/15.
 */
public class ConfigurationObject {

    private static float wSignificance = ((float)1/3);
    private static float wSimilarity = ((float)1/3);
    private static float wFreshness = ((float)1/3);

    private static int ratio;
    private static int numberOfThreads;
    private static int numberOfTweets;

    private static boolean stream;
    private static Index indexType;

    /**
     * Getter and Setter
     */
    public static int getNumberOfTweets() {
        return ConfigurationObject.numberOfTweets;
    }

    public static void setNumberOfTweets(int k) {
        ConfigurationObject.numberOfTweets = k;
    }

    public static float getwSignificance() {
        return ConfigurationObject.wSignificance;
    }

    public static void setwSignificance(float wSignificance) {
        ConfigurationObject.wSignificance = wSignificance;
    }

    public static float getwSimilarity() {
        return ConfigurationObject.wSimilarity;
    }

    public static void setwSimilarity(float wSimilarity) {
        ConfigurationObject.wSimilarity = wSimilarity;
    }

    public static float getwFreshness() {
        return ConfigurationObject.wFreshness;
    }

    public static void setwFreshness(float wFreshness) {
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

    public static boolean isStream() {
        return ConfigurationObject.stream;
    }

    public static void setStream(boolean stream) {
        ConfigurationObject.stream = stream;
    }

    public static Index getIndexType() {
        return ConfigurationObject.indexType;
    }

    @Deprecated
    public static void setIndexType(String index) {
        switch (index) {
            case "AO":
                setIndexType(Index.APPEND_ONLY);
                break;
            case "TPL":
                setIndexType(Index.TRIPLE_POSTING_LIST);
                break;
            case "LSII":
                setIndexType(Index.LSII);
                break;
            default:
                setIndexType(Index.APPEND_ONLY);
                break;
        }

    }

    public static void setIndexType(Index index) {
        ConfigurationObject.indexType = index;
    }

    public enum Index {
        APPEND_ONLY, TRIPLE_POSTING_LIST, LSII
    }

}
