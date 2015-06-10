package iocontroller.preprocessor;

import iocontroller.QueueContainer;
import model.TransportObject;

import java.util.concurrent.*;

import static java.lang.Runtime.getRuntime;

/**
 * Created by phil on 16.05.15.
 */
public class Preprocessor {

    private final ExecutorService preprocessors;
    private final BlockingQueue<Future<TransportObject>> outputQueue;

    public Preprocessor(QueueContainer queueContainer, int num_preprocessors) {
        preprocessors = Executors.newFixedThreadPool(num_preprocessors);
        outputQueue = queueContainer.getWriterQueue();
    }


    //This will initialize a dynamically growing Thread pool
    public Preprocessor(QueueContainer queueContainer, int maxPrepreprocessors, int timeOutInSeconds) {
        ThreadPoolExecutor x = new ThreadPoolExecutor(0, maxPrepreprocessors, timeOutInSeconds, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        x.allowCoreThreadTimeOut(true);
        preprocessors = x;
        outputQueue = queueContainer.getWriterQueue();
    }

    public Preprocessor(QueueContainer queueContainer) {
        preprocessors = Executors.newFixedThreadPool(getRuntime().availableProcessors());
        outputQueue = queueContainer.getWriterQueue();

    }

    public void addElement(PreprocessorRawObject e) {
        Future<TransportObject> output = preprocessors.submit(e);
        outputQueue.add(output);
    }


}
