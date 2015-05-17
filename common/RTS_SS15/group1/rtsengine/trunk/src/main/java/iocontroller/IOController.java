package iocontroller;

import static java.lang.Runtime.getRuntime;

/**
 * Created by phil on 17.05.15.
 */
public class IOController {
    private final PreprocessingMainThread preProcessor;
    private final WriterMainThread writer;
    private final QueueObserver queueObserver;


    public IOController(int numPreProcessors, int numQueryProcessors, boolean writerOutput) {
        preProcessor = new PreprocessingMainThread(numPreProcessors);
        writer = new WriterMainThread(writerOutput);
        queueObserver = new QueueObserver();
        Stemmer.init();
    }

    public IOController() {
        this(getRuntime().availableProcessors(), getRuntime().availableProcessors(), false);
    }

    public void startAll() {
        preProcessor.start();
        writer.start();
    }

    public void activateQueueObserver() {
        queueObserver.start();
    }

    public void stopAll() {
        writer.terminate();
        preProcessor.terminate();
        queueObserver.terminate();
    }

    public void stopPreprocessor() {
        preProcessor.terminate();
    }

    public void stopWriter() {
        writer.terminate();
    }

    public void waitForTermination() throws InterruptedException {
        preProcessor.join();
        writer.join();
        queueObserver.join();

    }
}
