package workers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPool {
    private final List<WorkerThread> workers = new ArrayList<>();
    private final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    private volatile boolean isRunning = true;

    public ThreadPool(int numThreads) {
        for (int i = 0; i < numThreads; i++) {
            WorkerThread worker = new WorkerThread();
            workers.add(worker);
            worker.start();
        }
    }

    public void submit(Runnable task) throws IllegalStateException, InterruptedException {
        if (!isRunning) {
            throw new IllegalStateException("Пул остановлен, новые задачи не принимаются");
        }

        try {
            taskQueue.put(task);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Поток был прерван при отправке задания", e);
        }
    }

    public void shutdown() {
        isRunning = false;
        for (WorkerThread worker : workers) {
            worker.interrupt();
        }
    }

    private class WorkerThread extends Thread {
        @Override
        public void run() {
            while (isRunning || !taskQueue.isEmpty()) {
                try {
                    Runnable task = taskQueue.take();
                    task.run();
                    System.out.println("[WorkerPool] Выполнил задачу\n");
                }
                catch (InterruptedException e) {
                    if (!isRunning) {
                        break;
                    }
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
