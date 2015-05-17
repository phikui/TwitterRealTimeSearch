package iocontroller;

import model.TweetObject;

import java.util.Date;

import static java.lang.Runtime.getRuntime;

/**
 * Created by phil on 17.05.15.
 */
public class IOController {
    private final PreprocessorMainThread preProcessor;
    private final WriterMainThread writer;
    private final QueueObserver queueObserver;


    public IOController(int numPreProcessors, int numQueryProcessors, boolean writerOutput) {
        preProcessor = new PreprocessorMainThread(numPreProcessors);
        writer = new WriterMainThread(writerOutput);
        queueObserver = new QueueObserver();
        Stemmer.init();
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
        QueueContainer.getRawObjectQueue().add(newRaw);
    }

    public void addQuery(String queryString, int k, Date timestamp) {
        PreprocessorRawObject newRaw = new PreprocessorRawObject(queryString, k, timestamp);
        this.addRawObject(newRaw);
    }

    public void addTweet(TweetObject tweet) {
        PreprocessorRawObject newRaw = new PreprocessorRawObject(tweet);
        this.addRawObject(newRaw);
    }
}
