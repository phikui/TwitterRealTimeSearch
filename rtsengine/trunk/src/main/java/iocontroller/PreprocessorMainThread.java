package iocontroller;

import model.TransportObject;

import java.util.Queue;
import java.util.concurrent.*;

import static java.lang.Runtime.getRuntime;

/**
 * Created by phil on 16.05.15.
 */
public class PreprocessorMainThread extends Thread {

    private final ExecutorService preprocessors;
    private final Queue<PreprocessorRawObject> incomingQueue = QueueContainer.getRawObjectQueue();
    private final Queue<Future<TransportObject>> outputQueue = QueueContainer.getPreprocessedOutputQueue();
    private volatile boolean isTerminated = false;

    public PreprocessorMainThread(int num_preprocessors) {
        preprocessors = Executors.newFixedThreadPool(num_preprocessors);
    }


    //This will initialize a dynamically growing Thread pool
    public PreprocessorMainThread(int maxPrepreprocessors, int timeOutInSeconds) {
        ThreadPoolExecutor x = new ThreadPoolExecutor(0, maxPrepreprocessors, timeOutInSeconds, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        x.allowCoreThreadTimeOut(true);
        preprocessors = x;
    }

    public PreprocessorMainThread() {
        preprocessors = Executors.newFixedThreadPool(getRuntime().availableProcessors());

    }

    public void terminate() {
        isTerminated = true;
    }


    public void run() {
        while (!isTerminated) {
            if (!incomingQueue.isEmpty()) {
                PreprocessorRawObject next = incomingQueue.remove();
                Future<TransportObject> output = preprocessors.submit(next);
                outputQueue.add(output);
            } else {
                //When incoming queue empty wait a bit
                //System.out.println("Incoming queue empty");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("preprocessor has stopped");
    }
}
