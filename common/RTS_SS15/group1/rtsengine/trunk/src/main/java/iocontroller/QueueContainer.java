package iocontroller;

import iocontroller.preprocessor.PreprocessorRawObject;
import model.QueryReturnObject;
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
    private final Queue<Future<QueryReturnObject>> queryOutputQueue;

    public QueueContainer() {
        preProcessorQueue = new LinkedBlockingQueue<>();
        writerQueue = new LinkedBlockingQueue<>();
        queryOutputQueue = new LinkedBlockingQueue<>();
    }

    public Queue<Future<TransportObject>> getWriterQueue() {
        return writerQueue;
    }

    public Queue<PreprocessorRawObject> getPreProcessorQueue() {
        return preProcessorQueue;
    }

    public Queue<Future<QueryReturnObject>> getQueryOutputQueue() {
        return queryOutputQueue;
    }

}
