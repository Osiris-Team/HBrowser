package com.osiris.headlessbrowser.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class AsyncReader {
    public final InputStream inputStream;
    public final Thread thread;
    public List<Consumer<String>> listeners = new CopyOnWriteArrayList<>();

    @SafeVarargs
    public AsyncReader(InputStream inputStream, Consumer<String>... listeners) {
        this.inputStream = inputStream;
        if (listeners != null && listeners.length != 0) this.listeners.addAll(Arrays.asList(listeners));
        Object o = this;
        thread = new Thread(() -> {
            String line = "";
            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                while ((line = br.readLine()) != null) {
                    for (Consumer<String> listener :
                            this.listeners) {
                        listener.accept(line);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error in thread for object '" + o + "' Details:");
                e.printStackTrace();
            }
        });
        thread.start();
    }
}
