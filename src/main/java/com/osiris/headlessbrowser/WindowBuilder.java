package com.osiris.headlessbrowser;

import com.osiris.headlessbrowser.exceptions.NodeJsCodeException;

import java.io.OutputStream;
import java.util.Map;

public class WindowBuilder {
    private final HBrowser parentBrowser;
    private boolean enableJavaScript = true;
    private Map<String, String> customHeaders = null;
    private OutputStream debugOutputStream = null;


    public WindowBuilder(HBrowser parentBrowser) {
        this.parentBrowser = parentBrowser;
    }


    public GraalWindow buildGraalJSWindow() {
        // TODO debugOutputStream
        return new GraalWindow(this.parentBrowser, this.enableJavaScript, this.customHeaders);
    }

    public NodeWindow buildNodeJSWindow() {
        // TODO customHeaders
        return new NodeWindow(this.parentBrowser, this.enableJavaScript, this.debugOutputStream);
    }

    public WindowBuilder customHeaders(Map<String, String> customHeaders) {
        this.customHeaders = customHeaders;
        return this;
    }

    public WindowBuilder enableJavaScript(boolean enableJavaScript) {
        this.enableJavaScript = enableJavaScript;
        return this;
    }

    public WindowBuilder debugOutputStream(OutputStream debugOutputStream) {
        this.debugOutputStream = debugOutputStream;
        return this;
    }

}
