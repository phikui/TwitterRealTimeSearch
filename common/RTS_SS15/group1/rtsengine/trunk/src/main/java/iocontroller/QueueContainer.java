package iocontroller;

import model.QueryReturnObject;
import model.TransportObject;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by phil on 16.05.15.
 */
public class QueueContainer {
    private final BlockingQueue<Future<TransportObject>> writerQueue;
    private final BlockingQueue<Future<QueryReturnObject>> queryOutputQueue;

    public QueueContainer() {
        writerQueue = new LinkedBlockingQueue<>();
        queryOutputQueue = new LinkedBlockingQueue<>();
    }

    public BlockingQueue<Future<TransportObject>> getWriterQueue() {
        return writerQueue;
    }


    public BlockingQueue<Future<QueryReturnObject>> getQueryOutputQueue() {
        return queryOutputQueue;
    }

}
