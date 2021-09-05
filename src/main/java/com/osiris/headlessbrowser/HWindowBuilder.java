package com.osiris.headlessbrowser;

import java.util.Map;

public class HWindowBuilder {
    private final HBrowser parentBrowser;
    private boolean enableJavaScript = true;
    private Map<String, String> customHeaders = null;


    public HWindowBuilder(HBrowser parentBrowser) {
        this.parentBrowser = parentBrowser;
    }


    public HWindow build() {
        return new HWindow(this.parentBrowser, this.enableJavaScript, this.customHeaders);
    }

    public HWindowBuilder customHeaders(Map<String, String> customHeaders) {
        this.customHeaders = customHeaders;
        return this;
    }

    public HWindowBuilder enableJavaScript(boolean enableJavaScript) {
        this.enableJavaScript = enableJavaScript;
        return this;
    }

}
