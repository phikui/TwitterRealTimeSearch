package iocontroller;

import model.PreprocessingRawObject;
import model.TransportObject;

import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by phil on 16.05.15.
 */
public class QueueContainer {
    private static Queue<PreprocessingRawObject> rawObjectQueue = new LinkedBlockingQueue<PreprocessingRawObject>();
    private static Queue<Future<TransportObject>> preprocessedOutput = new LinkedBlockingQueue<Future<TransportObject>>();

    public static Queue<Future<TransportObject>> getPreprocessedOutput() {
        return preprocessedOutput;
    }

    public static Queue<PreprocessingRawObject> getRawObjectQueue() {
        return rawObjectQueue;
    }
}
