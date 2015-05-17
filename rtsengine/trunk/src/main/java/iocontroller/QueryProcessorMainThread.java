package iocontroller;

import model.TransportObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Runtime.getRuntime;

/**
 * Created by phil on 16.05.15.
 */
public class QueryProcessorMainThread {

    //The writer will dispatch queries to here

    private static final ExecutorService queryprocessors = Executors.newFixedThreadPool(getRuntime().availableProcessors());


    public static void scheduleQuery(TransportObject query) {
        //TODO
    }
}

