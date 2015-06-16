package iocontroller.queryprocessor;

import indices.IndexDispatcher;
import model.QueryReturnObject;
import model.TransportObject;
import model.TweetDictionary;
import model.TweetObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by phil on 19.05.15.
 */

/*

    This class implements the code that the queryProcessor executes.
      The call method returns a list of Tweets that are the results of this query.

 */
public class QueryWorker implements Callable<QueryReturnObject> {
    private final TransportObject query;


    public QueryWorker(TransportObject query) {
        this.query = query;
    }

    public QueryReturnObject call() throws Exception {
        List<Integer> resultsIndex = IndexDispatcher.searchTweetIDs(query);

        // I don't know what the hell this is but intelliJ suggested doing this instead of the other code
        // List<TweetObject> results = resultsIndex.stream().map(TweetDictionary::getTweetObject).collect(Collectors.toList());

        List<TweetObject> results = new ArrayList<>();
        for (int index : resultsIndex) {
            results.add(TweetDictionary.getTransportObject(index).getTweetObject());
        }

        //if results are empty create dummy object
        //if (results.isEmpty()) {
        //    TweetObject dummy = new TweetObject(query.getText());
        //    results.add(dummy);
        //}

        return new QueryReturnObject(query.getText(), results);
    }
}
