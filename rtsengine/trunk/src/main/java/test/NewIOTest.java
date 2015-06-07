package test;

import indices.IndexDispatcher;
import iocontroller.IOController;
import model.ConfigurationObject;
import model.TermDictionary;
import model.TransportObject;
import model.TweetDictionary;

import java.util.Calendar;

/**
 * Created by phil on 31.05.2015.
 */
public class NewIOTest {

    public static void main(String[] args) throws InterruptedException {
        //creating the IOProcessor
        int num_preprocessors = 2;
        int num_queryProcessors = 1;
        boolean debugOutputs = false;
        ConfigurationObject.setIndexType(ConfigurationObject.Index.APPEND_ONLY);
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
            TransportObject query = new TransportObject("joke", Calendar.getInstance().getTime(), 5);
            ioController.addTransportObject(query);
        }


    }
}
