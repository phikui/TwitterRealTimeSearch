package iocontroller.queryprocessor;


import model.QueryReturnObject;
import model.TransportObject;

import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by phil on 16.05.15.
 */
public class QueryProcessorMainThread {

    //The writer will dispatch queries to here

    private final ExecutorService queryProcessors;
    private final Queue<Future<QueryReturnObject>> queryOutputQueue;


    public QueryProcessorMainThread(int numQueryProcessors, Queue<Future<QueryReturnObject>> queryQueue) {
        queryProcessors = Executors.newFixedThreadPool(numQueryProcessors);
        queryOutputQueue = queryQueue;
    }

    public QueryProcessorMainThread(Queue<Future<QueryReturnObject>> queryQueue) {
        queryProcessors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        queryOutputQueue = queryQueue;
    }

    public void scheduleQuery(TransportObject query) throws Exception {
        if (query.isQuery()) {
            QueryWorker newWorker = new QueryWorker(query);
            queryOutputQueue.add(queryProcessors.submit(newWorker));
        } else {
            throw new Exception("Query expected");
        }
    }
}


