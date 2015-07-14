package iocontroller.preprocessor;

import iocontroller.IOController;
import model.TransportObject;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by phil on 16.05.15.
 */
public class PreprocessorRawObject implements Callable<TransportObject> {
    private TransportObject transportObject;

    //Tweet constructor
    public PreprocessorRawObject(TransportObject transportObject) {
        this.transportObject = transportObject;

    }


    //Code for prepossessing
    public TransportObject call() throws Exception {
        List<String> stems;
        int sentiment = 0;
        if (IOController.useStandfordStemmer) {
            SpeechAnalysisResult result = IOController.stemmer.get().stemmingAndSentiment(transportObject.getText());
            stems = result.stems;
            sentiment = result.sentiment;
        } else {
            stems = SpeechAnalyser.trivial_stem(transportObject.getText());
        }
        transportObject.setTerms(stems);
        transportObject.setSentiment(sentiment);


        return transportObject;
    }

}
