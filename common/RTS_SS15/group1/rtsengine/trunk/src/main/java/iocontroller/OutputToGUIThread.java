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
        while (!isTerminated) {
            try {
                QueryReturnObject next = parent.getNextOutputElement();
                MainAppController.sendQueryResults(next);
                if(next.getResults().isEmpty()){
                    parent.putMessage(true);
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
