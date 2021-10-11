package com.osiris.headlessbrowser.windows;

import com.osiris.headlessbrowser.HBrowser;

import java.io.File;
import java.io.OutputStream;
import java.util.Map;

public class WindowBuilder {
    public final HBrowser parentBrowser;
    public boolean enableJavaScript = true;
    public Map<String, String> customHeaders = null;
    public OutputStream debugOutputStream = null;
    public int jsTimeout = 30;
    public boolean isHeadless = true;
    public File userDataDir = new File(System.getProperty("user.dir") + "/headless-browser/user-data");
    public boolean isDevTools = false;
    public int debuggingPort = 0;
    public String[] additionalStartupArgs = null;
    public boolean makeUndetectable = false;

    public WindowBuilder(HBrowser parentBrowser) {
        this.parentBrowser = parentBrowser;
    }

    public GraalWindow buildGraalJSWindow() {
        // TODO debugOutputStream jsTimeout
        return new GraalWindow(this.parentBrowser, this.enableJavaScript, this.customHeaders);
    }

    /**
     * For details see {@link PuppeteerWindow#PuppeteerWindow(HBrowser, boolean, OutputStream, int, boolean, File, boolean, int, boolean, String...)}.
     */
    public PuppeteerWindow buildPuppeteerWindow() {
        // TODO customHeaders
        return new PuppeteerWindow(this.parentBrowser, this.enableJavaScript, this.debugOutputStream, this.jsTimeout,
                this.isHeadless, this.userDataDir, this.isDevTools, this.debuggingPort, this.makeUndetectable, this.additionalStartupArgs);
    }

    /**
     * For details see {@link PuppeteerWindow#PuppeteerWindow(HBrowser, boolean, OutputStream, int, boolean, File, boolean, int, boolean, String...)}.
     */
    public PlaywrightWindow buildPlaywrightWindow() {
        return new PlaywrightWindow(this.parentBrowser, this.enableJavaScript, this.debugOutputStream, this.jsTimeout,
                this.isHeadless, this.userDataDir, this.isDevTools, this.makeUndetectable);
    }

    public LightWindow buildLightWindow() {
        return new LightWindow(this.parentBrowser, this.enableJavaScript, this.customHeaders, this.debugOutputStream,
                this.isHeadless, this.userDataDir, this.isDevTools, this.jsTimeout, this.makeUndetectable);
    }

    public WindowBuilder makeUndetectable(boolean val) {
        this.makeUndetectable = val;
        return this;
    }


    public WindowBuilder customHeaders(Map<String, String> val) {
        this.customHeaders = val;
        return this;
    }


    public WindowBuilder enableJavaScript(boolean val) {
        this.enableJavaScript = val;
        return this;
    }

    /**
     * For details see {@link PuppeteerWindow#PuppeteerWindow(HBrowser, boolean, OutputStream, int, boolean, File, boolean, int, String...)}.
     */
    public WindowBuilder debugOutputStream(OutputStream val) {
        this.debugOutputStream = val;
        return this;
    }

    /**
     * For details see {@link PuppeteerWindow#PuppeteerWindow(HBrowser, boolean, OutputStream, int, boolean, File, boolean, int, String...)}.
     */
    public WindowBuilder jsTimeout(int val) {
        this.jsTimeout = val;
        return this;
    }

    /**
     * For details see {@link PuppeteerWindow#PuppeteerWindow(HBrowser, boolean, OutputStream, int, boolean, File, boolean, int, String...)}.
     */
    public WindowBuilder headless(boolean val) {
        this.isHeadless = val;
        return this;
    }

    /**
     * For details see {@link PuppeteerWindow#PuppeteerWindow(HBrowser, boolean, OutputStream, int, boolean, File, boolean, int, String...)}.
     */
    public WindowBuilder userDataDir(File val) {
        this.userDataDir = val;
        return this;
    }

    /**
     * For details see {@link PuppeteerWindow#PuppeteerWindow(HBrowser, boolean, OutputStream, int, boolean, File, boolean, int, String...)}.
     */
    public WindowBuilder devTools(boolean val) {
        this.isDevTools = val;
        return this;
    }

    /**
     * For details see {@link PuppeteerWindow#PuppeteerWindow(HBrowser, boolean, OutputStream, int, boolean, File, boolean, int, String...)}.
     */
    public WindowBuilder debuggingPort(int val) {
        this.debuggingPort = val;
        return this;
    }

    /**
     * For details see {@link PuppeteerWindow#PuppeteerWindow(HBrowser, boolean, OutputStream, int, boolean, File, boolean, int, String...)}.
     */
    public WindowBuilder additionalStartupArgs(String... val) {
        this.additionalStartupArgs = val;
        return this;
    }

}
