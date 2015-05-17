package test;

import iocontroller.PreprocessingMainThread;
import iocontroller.QueueContainer;
import iocontroller.Stemmer;
import iocontroller.WriterMainThread;
import model.PreprocessingRawObject;
import utilities.RandomObjectFactory;

/**
 * Created by phil on 17.05.2015.
 */
public class PreprocessorWritingTest {

    public static void main(String[] args) throws InterruptedException {
        int num_tweets = 10000;
        boolean writerOutput = true;

        RandomObjectFactory randomObjectFactory = new RandomObjectFactory();
        Stemmer.init();

        //populate the incoming queue

        for (int i = 0; i < num_tweets; i++) {
            QueueContainer.getRawObjectQueue().add(randomObjectFactory.generateRandomRawObjecttReadyForPreprocessing());
        }
        System.out.println("added " + num_tweets + " random tweets");

        for (PreprocessingRawObject x : QueueContainer.getRawObjectQueue()) {
            // System.out.println(x.getTweet().getText());
        }

        System.out.println("Queue Populated");
        System.out.println("Preprocessor and writer threads will be started in 5 seconds");
        Thread.sleep(5000);

        System.out.println("Starting Preprocess Thread");
        PreprocessingMainThread preprocessor = new PreprocessingMainThread(2);
        preprocessor.start();

        System.out.println("Starting writing Thread");
        WriterMainThread writer = new WriterMainThread(writerOutput);
        writer.start();

        //insert some more tweets
        for (int i = 0; i < num_tweets; i++) {
            QueueContainer.getRawObjectQueue().add(randomObjectFactory.generateRandomRawObjecttReadyForPreprocessing());
        }
        System.out.println("added " + num_tweets + " more random tweets");

    }
}
