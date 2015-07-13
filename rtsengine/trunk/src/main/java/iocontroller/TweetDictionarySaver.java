package iocontroller;

/**
 * Created by phil on 13.07.15.
 */
public class TweetDictionarySaver extends Thread {
    private final IOController parent;
    private final long intervalInMs;
    private final String filename;
    private volatile boolean isRunning;

    public TweetDictionarySaver(IOController parent, long intervalInMs, String filename) {
        this.parent = parent;
        this.intervalInMs = intervalInMs;
        this.filename = filename;
        this.isRunning = false;
    }


    public boolean isRunning() {
        return isRunning;
    }

    public void stopSaving() {
        isRunning = false;
    }

    public void run() {
        isRunning = true;
        while (isRunning) {
            parent.dumpTweetDictionaryToDatabase(filename);
            try {
                sleep(intervalInMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
