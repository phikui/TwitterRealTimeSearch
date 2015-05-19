package iocontroller.queryprocessor;

import model.QueryReturnObject;

import java.util.Date;
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
    private final String query;
    private final List<String> terms;
    private final int k;
    private final Date timestamp;

    public QueryWorker(String query, List<String> terms, int k, Date timestamp) {
        this.query = query;
        this.terms = terms;
        this.k = k;
        this.timestamp = timestamp;
    }

    public QueryReturnObject call() throws Exception {

        //TODO do the search algorithm
        return null;
    }
}
