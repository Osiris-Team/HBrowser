package com.osiris.headlessbrowser;

import com.eclipsesource.v8.NodeJS;

import java.util.ArrayList;
import java.util.List;

public class HeadlessBrowser {
    public static NodeJS NODE_JS = NodeJS.createNodeJS();
    private final JSContext jsContext = new JSContext();

    public HeadlessBrowser() {
    }

    public HeadlessWindow openNewWindow() {
        return new HeadlessWindow();
    }

    public void closeWindow(HeadlessWindow HeadlessWindow) {
        HeadlessWindow.close();
    }

    public static NodeJS getNodeJs() {
        return NODE_JS;
    }

    public JSContext getJsContext() {
        return jsContext;
    }
}
