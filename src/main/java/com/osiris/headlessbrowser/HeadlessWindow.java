package com.osiris.headlessbrowser;


public class HeadlessWindow implements AutoCloseable {
    private final JSContext jsContext = new JSContext(this);


    public JSContext getJsContext() {
        return jsContext;
    }

    @Override
    public void close() {
        //TODO
    }
}
