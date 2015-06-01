package test;

import indices.IRTSIndex;
import indices.aoi.AOIIndex;
import indices.lsii.LSIIIndex;
import indices.tpl.TPLIndex;
import model.TransportObject;
import utilities.RandomObjectFactory;

import java.util.Date;
import java.util.List;

/**
 * Created by chans on 5/16/15.
 */
public class RTSIndexTestCase {

    private static RandomObjectFactory randomObjectFactory = new RandomObjectFactory();

    public static void main(String[] args) {
//        System.out.println(randomObjectFactory.generateRandomTransportObjectReadyForPreprocessing());
//
//        System.out.println(randomObjectFactory.generateRandomTransportObjectReadyForWriting());

//        IRTSIndex index = new AOIIndex();
        IRTSIndex index = new TPLIndex();
//        IRTSIndex index = new LSIIIndex();

        for (int i = 0; i < 50; i++) {
            insertAndPerformSimpleQueryOnTransportObject(index);
        }
    }


    private static void insertAndPerformSimpleQueryOnTransportObject(IRTSIndex index) {

        // Generate and insert and random object
        TransportObject transportObjectInsertion = randomObjectFactory.generateRandomTransportObjectReadyForIndexInsertion();
        index.insertTransportObject(transportObjectInsertion);

        // Print insertion TransportObject
        System.out.println("Inserted TransportObject:"+ " #"+transportObjectInsertion.getTweetID());
        System.out.println(transportObjectInsertion);
        System.out.println();

        // Query the index for the very first of the just inserted transported object
        String firstTermOfInsertedTransportObject = transportObjectInsertion.getTerms().get(0);
        TransportObject transportObjectQuery = new TransportObject(firstTermOfInsertedTransportObject, new Date(), 10);

        // Preprocess and write query TransportObject
        randomObjectFactory.preprocessTransportObjectDummy(transportObjectQuery);
        // TODO: Query doesn't need to be fully written to Models,
        //       just translating terms into termIDs would be sufficient
        randomObjectFactory.writeTransportObjectDummy(transportObjectQuery);

        // Print query TransportObject
        System.out.println("Query TransportObject:");
        System.out.println(transportObjectQuery);
        System.out.println();

        // Print query result

        List<Integer> tweetIDsAccordingToIndex = index.searchTweetIDs(transportObjectQuery);
        System.out.println("Found TweetIDs according to Index:");
        System.out.println(tweetIDsAccordingToIndex);


        // TODO: validate result
        // TODO: Also make sure to verify that the top-k (for example top-k with
        //       regard to highest timestamp) tweetIDs are returned by the index
        //       in the correct order.

        System.out.println("######");
    }

}
