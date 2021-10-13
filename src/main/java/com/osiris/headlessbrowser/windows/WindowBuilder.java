package com.osiris.headlessbrowser.windows;

import com.osiris.headlessbrowser.HBrowser;
import com.osiris.headlessbrowser.exceptions.NodeJsCodeException;

import java.io.File;
import java.io.OutputStream;
import java.util.Map;

public class WindowBuilder {
    /**
     * The {@link HBrowser} this window was started from.
     */
    public final HBrowser parentBrowser;
    /**
     * Enable/Disable JavaScript code execution for this window.
     */
    public boolean enableJavaScript = true;
    /**
     * Default is null. Otherwise the provided headers will be used instead of the default ones.
     */
    public Map<String, String> customHeaders = null;
    /**
     * Default is null. Otherwise, writes/prints debug related information and JavaScript code console output to the debug output.
     */
    public OutputStream debugOutputStream = null;
    /**
     * Default is 30s. The timeout in seconds to wait before throwing a {@link NodeJsCodeException}, if the running js code didn't finish. Set to 0 to disable.
     */
    public int jsTimeout = 30;
    /**
     * Whether to run browser in headless mode. Defaults to true unless the devtools option is true.
     */
    public boolean isHeadless = true;
    /**
     * Path to a User Data Directory. Default is ./headless-browser/user-data (the "." represents the current working directory).
     */
    public File userDataDir = new File(System.getProperty("user.dir") + "/headless-browser/user-data");
    /**
     * Whether to auto-open a DevTools panel for each tab. If this option is true, the headless option will be set false.
     */
    public boolean isDevTools = false;
    /**
     * Default is 0. Specify custom debugging port. Pass 0 to discover a random port.
     */
    public int debuggingPort = 0;
    /**
     * Default is null. Additional arguments to pass to the browser instance. The list of Chromium flags can be found here: https://peter.sh/experiments/chromium-command-line-switches/
     */
    public String[] additionalStartupArgs = null;
    /**
     * Makes this window indistinguishable from 'real', user operated windows, by using the code from puppeteer/playwright-extra and puppeteer-extra-plugin-stealth.
     */
    public boolean makeUndetectable = false;

    public WindowBuilder(HBrowser parentBrowser) {
        this.parentBrowser = parentBrowser;
    }

    public GraalWindow buildGraalWindow() {
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

    /**
     * For details see {@link #makeUndetectable}.
     */
    public WindowBuilder makeUndetectable(boolean val) {
        this.makeUndetectable = val;
        return this;
    }

    /**
     * For details see {@link #customHeaders}.
     */
    public WindowBuilder customHeaders(Map<String, String> val) {
        this.customHeaders = val;
        return this;
    }

    /**
     * For details see {@link #enableJavaScript}.
     */
    public WindowBuilder enableJavaScript(boolean val) {
        this.enableJavaScript = val;
        return this;
    }

    /**
     * For details see {@link #debugOutputStream}
     */
    public WindowBuilder debugOutputStream(OutputStream val) {
        this.debugOutputStream = val;
        return this;
    }

    /**
     * For details see {@link #jsTimeout}.
     */
    public WindowBuilder jsTimeout(int val) {
        this.jsTimeout = val;
        return this;
    }

    /**
     * For details see {@link #isHeadless}.
     */
    public WindowBuilder headless(boolean val) {
        this.isHeadless = val;
        return this;
    }

    /**
     * For details see {@link #userDataDir}.
     */
    public WindowBuilder userDataDir(File val) {
        this.userDataDir = val;
        return this;
    }

    /**
     * For details see {@link #isDevTools}.
     */
    public WindowBuilder devTools(boolean val) {
        this.isDevTools = val;
        return this;
    }

    /**
     * For details see {@link #debuggingPort}.
     */
    public WindowBuilder debuggingPort(int val) {
        this.debuggingPort = val;
        return this;
    }

    /**
     * For details see {@link #additionalStartupArgs}.
     */
    public WindowBuilder additionalStartupArgs(String... val) {
        this.additionalStartupArgs = val;
        return this;
    }

}
