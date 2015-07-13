package utilities;

import iocontroller.preprocessor.PreprocessorRawObject;
import model.TermDictionary;
import model.TransportObject;
import model.TweetDictionary;
import model.TweetObject;
import twitter4j.GeoLocation;
import twitter4j.Place;

import java.util.*;

/**
 * Generates random words, sentences, TweetObjects and
 * TransportObjects ready for insertion into an index.
 */
public class RandomObjectFactory {

    private int numberOfWordsInRandomWordList = 25;

    private Random random;

    // List of  random words generated in constructor,
    // used to generate random sentences.
    // See numberOfWordsInRandomWordList below.
    private List<String> randomWordList;

    public RandomObjectFactory() {
        this.random = new Random();

        this.randomWordList = new ArrayList<String>(numberOfWordsInRandomWordList);

        // Generate random words
//        for (int i = 0; i < this.numberOfWordsInRandomWordList; i++) {
//            this.randomWordList.add(this.generateRandomWord());
//        }

        // Don't generate random words but use pre-defined list
        randomWordList.add("Aachen");
        randomWordList.add("real");
        randomWordList.add("time");
        randomWordList.add("Miami");
        randomWordList.add("Sun");
        randomWordList.add("Beach");
        randomWordList.add("Vacation");
        randomWordList.add("Aircraft");
        randomWordList.add("Wings");
        randomWordList.add("of");
        randomWordList.add("not");
        randomWordList.add("a");
        randomWordList.add("back");
        randomWordList.add("before");
        randomWordList.add("next");
        randomWordList.add("I");
        randomWordList.add("you");
        randomWordList.add("we");
        randomWordList.add("are");
        randomWordList.add("am");
        randomWordList.add("pool");
        randomWordList.add("swimming");
        randomWordList.add("Florida");
        randomWordList.add("Disney");
        randomWordList.add("World");
    }

    public PreprocessorRawObject generateRandomRawObjectReadyForPreprocessing() {
        TransportObject transportObject = this.generateRandomTransportObjectReadyForPreprocessing();
        return new PreprocessorRawObject(transportObject);
    }

    public TransportObject generateRandomTransportObjectReadyForPreprocessing() {
        TweetObject tweetObject = this.generateRandomTweet();
        return new TransportObject(tweetObject);
    }

    public TransportObject generateRandomTransportObjectReadyForWriting() {
        TransportObject transportObject = this.generateRandomTransportObjectReadyForPreprocessing();
        this.preprocessTransportObjectDummy(transportObject);
        return transportObject;
    }

    public TransportObject generateRandomTransportObjectReadyForIndexInsertion() {
        TransportObject transportObject = this.generateRandomTransportObjectReadyForWriting();
        this.writeTransportObjectDummy(transportObject);
        return transportObject;
    }

    /**
     * Simple dummy preprocessing function for testing purposes
     *
     * @param transportObject
     */
    public void preprocessTransportObjectDummy(TransportObject transportObject) {
        String text = transportObject.getText();
        List<String> terms = new LinkedList<String>();

        StringTokenizer stringTokenizer = new StringTokenizer(text);
        while (stringTokenizer.hasMoreTokens()) {
            String token = stringTokenizer.nextToken();
            terms.add(token);
        }

        transportObject.setTerms(terms);
    }

    public void writeTransportObjectDummy(TransportObject transportObject) {
        List<String> terms = transportObject.getTerms();

        // Write tweetObject for non-queries
        if (!transportObject.isQuery()) {
            int tweetID = TweetDictionary.insertTransportObject(transportObject);
            transportObject.setTweetID(tweetID);
        }

        // Write terms
        List<Integer> termIDs = new LinkedList<Integer>();

        for (String term: terms) {
            int termID = TermDictionary.insertTerm(term);
            termIDs.add(termID);
        }

        transportObject.setTermIDs(termIDs);
    }

    public TweetObject generateRandomTweet() {
        String username = this.generateRandomWord(15);
        String text = this.generateRandomSentence(10) + " testterm";
        GeoLocation geoLocation = new GeoLocation(50, 55);
        Place place = null; // TODO
        Date timestamp = new Date();
        float numberOfAuthorFollowers = this.random.nextInt(250);

        return new TweetObject(username, text, geoLocation, place, timestamp, numberOfAuthorFollowers, false);
    }

    /**
     * Returns a random sentence with a maximum amount of
     * random words (separated by space character).
     *
     * Sentences are generated based on randomWordList.
     *
     * @param numberOfWords
     * @return
     */
    public String generateRandomSentence(int maximumNumberOfWords) {
        int numberOfWords = this.random.nextInt(maximumNumberOfWords - 1) + 1;

        String randomSentence = "";

        for (int i = 0; i < numberOfWords; i++) {
            int nextRandomWordIndex = random.nextInt(this.numberOfWordsInRandomWordList);
            String nextRandomWord = this.randomWordList.get(nextRandomWordIndex);

            String seperator = " ";
            if (i == 0) {
                seperator = "";
            }

            randomSentence += seperator + nextRandomWord;
        }

        return randomSentence;
    }

    /**
     * Returns a random word of a specific maximum length
     *
     * Characters are generated randomly.
     *
     * @param length
     * @return
     */
    public String generateRandomWord(int maximumLength) {
        int length = this.random.nextInt(maximumLength - 1) + 1;

        char[] randomCharArray = new char[length];

        for (int i = 0; i < length; i++) {
            // Generate random number in range [65,122] (ASCII characters a-Z)
            char randomChar = (char) (this.random.nextInt(57) + 65);

            // Random words should not contain space, replace by 'a' character
            if (randomChar == ' ') {
                randomChar = 'a';
            }

            randomCharArray[i] = randomChar;
        }

        return new String(randomCharArray);
    }

}
