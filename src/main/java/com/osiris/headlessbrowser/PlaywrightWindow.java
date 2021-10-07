package com.osiris.headlessbrowser;

import com.osiris.headlessbrowser.exceptions.NodeJsCodeException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class PlaywrightWindow implements AutoCloseable {
    private final NodeContext jsContext;
    private final OutputStream debugOutput;
    private final HBrowser parentBrowser;
    private boolean enableJavaScript;
    private String url;
    private final boolean isHeadless;
    private final File userDataDir;
    private final boolean isDevTools;
    private final File downloadTempDir;

    /**
     * <p style="color: red;">Note that this is not the recommended way of creating a NodeWindow object.</p>
     * Use the {@link WindowBuilder} instead. The {@link HBrowser} has a shortcut method for creating custom windows: {@link HBrowser#openCustomWindow()}.
     *
     * @param parentBrowser    The {@link HBrowser} this window was started from. <br><br>
     * @param enableJavaScript Enable/Disable JavaScript code execution for this window. <br><br>
     * @param debugOutput      Default is null. Otherwise, writes/prints debug related information and JavaScript code console output to the debug output. <br><br>
     * @param jsTimeout        Default is 30s. The timeout in seconds to wait before throwing a {@link NodeJsCodeException}, if the running js code didn't finish. Set to 0 to disable. <br><br>
     * @param isHeadless       Whether to run browser in headless mode. Defaults to true unless the devtools option is true. <br><br>
     * @param userDataDir      Path to a User Data Directory. Default is ./headless-browser/user-data (the "." represents the current working directory). <br><br>
     * @param isDevTools       Whether to auto-open a DevTools panel for each tab. If this option is true, the headless option will be set false. <br><br>
     */
    public PlaywrightWindow(HBrowser parentBrowser, boolean enableJavaScript, OutputStream debugOutput, int jsTimeout,
                            boolean isHeadless, File userDataDir, boolean isDevTools) {
        this.parentBrowser = parentBrowser;
        this.debugOutput = debugOutput;
        this.isHeadless = isHeadless;
        this.userDataDir = userDataDir;
        this.isDevTools = isDevTools;
        try {
            this.jsContext = new NodeContext(new File(userDataDir.getParentFile() + "/node-js"), debugOutput, jsTimeout);
            jsContext.npmInstall("playwright");

            // Define global variables/constants
            jsContext.executeJavaScript(
                    "const playwright = require('playwright');\n" +
                            "var browser = null;\n" +
                            "var page = null;\n", 30, false);

            if (userDataDir == null) {
                userDataDir = new WindowBuilder(null).userDataDir; // Get the default value
            }
            if (userDataDir.isFile())
                throw new Exception("userDataDir must be a directory and cannot be a file (" + userDataDir.getAbsolutePath() + ")!");
            if (!userDataDir.exists()) userDataDir.mkdirs();

            // To be able to download files:
            downloadTempDir = new File(userDataDir + "/downloads-temp");
            if (!downloadTempDir.exists()) downloadTempDir.mkdirs();

            jsContext.executeJavaScript(
                    "browser = await playwright['chromium'].launchPersistentContext('" + userDataDir.getAbsolutePath().replace("\\", "/") + "', " +
                            "{ acceptDownloads: true,\n" +
                            "  headless : " + isHeadless + ",\n" +
                            "  javaScriptEnabled: " + enableJavaScript + ",\n" +
                            //"  downloadsPath: '" + downloadTempDir.getAbsolutePath().replace("\\", "/") + "',\n" + // Active issue at: https://github.com/microsoft/playwright/issues/9279
                            "  devtools: " + isDevTools + "\n" +
                            "});\n" +
                            "page = await browser.newPage();\n", 30, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public PlaywrightWindow load(String url) throws NodeJsCodeException {
        if (!url.startsWith("http"))
            url = "https://" + url;

        jsContext.executeJavaScript("" +
                "var response = await page.goto('" + url + "');\n");
        this.url = url;
        return this;
    }

    public PlaywrightWindow download(String url, File dest) throws IOException {
        File download = new File(jsContext.executeJavaScriptAndGetResult("" +
                "console.log('preparing download'); \n" +
                "await page.goto('about:blank');\n" + // To make sure we got a page where we can actually add the download link and click on it
                "await page.evaluate(`" +
                "var myCUSTel = document.createElement('a');" +
                "myCUSTel.innerHTML = 'Download';" +
                "myCUSTel.setAttribute('href', '" + url + "');" +
                "myCUSTel.setAttribute('id', 'myCUSTel');" +
                "document.getElementsByTagName('body')[0].appendChild(myCUSTel);`);\n" +
                "var [ download ] = await Promise.all([\n" +
                "  page.waitForEvent('download'), // wait for download to start\n" +
                "  page.click('id=myCUSTel')\n" +
                "]);\n" +
                "var downloadError = await download.failure();\n" +
                "if (downloadError!=null) throw new Error(downloadError);\n" +
                "var result = await download.path();\n", 0, true).trim());
        if (dest != null){
            Files.copy(download.toPath(), dest.toPath());
            download.delete();
        }
        this.url = url;
        return this;
    }

    @Override
    public void close() throws Exception {
        // Running js: browser.close() here causes a weird exception: https://github.com/isaacs/rimraf/issues/221
        // Since it's not mandatory we just don't do it.
        jsContext.close();
    }

}
