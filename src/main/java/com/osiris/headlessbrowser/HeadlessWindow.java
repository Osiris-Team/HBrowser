package com.osiris.headlessbrowser;

import com.eclipsesource.v8.NodeJS;

public class HeadlessWindow implements AutoCloseable{
    public static NodeJS NODE_JS = NodeJS.createNodeJS();
    private final JSContext jsContext = new JSContext(this);


    public static NodeJS getNodeJs() {
        return NODE_JS;
    }

    public JSContext getJsContext() {
        return jsContext;
    }

    @Override
    public void close() {
        //TODO
    }
}
