package model;

import java.util.List;

/**
 * Created by phil on 19.05.15.
 */
public class QueryReturnObject {
    private final String query;
    private final List<TweetObject> results;

    public QueryReturnObject(String query, List<TweetObject> results) {
        this.query = query;
        this.results = results;
    }

    public String getQuery() {
        return query;
    }

    public List<TweetObject> getResults() {
        return results;
    }
}
