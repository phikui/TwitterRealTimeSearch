package iocontroller;

import model.TermDictionary;
import model.TransportObject;
import model.TweetDictionary;
import model.TweetObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Future;

/**
 * Created by phil on 16.05.15.
 */
public class WriterMainThread extends Thread {
    private volatile boolean isTerminated = false;

    private Queue<Future<TransportObject>> incomingQueue = QueueContainer.getPreprocessedOutput();


    public void terminate() {
        isTerminated = true;
    }


    public void run() {
        while (!isTerminated) {
            if (!incomingQueue.isEmpty()) {
                try {
                    //Get TransportObject out of the queue
                    TransportObject x;
                    x = incomingQueue.remove().get();
                    if (x.isQuery()) {
                        //If it is a query dispatch to query processor
                        QueryProcessorMainThread.scheduleQuery(x);
                    } else {
                        //There should be a tweet Object be added to transport Object
                        //TODO
                        TweetObject tweet = null;

                        //Update tweet and termDictionaries
                        int tweet_id = TweetDictionary.insertTweetObject(tweet);
                        x.setTweetID(tweet_id);
                        List<Integer> termIds = new ArrayList<Integer>();
                        for (String term : x.getTerms()) {
                            int id = TermDictionary.insertTerm(term);
                            termIds.add(id);

                        }
                        x.setTermIDs(termIds);

                        //Add to indices
                        //TODO
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Could not get element from writer queue");
                }

            }
        }
    }
}
