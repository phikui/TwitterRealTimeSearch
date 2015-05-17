package iocontroller;

/**
 * Created by phil on 17.05.2015.
 */

//This class is for outputting diagnostic information of the queues
public class QueueObserver extends Thread {
    private volatile boolean isTerminated = false;

    public void run() {
        while (!isTerminated) {
            System.out.println("_______________________________________");
            System.out.println("Size of preprocessor queue: " + QueueContainer.getRawObjectQueue().size());
            System.out.println("Size of writer queue: " + QueueContainer.getPreprocessedOutput().size());
            System.out.println("_______________________________________");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Queue Observer has stopped");
    }

    public void terminate() {
        isTerminated = true;
    }

}
