package iocontroller;

import iocontroller.preprocessor.Preprocessor;
import iocontroller.preprocessor.PreprocessorRawObject;
import iocontroller.preprocessor.Stemmer;
import iocontroller.queryprocessor.QueryProcessor;
import iocontroller.writer.Writer;
import model.QueryReturnObject;
import model.TransportObject;
import model.TweetDictionary;
import model.TweetObject;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static java.lang.Runtime.getRuntime;

/**
 * Created by phil on 17.05.15.
 *
 *
 * This is the main communication tool. It handles arriving tweets and queries and does
 * the necessary calculations and insertions
 *
 */
public class IOController {

    public static final ThreadLocal<Stemmer> stemmer = new ThreadLocal<Stemmer>() {
        @Override
        public Stemmer initialValue() {
            return new Stemmer();
        }
    };
    public static boolean useStandfordStemmer = true;
    //protected static final Stemmer stemmer = new Stemmer();
    protected final QueueContainer queueContainer;
    private final Preprocessor preProcessor;
    private final Writer writer;
    private final QueryProcessor queryProcessor;
    private final OutputToGUIThread guiThread;
    private final TweetCollector tweetcollector;

    private Boolean message = false;


    /**
     * Instantiates an IOController object
     *
     * @param numPreProcessors   The number of preprocessor threads to use
     * @param numQueryProcessors The number of queryprocessor threads to use
     * @param writerOutput       Toggle for debug printouts
     */
    public IOController(int numPreProcessors, int numQueryProcessors, boolean writerOutput) {
        queueContainer = new QueueContainer();
        preProcessor = new Preprocessor(queueContainer, numPreProcessors);
        writer = new Writer(queueContainer, writerOutput);
        queryProcessor = new QueryProcessor(numQueryProcessors, queueContainer.getQueryOutputQueue());
        writer.setQueryProcessor(queryProcessor);
        guiThread = new OutputToGUIThread(this);
        tweetcollector = new TweetCollector(this);

        //set threads to deamon
        writer.setDaemon(true);
        guiThread.setDaemon(true);
    }

    /**
     * Instantiates an IOController object
     *
     * @param numPreProcessors The number of preprocessor threads to use
     * @param numQueryProcessors The number of queryprocessor threads to use
     */
    public IOController(int numPreProcessors, int numQueryProcessors) {
        this(numPreProcessors, numQueryProcessors, false);
    }

    /**
     * Instantiates an IOController object using the default values of cores of machine preprocessors
     * and 1 queryprocessor and sets the debug outputs to false
     *
     *
     */
    public IOController() {
        this(getRuntime().availableProcessors(), 1, false);
    }

    public IOController(boolean useStandfordStemmer) {
        this(getRuntime().availableProcessors(), 1, false);
        IOController.useStandfordStemmer = useStandfordStemmer;
    }

    public static boolean isUseStandfordStemmer() {
        return useStandfordStemmer;
    }

    public static void setUseStandfordStemmer(boolean useStandfordStemmer) {
        IOController.useStandfordStemmer = useStandfordStemmer;
    }

    /**
     * Start all threads
     */
    public void startAll() {
        writer.start();
        guiThread.start();
    }

    /**
     * Stop all running threads, it will finish all current preprocessing/writing and queryprocessing
     */
    public void stopAll() {
        if (this.hasUnprocessedItems()) {
            System.out.println("Warning: there are unprocessed items");
        }
        writer.terminate();
        guiThread.terminate();
        tweetcollector.stopCollecting();
    }

    public void collectTweets() {
        tweetcollector.startCollecting();
    }

    public void stopcollectingTweets() {
        tweetcollector.stopCollecting();
    }

    public void stopWriter() {
        writer.terminate();
    }

    /**
     * Calls the join method of associated threads
     */
    public void waitForTermination() throws InterruptedException {
        writer.join();
        guiThread.join();
    }

    /**
     * Add a transport object to the preprocessor queue. Can be tweet or query
     *
     * @param transportObject The transport object
     */
    public void addTransportObject(TransportObject transportObject) {
        PreprocessorRawObject rawObject = new PreprocessorRawObject(transportObject);
        preProcessor.addElement(rawObject);
    }

    public boolean hasUnprocessedItems() {
        return !(queueContainer.getWriterQueue().isEmpty());
    }

    public int numUnprocessedItems() {
        return queueContainer.getWriterQueue().size() + queueContainer.getQueryOutputQueue().size();
    }

    public int sizeWriterQueue() {
        return queueContainer.getWriterQueue().size();
    }

    public int sizeQueryQueue() {
        return queueContainer.getQueryOutputQueue().size();
    }

    public Queue<Future<QueryReturnObject>> getOutputQueue() {
        return queueContainer.getQueryOutputQueue();
    }

    public boolean hasNextOutputElement() {
        return queueContainer.getQueryOutputQueue().size() > 0;
    }

    public QueryReturnObject getNextOutputElement() throws ExecutionException, InterruptedException {
        return queueContainer.getQueryOutputQueue().take().get();
    }
    public synchronized void putMessage(Boolean bool){
        message = bool;
        notify();
    }
    public synchronized Boolean getMessage() throws InterruptedException{
        notify();
        while(!message){
            wait();
        }
        return message;
    }


    public void dumpTweetDictionaryToDatabase(String filename) {
        DB mapDB = DBMaker.newFileDB(new File(filename))
                .closeOnJvmShutdown()
                .make();


        HTreeMap<Integer, TweetObject> tweetObjectsMapDB = mapDB.getHashMap("tweetObjects");

        for (int index : TweetDictionary.getTweetDictionary().keySet()) {
            TweetObject tweet = TweetDictionary.getTweetDictionary().get(index).getTweetObject();
            tweetObjectsMapDB.put(index, tweet);
        }
        mapDB.commit();
        mapDB.compact(); //make file smaller
        mapDB.close();

    }

    public void loadTweetDictionaryFromDatabase(String filename) {
        DB mapDB = DBMaker.newFileDB(new File(filename))
                .closeOnJvmShutdown()
                .make();


        HTreeMap<Integer, TweetObject> tweetObjectsMapDB = mapDB.getHashMap("tweetObjects");

        //insert into index
        for (int index : tweetObjectsMapDB.keySet()) {
            TweetObject currentTweetObject = tweetObjectsMapDB.get(index);
            this.addTransportObject(new TransportObject(currentTweetObject));
        }
        mapDB.close();

    }

}
