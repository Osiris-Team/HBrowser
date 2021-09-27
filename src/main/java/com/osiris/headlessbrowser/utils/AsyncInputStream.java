package com.osiris.headlessbrowser.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class AsyncInputStream {
    public List<Consumer<String>> listeners = new CopyOnWriteArrayList<>();
    private final InputStream inputStream;
    private final Thread thread;

    public AsyncInputStream(InputStream inputStream) {
        this.inputStream = inputStream;

        Object o = this;
        thread = new Thread(() -> {
            String line = "";
            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                while ((line = br.readLine()) != null) {
                    for (Consumer<String> listener :
                            listeners) {
                        listener.accept(line);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error in thread for object '" + o + "' Details:");
                e.printStackTrace();
            }
        });
        thread.start();
    }
}
