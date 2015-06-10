package iocontroller;

import iocontroller.preprocessor.PreprocessorRawObject;
import model.QueryReturnObject;
import model.TransportObject;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by phil on 16.05.15.
 */
public class QueueContainer {
    private final BlockingQueue<PreprocessorRawObject> preProcessorQueue;
    private final BlockingQueue<Future<TransportObject>> writerQueue;
    private final BlockingQueue<Future<QueryReturnObject>> queryOutputQueue;

    public QueueContainer() {
        preProcessorQueue = new LinkedBlockingQueue<>();
        writerQueue = new LinkedBlockingQueue<>();
        queryOutputQueue = new LinkedBlockingQueue<>();
    }

    public BlockingQueue<Future<TransportObject>> getWriterQueue() {
        return writerQueue;
    }

    public BlockingQueue<PreprocessorRawObject> getPreProcessorQueue() {
        return preProcessorQueue;
    }

    public BlockingQueue<Future<QueryReturnObject>> getQueryOutputQueue() {
        return queryOutputQueue;
    }

}
