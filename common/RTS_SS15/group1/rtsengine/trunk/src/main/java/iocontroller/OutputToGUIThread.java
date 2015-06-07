package iocontroller;

import model.QueryReturnObject;
import model.TweetObject;

import java.util.concurrent.ExecutionException;

/**
 * Created by phil on 31.05.2015.
 */
public class OutputToGUIThread extends Thread {

    private static IOController parent;
    private volatile boolean isTerminated = false;

    public OutputToGUIThread(IOController parent) {
        this.parent = parent;
    }

    public void run() {
        System.out.println("QueryToGUI Thread has started");
        while (!isTerminated) {
            try {
                if (parent.hasNextOutputElement()) {
                    QueryReturnObject next = parent.getNextOutputElement();
                    for (TweetObject tweet : next.getResults()) {
                        System.out.println(tweet.getText());
                    }
                    if (next.getResults().isEmpty()) {
                        System.out.println("no result");
                    }
                    //TODO sent to GUI
                } else {
                    Thread.sleep(500);
                }

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void terminate() {
        isTerminated = true;
    }

}
