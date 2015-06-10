package iocontroller.preprocessor;

import iocontroller.QueueContainer;
import model.TransportObject;

import java.util.concurrent.*;

import static java.lang.Runtime.getRuntime;

/**
 * Created by phil on 16.05.15.
 */
public class PreprocessorMainThread {

    private final ExecutorService preprocessors;
    private final BlockingQueue<Future<TransportObject>> outputQueue;
    private volatile boolean isTerminated = false;

    public PreprocessorMainThread(QueueContainer queueContainer, int num_preprocessors) {
        preprocessors = Executors.newFixedThreadPool(num_preprocessors);
        outputQueue = queueContainer.getWriterQueue();
    }


    //This will initialize a dynamically growing Thread pool
    public PreprocessorMainThread(QueueContainer queueContainer, int maxPrepreprocessors, int timeOutInSeconds) {
        ThreadPoolExecutor x = new ThreadPoolExecutor(0, maxPrepreprocessors, timeOutInSeconds, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        x.allowCoreThreadTimeOut(true);
        preprocessors = x;
        outputQueue = queueContainer.getWriterQueue();
    }

    public PreprocessorMainThread(QueueContainer queueContainer) {
        preprocessors = Executors.newFixedThreadPool(getRuntime().availableProcessors());
        outputQueue = queueContainer.getWriterQueue();

    }

    public void addElement(PreprocessorRawObject e) {
        Future<TransportObject> output = preprocessors.submit(e);
        outputQueue.add(output);
    }

    public void terminate() {
        isTerminated = true;
    }


}
