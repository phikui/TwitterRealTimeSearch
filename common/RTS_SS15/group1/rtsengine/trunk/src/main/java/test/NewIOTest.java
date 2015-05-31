package test;

import gui.ConfigurationObject;
import indices.IndexDispatcher;
import iocontroller.IOController;
import model.TermDictionary;
import model.TweetDictionary;

/**
 * Created by phil on 31.05.2015.
 */
public class NewIOTest {

    public static void main(String[] args) throws InterruptedException {
        //creating the IOProcessor
        int num_preprocessors = 2;
        int num_queryProcessors = 1;
        boolean debugOutputs = false;
        ConfigurationObject.setIndexType("aoi");
        IOController ioController = new IOController(num_preprocessors, num_queryProcessors, debugOutputs);

        ioController.startAll();
        ioController.collectTweets();

        while (true) {
            System.out.println();
            System.out.println();
            System.out.println("Size of Term dictionary: " + TermDictionary.size());
            System.out.println("Size of Tweet dictionary: " + TweetDictionary.size());
            System.out.println("Size of AO index: " + IndexDispatcher.size());
            Thread.sleep(3000);
        }


    }
}
