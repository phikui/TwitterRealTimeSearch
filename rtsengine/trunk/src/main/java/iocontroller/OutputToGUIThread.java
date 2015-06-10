package iocontroller;

import gui.MainAppController;
import model.QueryReturnObject;

import java.util.concurrent.ExecutionException;

/**
 * Created by phil on 31.05.2015.
 */
public class OutputToGUIThread extends Thread {

    private static IOController parent;
    private volatile boolean isTerminated = false;

    public OutputToGUIThread(IOController parent) {
        OutputToGUIThread.parent = parent;
    }

    public void run() {
        System.out.println("QueryToGUI Thread has started");
        while (!isTerminated) {
            try {
                System.out.println("sending query");
                    QueryReturnObject next = parent.getNextOutputElement();
                    MainAppController.sendQueryResults(next);
                System.out.println("sending done");
                   /* if(next.getResults().isEmpty()) {
                        parent.putMessage();
                    }*/

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
