package iocontroller;

import model.TransportObject;

import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by phil on 16.05.15.
 */
public class QueueContainer {
    private static final Queue<PreprocessorRawObject> preProcessorQueue = new LinkedBlockingQueue<PreprocessorRawObject>();
    private static final Queue<Future<TransportObject>> writerQueue = new LinkedBlockingQueue<Future<TransportObject>>();

    public static Queue<Future<TransportObject>> getWriterQueue() {
        return writerQueue;
    }

    public static Queue<PreprocessorRawObject> getPreProcessorQueue() {
        return preProcessorQueue;
    }
}
