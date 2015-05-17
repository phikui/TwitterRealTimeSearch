package test;

import iocontroller.*;
import utilities.RandomObjectFactory;

/**
 * Created by phil on 17.05.2015.
 */
public class PreprocessorWritingTest {

    public static void main(String[] args) throws InterruptedException {
        int num_tweets = 500000;
        int num_preprocsessors = 4;
        boolean writerOutput = false;

        RandomObjectFactory randomObjectFactory = new RandomObjectFactory();
        Stemmer.init();


        //populate the incoming queue
        System.out.print("adding " + num_tweets + " random tweets to the incoming queue.");
        for (int i = 0; i < num_tweets; i++) {
            QueueContainer.getRawObjectQueue().add(randomObjectFactory.generateRandomRawObjecttReadyForPreprocessing());
        }
        System.out.println(" done.");



        System.out.println("Queue Populated");
        System.out.println("Preprocessor and writer threads will be started in 5 seconds");
        Thread.sleep(5000);

        long now = System.currentTimeMillis();

        System.out.println("Starting Preprocess Thread");
        PreprocessorMainThread preprocessor = new PreprocessorMainThread(num_preprocsessors);
        preprocessor.start();

        System.out.println("Starting writing Thread");
        WriterMainThread writer = new WriterMainThread(writerOutput);
        writer.start();

        System.out.println("Starting observer Thread");
        QueueObserver obs = new QueueObserver();
        obs.start();


        //insert some more tweets
        System.out.println("adding " + num_tweets + " more random tweets to the incoming queue.");
        for (int i = 0; i < num_tweets; i++) {
            QueueContainer.getRawObjectQueue().add(randomObjectFactory.generateRandomRawObjecttReadyForPreprocessing());
        }
        System.out.println("!!!!!!          Added all new tweets. !!!!!! ");

        while (!(QueueContainer.getRawObjectQueue().isEmpty() && QueueContainer.getPreprocessedOutputQueue().isEmpty())) {
            Thread.sleep(1000);
        }

        long timeTaken = System.currentTimeMillis() - now;
        System.out.println("Both queues empty, shutting down");
        System.out.println("It took a total of " + (timeTaken / 1000) + " seeconds to process " + num_tweets * 2 + " tweets");
        //send terminate requests
        preprocessor.terminate();
        writer.terminate();
        obs.terminate();

        //wait for actual termination
        preprocessor.join();
        writer.join();
        obs.join();

        System.exit(0);

    }
}
