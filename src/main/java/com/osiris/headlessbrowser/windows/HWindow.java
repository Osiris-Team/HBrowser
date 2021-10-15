package com.osiris.headlessbrowser.windows;

public interface HWindow extends AutoCloseable {

    @Override
    void close() throws RuntimeException;
}
