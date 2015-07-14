package iocontroller.preprocessor;

import java.util.List;

/**
 * Created by phil on 14.07.15.
 */
public class SpeechAnalysisResult {
    public List<String> stems;
    public int sentiment;

    public SpeechAnalysisResult(List<String> stems, int sentiment) {
        this.stems = stems;
        this.sentiment = sentiment;
    }
}
