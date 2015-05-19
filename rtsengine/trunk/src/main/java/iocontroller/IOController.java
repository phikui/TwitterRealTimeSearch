package iocontroller;

import iocontroller.preprocessor.PreprocessorMainThread;
import iocontroller.preprocessor.PreprocessorRawObject;
import iocontroller.preprocessor.Stemmer;
import iocontroller.writer.WriterMainThread;
import model.TweetObject;

import java.util.Date;

import static java.lang.Runtime.getRuntime;

/**
 * Created by phil on 17.05.15.
 */
public class IOController {
    protected final static boolean useStandfordStemmer = true;
    protected static final ThreadLocal<Stemmer> stemmer = new ThreadLocal<Stemmer>() {
        @Override
        public Stemmer initialValue() {
            return new Stemmer();
        }
    };


    //protected static final Stemmer stemmer = new Stemmer();
    protected final QueueContainer queueContainer;
    private final PreprocessorMainThread preProcessor;
    private final WriterMainThread writer;
    private final QueueObserver queueObserver;
    //private final QueryProcessorMainThread queryProcessor;


    public IOController(int numPreProcessors, int numQueryProcessors, boolean writerOutput) {
        queueContainer = new QueueContainer();
        preProcessor = new PreprocessorMainThread(queueContainer, numPreProcessors);
        writer = new WriterMainThread(queueContainer, writerOutput);
        queueObserver = new QueueObserver(queueContainer);
        //queryProcessor = new QueryProcessorMainThread(numQueryProcessors);
    }

    public IOController() {
        this(getRuntime().availableProcessors(), getRuntime().availableProcessors(), false);
    }

    public void startAll() {
        preProcessor.start();
        writer.start();
    }

    public void activateQueueObserver() {
        queueObserver.start();
    }

    public void stopAll() {
        if (this.hasUnprocessedItems()) {
            System.out.println("Warning: there are unprocessed items");
        }
        writer.terminate();
        preProcessor.terminate();
        queueObserver.terminate();
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
        queueObserver.join();
    }

    public void addRawObject(PreprocessorRawObject newRaw) {
        queueContainer.getPreProcessorQueue().add(newRaw);
    }

    public void addQuery(String queryString, int k, Date timestamp) {
        PreprocessorRawObject newRaw = new PreprocessorRawObject(queryString, k, timestamp);
        this.addRawObject(newRaw);
    }

    public void addTweet(TweetObject tweet) {
        PreprocessorRawObject newRaw = new PreprocessorRawObject(tweet);
        this.addRawObject(newRaw);
    }

    public boolean hasUnprocessedItems() {
        return !(queueContainer.getPreProcessorQueue().isEmpty() && queueContainer.getWriterQueue().isEmpty());
    }

    public int numUnprocessedItems() {
        return queueContainer.getWriterQueue().size() + queueContainer.getPreProcessorQueue().size();
    }

    public int sizePreProcessorQueue() {
        return queueContainer.getPreProcessorQueue().size();
    }

    public int sizeWriterQueue() {
        return queueContainer.getWriterQueue().size();
    }
}
