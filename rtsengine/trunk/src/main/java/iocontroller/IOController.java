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

    public IOController(int numPreProcessors, int numQueryProcessors, boolean writerOutput) {
        queueContainer = new QueueContainer();
        preProcessor = new PreprocessorMainThread(queueContainer, numPreProcessors);
        writer = new WriterMainThread(queueContainer, writerOutput);
        queryProcessor = new QueryProcessorMainThread(numQueryProcessors, queueContainer.getQueryOutputQueue());
        writer.setQueryProcessor(queryProcessor);
        guiThread = new OutputToGUIThread(this);
        tweetcollector = new TweetCollector(this);
    }

    public IOController(int numPreProcessors, int numQueryProcessors) {
        this(numPreProcessors, numQueryProcessors, false);
    }

    public IOController() {
        this(getRuntime().availableProcessors(), getRuntime().availableProcessors(), false);
    }

    public void startAll() {
        preProcessor.start();
        writer.start();
        guiThread.start();
    }



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

    public void waitForTermination() throws InterruptedException {
        preProcessor.join();
        writer.join();
        guiThread.join();
    }

    private void addRawObject(PreprocessorRawObject newRaw) {
        queueContainer.getPreProcessorQueue().add(newRaw);
    }

    public void addTransportObject(TransportObject transportObject) {
        PreprocessorRawObject rawObject = new PreprocessorRawObject(transportObject);
        this.addRawObject(rawObject);
    }

    public boolean hasUnprocessedItems() {
        return !(queueContainer.getPreProcessorQueue().isEmpty() && queueContainer.getWriterQueue().isEmpty());
    }

    public int numUnprocessedItems() {
        return queueContainer.getWriterQueue().size() + queueContainer.getPreProcessorQueue().size() + queueContainer.getQueryOutputQueue().size();
    }

    public int sizePreProcessorQueue() {
        return queueContainer.getPreProcessorQueue().size();
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
        return queueContainer.getQueryOutputQueue().poll().get();
    }


}
