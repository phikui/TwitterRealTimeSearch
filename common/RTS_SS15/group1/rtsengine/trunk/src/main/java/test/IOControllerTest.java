package test;

import indices.IndexDispatcher;
import iocontroller.IOController;
import model.TermDictionary;
import model.TweetDictionary;
import utilities.RandomObjectFactory;

/**
 * Created by phil on 17.05.15.
 */
public class IOControllerTest {

    public static void main(String[] args) throws InterruptedException {
        long now = System.currentTimeMillis();
        int num_tweets = 500000;
        int num_preprocessors = 4;
        IOController ioController = new IOController(num_preprocessors, 0, false);
        RandomObjectFactory randomObjectFactory = new RandomObjectFactory();
        ioController.startAll();

        //populate the incoming queue

        System.out.print("adding " + num_tweets + " random tweets to the incoming queue.");
        for (int i = 0; i < num_tweets; i++) {
            ioController.addRawObject(randomObjectFactory.generateRandomRawObjecttReadyForPreprocessing());
        }
        System.out.println(" done.");

        while (ioController.hasUnprocessedItems()) {
            Thread.sleep(1000);
        }

        ioController.stopAll();
        ioController.waitForTermination();
        System.out.println();
        System.out.println();
        System.out.println("Size of Term dictionary: " + TermDictionary.size());
        System.out.println("Size of Tweet dictionary: " + TweetDictionary.size());
        System.out.println("Size of AO index: " + IndexDispatcher.size());
        long timeTaken = System.currentTimeMillis() - now;
        System.out.println("It took a total of " + (timeTaken / 1000) + " seconds to generate and process "
                + num_tweets + " tweets, unsing " + num_preprocessors + " preprocessors");

        System.exit(0);

    }
}
