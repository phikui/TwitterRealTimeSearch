package iocontroller;

import model.TransportObject;

import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.lang.Runtime.getRuntime;

/**
 * Created by phil on 16.05.15.
 */
public class PreprocessingMainThread extends Thread {

    private final ExecutorService preprocessors;
    private volatile boolean isTerminated = false;
    private Queue<PreprocessingRawObject> incomingQueue = QueueContainer.getRawObjectQueue();
    private Queue<Future<TransportObject>> outputQueue = QueueContainer.getPreprocessedOutput();

    public PreprocessingMainThread(int num_preprocessors) {
        preprocessors = Executors.newFixedThreadPool(num_preprocessors);
    }

    public PreprocessingMainThread() {
        preprocessors = Executors.newFixedThreadPool(getRuntime().availableProcessors());

    }

    public void terminate() {
        isTerminated = true;
    }


    public void run() {
        while (!isTerminated) {
            if (!incomingQueue.isEmpty()) {
                PreprocessingRawObject next = incomingQueue.remove();
                Future<TransportObject> output = preprocessors.submit(next);
                outputQueue.add(output);
            }
        }

    }
}
