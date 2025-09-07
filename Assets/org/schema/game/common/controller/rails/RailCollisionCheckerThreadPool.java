package org.schema.game.common.controller.rails;

import java.util.concurrent.*;

public class RailCollisionCheckerThreadPool {
    private static RailCollisionCheckerThreadPool instance;
    private final ExecutorService executor;
    private final LinkedBlockingQueue<Runnable> taskQueue;

    // Private constructor to prevent instantiation
    private RailCollisionCheckerThreadPool(int poolSize) {
        this.executor = Executors.newFixedThreadPool(poolSize);
        this.taskQueue = new LinkedBlockingQueue<>();
        startTaskProcessor();
    }

    // Method to get the singleton instance
    public static synchronized RailCollisionCheckerThreadPool getInstance(int poolSize) {
        if (instance == null) {
            instance = new RailCollisionCheckerThreadPool(poolSize);
        }
        return instance;
    }

    // Method to submit a task to the queue
    public void submitTask(Runnable task) {
        try {
            taskQueue.put(task);  // Add task to the queue
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Task submission interrupted: " + e.getMessage());
        }
    }

    // Private method to start processing tasks from the queue
    private void startTaskProcessor() {
        Runnable taskProcessor = () -> {
            while (true) {
                try {
                    Runnable task = taskQueue.take();  // Retrieve task from the queue
                    executor.submit(task);  // Submit task to the executor
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (RejectedExecutionException e) {
                    System.err.println("Task rejected: " + e.getMessage());
                }
            }
        };
        new Thread(taskProcessor).start();
    }

    // Method to shut down the thread pool
    public void shutdown() {
        executor.shutdown();
        System.out.println("ThreadPoolSingleton has been shut down.");
    }
}