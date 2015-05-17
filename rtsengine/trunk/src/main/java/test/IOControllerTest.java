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
        int num_tweets = 500000;
        IOController ioController = new IOController(4, 4, false);
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

        System.exit(0);

    }
}
