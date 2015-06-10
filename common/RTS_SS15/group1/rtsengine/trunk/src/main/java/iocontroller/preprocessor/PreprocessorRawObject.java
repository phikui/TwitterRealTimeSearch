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
        if (IOController.useStandfordStemmer) {
            stems = IOController.stemmer.get().stem(transportObject.getText());
        } else {
            stems = Stemmer.trivial_stem(transportObject.getText());
        }
        transportObject.setTerms(stems);

        return transportObject;
    }

}
