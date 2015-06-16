package utilities;

import indices.postinglists.*;
import model.ConfigurationObject;
import model.TweetObject;

import java.util.*;

/**
 * Created by chans on 5/15/15.
 */
public class HelperFunctions {

    private final static float ONE_HOUR = 3600000;
    private final static float THREE_HOURS = 10800000;
    private final static float SIX_HOURS = 21600000;
    private final static float TWELVE_HOURS = 43200000;
    private final static float ONE_DAY = 86400000;
    private final static float TWO_DAYS = 172800000;
    private final static float THREE_DAYS = 259200000;
    private final static float ONE_WEEK = 604800000;

    private final static float MAX_FOLLOWER = 10000000;


    /**
     * use MAX_FOLLOWER to determine 1.0 score / granularity
     * everyone over MAX_FOLLOWER will have 1.0 score, should be improved
     * for now to work with a value in interval [0,1]
     *
     * @param tweetObject
     * @return
     */
    public static float calculateSignificance(TweetObject tweetObject) {

        return (tweetObject.getNumberOfAuthorFollowers() / MAX_FOLLOWER);
    }

    /**
     * calculate the term similarity based on cosine similarity
     * create word count vectors usable in cosine similarity function
     *
     * @param termIDs1
     * @param termIDs2
     * @return
     */
    public static float calculateTermSimilarity(List<Integer> termIDs1, List<Integer> termIDs2) {
        // get all terms without duplicates
        List<Integer> allTermIDs = concatenateWithoutDuplicates(termIDs1, termIDs2);

        List<Integer> vectorTextA = new ArrayList<Integer>();
        List<Integer> vectorTextB = new ArrayList<Integer>();
        int countWordA = 0;
        int countWordB = 0;

        // create vectors A and B which count occurrences of each word
        for (int termID : allTermIDs) {
            // set count for vectorA
            for (int i = 0; i < termIDs1.size(); i++) {
                if (termIDs1.get(i).equals(termID)) {
                    countWordA++;
                }
            }
            vectorTextA.add(countWordA);
            countWordA = 0;

            // set count for vectorB
            for (int j = 0; j < termIDs2.size(); j++) {
                if (termIDs2.get(j).equals(termID)) {
                    countWordB++;
                }
            }
            vectorTextB.add(countWordB);
            countWordB = 0;
        }

        float cosineSim = calculateCosineSimilarity(vectorTextA, vectorTextB);

        return cosineSim;
    }

    public static List<Integer> concatenateWithoutDuplicates(List listA, List listB) {
        List tempList = new ArrayList();
        tempList.addAll(listA);
        tempList.addAll(listB);
        List<Integer> listC = new ArrayList<Integer>(new LinkedHashSet<Integer>(tempList));
        return listC;
    }

    private static float calculateCosineSimilarity(List<Integer> vectorA, List<Integer> vectorB) {
        float dotProduct = 0;
        float normA = 0;
        float normB = 0;

        // cosine similarity formula
        for (int i = 0; i < vectorA.size(); i++) {
            dotProduct += vectorA.get(i) * vectorB.get(i);
            normA += Math.pow(vectorA.get(i), 2);
            normB += Math.pow(vectorB.get(i), 2);
        }

        return dotProduct / ((float) Math.sqrt(normA) * (float) Math.sqrt(normB));
    }

    /**
     * Use milliseconds for determining freshness:
     * 3 600 000 =  1 hour
     * 10 800 000 =  3 hours
     * 21 600 000 =  6 hours
     * 43 200 000 = 12 hours
     * 86 400 000 = 1 day
     * 172 800 000 = 2 days
     * 259 200 000 = 3 days
     * 604 800 000 = 1 week
     * <p>
     * freshness based on used granularity given in the constant
     *
     * @param timestampData
     * @param timestampQuery
     * @return
     */
    public static float calculateFreshness(Date timestampData, Date timestampQuery) {
        float difference = timestampData.getTime() - timestampQuery.getTime();
        float constant = 1 / ONE_HOUR;
        float freshness = (float) Math.pow(Math.E, (double) (constant * difference));

        return freshness;
    }

    /**
     * calculates the ranking function f using same weights of 1/3 to get values in range [0;1]
     *
     * @param freshness
     * @param significance
     * @param similarity
     * @return
     */
    public static float calculateRankingFunction(float freshness, float significance, float similarity) {
        float f;
        float weight_fresh = ConfigurationObject.getwFreshness();
        float weight_sig = ConfigurationObject.getwSignificance();
        float weight_sim = ConfigurationObject.getwSimilarity();

        f = weight_fresh * freshness + weight_sig * significance + weight_sim * similarity;

        return f;
    }


    public static ITriplePostingList mergeTriplePostingLists(ITriplePostingList listA, ITriplePostingList listB, int termID) {
        ITriplePostingList resultList = new TriplePostingList(termID);

        resultList.setFreshnessPostingList(mergeSinglePostingLists(listA.getFreshnessPostingList(), listB.getFreshnessPostingList()));
        resultList.setSignificancePostingList(mergeSinglePostingLists(listA.getSignificancePostingList(), listB.getSignificancePostingList()));
        resultList.setTermSimilarityPostingList(mergeSinglePostingLists(listA.getTermSimilarityPostingList(), listB.getTermSimilarityPostingList()));

        return resultList;
    }


    public static IPostingList mergeSinglePostingLists(IPostingList listA, IPostingList listB) {
        IPostingList resultList = new PostingList();

        //System.out.println("List A: " + listA);
        //System.out.println("List B: " + listB);

        int i = 0;
        int j = 0;

        while (i < listA.size() && j < listB.size()) {

            if (listA.get(i).getSortKey() >= listB.get(j).getSortKey()) {
                resultList.insertSorted(listA.get(i).getTweetID(), listA.get(i).getSortKey());
                i++;
            } else {
                resultList.insertSorted(listB.get(j).getTweetID(), listB.get(j).getSortKey());
                j++;
            }
        }

        while (i < listA.size()) {
            resultList.insertSorted(listA.get(i).getTweetID(), listA.get(i).getSortKey());
            i++;
        }
        while (j < listB.size()) {
            resultList.insertSorted(listB.get(j).getTweetID(), listB.get(j).getSortKey());
            j++;
        }

        //System.out.println(resultList);
        return resultList;
    }

}
