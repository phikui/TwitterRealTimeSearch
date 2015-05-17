package iocontroller;

import model.PreprocessingRawObject;
import model.TransportObject;

import java.util.Queue;
import java.util.concurrent.*;

import static java.lang.Runtime.getRuntime;

/**
 * Created by phil on 16.05.15.
 */
public class PreprocessingMainThread extends Thread {

    private final ExecutorService preprocessors;
    private final Queue<PreprocessingRawObject> incomingQueue = QueueContainer.getRawObjectQueue();
    private final Queue<Future<TransportObject>> outputQueue = QueueContainer.getPreprocessedOutput();
    private volatile boolean isTerminated = false;

    public PreprocessingMainThread(int num_preprocessors) {
        preprocessors = Executors.newFixedThreadPool(num_preprocessors);
    }


    //This will initialize a dynamically growing Thread pool
    public PreprocessingMainThread(int maxPrepreprocessors, int timeOutInSeconds) {
        ThreadPoolExecutor x = new ThreadPoolExecutor(0, maxPrepreprocessors, timeOutInSeconds, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        x.allowCoreThreadTimeOut(true);
        preprocessors = x;
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
