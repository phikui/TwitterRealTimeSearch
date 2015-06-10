package iocontroller.preprocessor;

import iocontroller.QueueContainer;
import model.TransportObject;

import java.util.Queue;
import java.util.concurrent.*;

import static java.lang.Runtime.getRuntime;

/**
 * Created by phil on 16.05.15.
 */
public class PreprocessorMainThread extends Thread {

    private final ExecutorService preprocessors;
    private final Queue<PreprocessorRawObject> incomingQueue;
    private final Queue<Future<TransportObject>> outputQueue;
    private volatile boolean isTerminated = false;

    public PreprocessorMainThread(QueueContainer queueContainer, int num_preprocessors) {
        preprocessors = Executors.newFixedThreadPool(num_preprocessors);
        incomingQueue = queueContainer.getPreProcessorQueue();
        outputQueue = queueContainer.getWriterQueue();
    }


    //This will initialize a dynamically growing Thread pool
    public PreprocessorMainThread(QueueContainer queueContainer, int maxPrepreprocessors, int timeOutInSeconds) {
        ThreadPoolExecutor x = new ThreadPoolExecutor(0, maxPrepreprocessors, timeOutInSeconds, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        x.allowCoreThreadTimeOut(true);
        preprocessors = x;
        incomingQueue = queueContainer.getPreProcessorQueue();
        outputQueue = queueContainer.getWriterQueue();
    }

    public PreprocessorMainThread(QueueContainer queueContainer) {
        preprocessors = Executors.newFixedThreadPool(getRuntime().availableProcessors());
        incomingQueue = queueContainer.getPreProcessorQueue();
        outputQueue = queueContainer.getWriterQueue();

    }

    public void terminate() {
        isTerminated = true;
    }


    public void run() {
        System.out.println("Preprocessor has started");
        while (!isTerminated) { //TODO use take from blocking queue
            if (!incomingQueue.isEmpty()) {
                PreprocessorRawObject next = incomingQueue.remove();
                Future<TransportObject> output = preprocessors.submit(next);
                outputQueue.add(output);
            } else {
                //When incoming queue empty wait a bit
                //System.out.println("Incoming queue empty");
                try {
                    //TODO check if incoming queue gets empty
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Preprocessor has stopped");
    }
}
