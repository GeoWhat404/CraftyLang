package me.geowhat.craftylang.client.util;

import me.geowhat.craftylang.interpreter.CraftScript;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scheduler {
    private static final ScheduledExecutorService executor = CraftScript.executorService;
    private static boolean continueExecution = true;

    public static void executeAfter(Runnable code, long ms) {
        executor.schedule(code, ms, TimeUnit.MILLISECONDS);
    }

    public static void repeat(Runnable code, long intervalMs, int repetitions) {
        Runnable repeatedTask = new Runnable() {
            private int count = 0;

            @Override
            public void run() {
                if (count < repetitions && continueExecution) {

                    code.run();
                    count++;

                    executor.schedule(this, intervalMs, TimeUnit.MILLISECONDS);
                }
            }
        };
        repeatedTask.run();
    }

    public static void stopExecution() {
        continueExecution = false;
    }

    public static void startExecution() {
        continueExecution = true;
    }

    public static boolean isContinueExecution() {
        return continueExecution;
    }
}
