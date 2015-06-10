package iocontroller.writer;

import indices.IndexDispatcher;
import iocontroller.QueueContainer;
import iocontroller.queryprocessor.QueryProcessorMainThread;
import model.TermDictionary;
import model.TransportObject;
import model.TweetDictionary;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by phil on 16.05.15.
 */
public class WriterMainThread extends Thread {
    private final boolean output;
    private final BlockingQueue<Future<TransportObject>> incomingQueue;
    private volatile boolean isTerminated = false;
    private QueryProcessorMainThread queryProcessor;

    public WriterMainThread(QueueContainer queueContainer) {
        incomingQueue = queueContainer.getWriterQueue();
        output = false;
    }

    public WriterMainThread(QueueContainer queueContainer, boolean output) {
        incomingQueue = queueContainer.getWriterQueue();
        this.output = output;
    }

    public void setQueryProcessor(QueryProcessorMainThread processor) {
        queryProcessor = processor;
    }

    public void terminate() {
        isTerminated = true;
    }


    public void run() {
        System.out.println("Writer has started");
        while (!isTerminated) {
                try {
                    //Get TransportObject out of the queue
                    TransportObject x;
                    x = incomingQueue.poll(2, TimeUnit.SECONDS).get();

                    //check for time out
                    if (x == null) {
                        continue;
                    }

                    if (x.isQuery()) {
                        //If it is a query dispatch to query processor
                        List<Integer> termIds = new ArrayList<Integer>();
                        for (String term : x.getTerms()) {
                            int id = TermDictionary.insertTerm(term);
                            termIds.add(id);

                        }
                        x.setTermIDs(termIds);
                        queryProcessor.scheduleQuery(x);
                    } else {
                        //Update tweet and termDictionaries
                        int tweet_id = TweetDictionary.insertTransportObject(x);
                        x.setTweetID(tweet_id);
                        List<Integer> termIds = new ArrayList<Integer>();
                        for (String term : x.getTerms()) {
                            int id = TermDictionary.insertTerm(term);
                            termIds.add(id);

                        }
                        x.setTermIDs(termIds);

                        IndexDispatcher.insertTransportObject(x);
                        if (output) {
                            System.out.println("Inserted tweet:");
                            System.out.println(x.getText());
                            System.out.println();
                            System.out.println();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Could not get element from writer queue");
                }


        }
        System.out.println("Writer has stopped");
    }
}
