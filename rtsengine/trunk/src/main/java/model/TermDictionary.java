package model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maps terms to their termID
 */
public class TermDictionary {

    private static int termIDCounter;

    private static Map<String, Integer> termDictionary;

    static {
        termIDCounter = 0;
        termDictionary = new ConcurrentHashMap<String, Integer>();
    }

    /**
     * Returns the stored termID for the given term
     *
     * @param   String  term
     *
     * @return  int
     */
    public static int getTermID(String term) {
        return termDictionary.get(term);
    }

    /**
     * Inserts a new term into the dictionary.
     * A termID is chosen automatically and returned.
     *
     * Doesn't insert a term if it is already in the dictionary,
     * but just returns the already stored termID.
     *
     * @param String term
     *
     * @return termID
     */
    public static int insertTerm(String term) {
        // Check whether term is already stored and just
        // return stored termID
        Integer storedTermID = termDictionary.get(term);
        if (storedTermID != null) {
            return storedTermID;
        }

        // Get new termID and insert tweet
        int termID = termIDCounter;
        termIDCounter++;
        termDictionary.put(term, termID);

        return termID;
    }

}