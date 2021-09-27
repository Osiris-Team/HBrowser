package com.osiris.headlessbrowser;

import java.util.Map;

public class WindowBuilder {
    private final HBrowser parentBrowser;
    private boolean enableJavaScript = true;
    private Map<String, String> customHeaders = null;


    public WindowBuilder(HBrowser parentBrowser) {
        this.parentBrowser = parentBrowser;
    }


    public GraalWindow buildGraalJSWindow() {
        return new GraalWindow(this.parentBrowser, this.enableJavaScript, this.customHeaders);
    }

    public NodeWindow buildNodeJSWindow() {
        return new NodeWindow(this.parentBrowser, this.enableJavaScript, this.customHeaders);
    }

    public WindowBuilder customHeaders(Map<String, String> customHeaders) {
        this.customHeaders = customHeaders;
        return this;
    }

    public WindowBuilder enableJavaScript(boolean enableJavaScript) {
        this.enableJavaScript = enableJavaScript;
        return this;
    }

}
