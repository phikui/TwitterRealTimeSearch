package iocontroller;

import iocontroller.preprocessor.PreprocessorMainThread;
import iocontroller.preprocessor.PreprocessorRawObject;
import iocontroller.preprocessor.Stemmer;
import iocontroller.queryprocessor.QueryProcessorMainThread;
import iocontroller.writer.WriterMainThread;
import model.QueryReturnObject;
import model.TransportObject;

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
    public final static boolean useStandfordStemmer = true;
    public static final ThreadLocal<Stemmer> stemmer = new ThreadLocal<Stemmer>() {
        @Override
        public Stemmer initialValue() {
            return new Stemmer();
        }
    };


    //protected static final Stemmer stemmer = new Stemmer();
    protected final QueueContainer queueContainer;
    private final PreprocessorMainThread preProcessor;
    private final WriterMainThread writer;
    private final QueryProcessorMainThread queryProcessor;
    private final OutputToGUIThread guiThread;
    private final TweetCollector tweetcollector;

    private String message;


    /**
     * Instantiates an IOController object
     *
     * @param numPreProcessors   The number of preprocessor threads to use
     * @param numQueryProcessors The number of queryprocessor threads to use
     * @param writerOutput       Toggle for debug printouts
     */
    public IOController(int numPreProcessors, int numQueryProcessors, boolean writerOutput) {
        queueContainer = new QueueContainer();
        preProcessor = new PreprocessorMainThread(queueContainer, numPreProcessors);
        writer = new WriterMainThread(queueContainer, writerOutput);
        queryProcessor = new QueryProcessorMainThread(numQueryProcessors, queueContainer.getQueryOutputQueue());
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
        preProcessor.terminate();
        guiThread.terminate();
        tweetcollector.stopCollecting();
    }


    public void collectTweets() {
        tweetcollector.startCollecting();
    }

    public void stopcollectingTweets() {
        tweetcollector.stopCollecting();
    }

    public void stopPreprocessor() {
        preProcessor.terminate();
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

    // Message Passing
   /* public synchronized void putMessage(){
        message = "empty";
        notify();
    }
    public synchronized String getMessage() throws InterruptedException{
        notify();
        while(message.isEmpty()){
            wait();
        }
        String result = message;
        message = "";
        return result;
    }*/

}
