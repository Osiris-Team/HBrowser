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
     * Default is null. Otherwise, writes/prints debug related information and JavaScript code console output to the provided {@link OutputStream}.
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
    public File userDataDir;
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
     * Makes this window indistinguishable from 'real', user operated windows,
     * by installing additional software: <br>
     * {@link PlaywrightWindow} -> <a href="https://github.com/berstend/puppeteer-extra/tree/master/packages/playwright-extra">
     *           playwright-extra</a> <br>
     * {@link PuppeteerWindow} ->  <a href="https://github.com/berstend/puppeteer-extra/tree/master/packages/puppeteer-extra-plugin-stealth">
     *     puppeteer-extra-plugin-stealth</a> <br>
     * Other windows are not supported. <br<
     */
    public boolean makeUndetectable = false;
    /**
     * If true, a new, unique, temporary directory will be created. <br>
     * The value of {@link #userDataDir} will be overwritten. <br>
     * Example: ./headless-browser/user-data-89213<br>
     * As you can see, the directory name will contain the {@link HWindow} objects unique {@link #hashCode()} as {@link Integer#toHexString(int)}. <br>
     * The directory will get deleted on {@link HWindow#close()}. <br>
     */
    public boolean temporaryUserDataDir = false;

    public WindowBuilder(HBrowser parentBrowser) {
        this.parentBrowser = parentBrowser;
        this.userDataDir = new File(parentBrowser.getMainDirectory() + "/user-data");
    }

    public PuppeteerWindow buildPuppeteerWindow() {
        return new PuppeteerWindow(this.parentBrowser, this.enableJavaScript, this.debugOutputStream, this.jsTimeout,
                this.isHeadless, this.userDataDir, this.isDevTools, this.debuggingPort, this.makeUndetectable, this.additionalStartupArgs);
    }

    public PlaywrightWindow buildPlaywrightWindow() {
        return new PlaywrightWindow(this.parentBrowser, this.enableJavaScript, this.debugOutputStream, this.jsTimeout,
                this.isHeadless, this.userDataDir, this.isDevTools, this.makeUndetectable, this.temporaryUserDataDir);
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

    /**
     * For details see {@link #temporaryUserDataDir}.
     */
    public WindowBuilder temporaryUserDataDir(boolean val) {
        this.temporaryUserDataDir = val;
        return this;
    }

}
