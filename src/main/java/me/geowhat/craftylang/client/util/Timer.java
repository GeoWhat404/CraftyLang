package me.geowhat.craftylang.client.util;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class Timer {

    private Runnable code;
    private boolean running;
    private Runnable onCompletionListener;

    public Timer() {
        code = () -> {};
        running = false;
        onCompletionListener = null;
    }

    public void start() {
        running = true;
        ClientTickEvents.END_CLIENT_TICK.register(event -> {
            if (running)
                code.run();
        });
    }

    public void stop() {
        running = false;
    }

    public void setCode(Runnable code) {
        this.code = code;
    }

    public boolean isRunning() {
        return running;
    }

    public void setOnCompletionListener(Runnable listener) {
        this.onCompletionListener = listener;
    }

    public void executeOnCompletionListener() {
        if (onCompletionListener != null) {
            onCompletionListener.run();
        }
    }

    public Runnable getOnCompletionListener() {
        return this.onCompletionListener;
    }
}
