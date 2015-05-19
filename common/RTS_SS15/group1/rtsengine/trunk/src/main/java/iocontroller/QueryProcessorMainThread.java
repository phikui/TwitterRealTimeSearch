package iocontroller;

import model.TransportObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by phil on 16.05.15.
 */
public class QueryProcessorMainThread {

    //The writer will dispatch queries to here

    private static ExecutorService queryprocessors;


    public QueryProcessorMainThread(int numQueryProcessors) {
        queryprocessors = Executors.newFixedThreadPool(numQueryProcessors);
    }

    public QueryProcessorMainThread() {
        queryprocessors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public static void scheduleQuery(TransportObject query) throws Exception {
        if (query.isQuery()) {
            //TODO
        } else {
            throw new Exception("Query expected");
        }
    }
}


