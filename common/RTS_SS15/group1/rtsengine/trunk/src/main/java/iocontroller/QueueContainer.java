package iocontroller;

import model.TransportObject;

import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by phil on 16.05.15.
 */
public class QueueContainer {
    private final Queue<PreprocessorRawObject> preProcessorQueue;
    private final Queue<Future<TransportObject>> writerQueue;

    public QueueContainer() {
        preProcessorQueue = new LinkedBlockingQueue<PreprocessorRawObject>();
        writerQueue = new LinkedBlockingQueue<Future<TransportObject>>();
    }

    public Queue<Future<TransportObject>> getWriterQueue() {
        return writerQueue;
    }

    public Queue<PreprocessorRawObject> getPreProcessorQueue() {
        return preProcessorQueue;
    }
}
