package com.osiris.headlessbrowser;


import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.osiris.headlessbrowser.exceptions.NodeJsCodeException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Headless-Window.
 *
 * @author Osiris-Team
 */
public class PuppeteerWindow implements AutoCloseable {
    private final NodeContext jsContext;
    private final OutputStream debugOutput;
    private HBrowser parentBrowser;
    private boolean enableJavaScript;
    private String url;
    private final boolean isHeadless;
    private final File userDataDir;
    private final boolean isDevTools;
    private final int debuggingPort;
    private final String[] additionalStartupArgs;
    private final File downloadTempDir;

    /**
     * <p style="color: red;">Note that this is not the recommended way of creating a NodeWindow object.</p>
     * Use the {@link WindowBuilder} instead. The {@link HBrowser} has a shortcut method for creating custom windows: {@link HBrowser#openCustomWindow()}.
     *
     * @param parentBrowser         The {@link HBrowser} this window was started from. <br><br>
     * @param enableJavaScript      Enable/Disable JavaScript code execution for this window. <br><br>
     * @param debugOutput           Default is null. Otherwise, writes/prints debug related information and JavaScript code console output to the debug output. <br><br>
     * @param jsTimeout             Default is 30s. The timeout in seconds to wait before throwing a {@link NodeJsCodeException}, if the running js code didn't finish. Set to 0 to disable. <br><br>
     * @param isHeadless            Whether to run browser in headless mode. Defaults to true unless the devtools option is true. <br><br>
     * @param userDataDir           Path to a User Data Directory. Default is ./headless-browser/user-data (the "." represents the current working directory). <br><br>
     * @param isDevTools            Whether to auto-open a DevTools panel for each tab. If this option is true, the headless option will be set false. <br><br>
     * @param debuggingPort         Default is 0. Specify custom debugging port. Pass 0 to discover a random port. <br><br>
     * @param additionalStartupArgs Default is null. Additional arguments to pass to the browser instance. The list of Chromium flags can be found here: https://peter.sh/experiments/chromium-command-line-switches/ <br><br>
     */
    public PuppeteerWindow(HBrowser parentBrowser, boolean enableJavaScript, OutputStream debugOutput, int jsTimeout,
                           boolean isHeadless, File userDataDir, boolean isDevTools, int debuggingPort, String... additionalStartupArgs) {
        this.parentBrowser = parentBrowser;
        this.debugOutput = debugOutput;
        this.isHeadless = isHeadless;
        this.userDataDir = userDataDir;
        this.isDevTools = isDevTools;
        this.debuggingPort = debuggingPort;
        this.additionalStartupArgs = additionalStartupArgs;
        try {
            this.jsContext = new NodeContext(new File(userDataDir.getParentFile() + "/node-js"), debugOutput, jsTimeout);
            jsContext.npmInstall("puppeteer");
            // Define global variables/constants
            jsContext.executeJavaScript(
                    "const puppeteer = require('puppeteer');\n" +
                            "var browser = null;\n" +
                            "var page = null;\n" +
                            "var downloadFile = null;\n", 30, false);

            StringBuilder jsInitCode = new StringBuilder();
            jsInitCode.append("var defaultArgs = {\n");
            jsInitCode.append("  headless : " + isHeadless + ",\n");
            if (additionalStartupArgs != null) {
                jsInitCode.append("  args: " + Arrays.toString(additionalStartupArgs) + ",\n");
            }
            if (userDataDir == null) {
                userDataDir = new WindowBuilder(null).userDataDir; // Get the default value
            }
            if (userDataDir.isFile())
                throw new Exception("userDataDir must be a directory and cannot be a file (" + userDataDir.getAbsolutePath() + ")!");
            if (!userDataDir.exists()) userDataDir.mkdirs();
            jsInitCode.append("  userDataDir: \"" + userDataDir.getAbsolutePath().replace("\\", "/") + "\",\n");
            jsInitCode.append("  devtools: " + isDevTools + ",\n");
            jsInitCode.append("  debuggingPort: " + debuggingPort + "\n");
            jsInitCode.append("};\n" +
                    "var argsAsArray = puppeteer.defaultArgs(defaultArgs);\n" +
                    "console.log(puppeteer.defaultArgs(defaultArgs));\n");
            //jsInitCode.append("browser = await puppeteer.launch(defaultArgs);\n");
            jsInitCode.append("browser = await puppeteer.launch({\n" +
                    "ignoreDefaultArgs: true,\n" +
                    "args: [\n" +
                    //"   '--disable-background-networking',\n" +
                    //"   '--enable-features=NetworkService,NetworkServiceInProcess',\n" + // Make sure to enable all features
                    "   '--disable-background-timer-throttling',\n" +
                    //"   '--disable-backgrounding-occluded-windows',\n" +
                    "   '--disable-breakpad',\n" +
                    "   '--disable-client-side-phishing-detection',\n" +
                    //"   '--disable-component-extensions-with-background-pages',\n" +
                    "   '--disable-default-apps',\n" +
                    "   '--disable-dev-shm-usage',\n" +
                    "   '--disable-extensions',\n" +
                    "   '--disable-features=Translate',\n" +
                    "   '--disable-hang-monitor',\n" +
                    "   '--disable-ipc-flooding-protection',\n" +
                    "   '--disable-popup-blocking',\n" +
                    "   '--disable-prompt-on-repost',\n" +
                    "   '--disable-web-security',\n" + // NEW  !
                    //"   '--disable-renderer-backgrounding',\n" +
                    "   '--disable-sync',\n" +
                    "   '--test-type=browser',\n" + //NEW !
                    "   '--force-color-profile=srgb',\n" +
                    "   '--metrics-recording-only',\n" +
                    "   '--no-first-run',\n" +
                    //"   '--enable-automation',\n" +
                    "   '--password-store=basic',\n" +
                    "   '--use-mock-keychain',\n" +
                    "   '--enable-blink-features=IdleDetection',\n" +
                    "   '--user-data-dir=D:\\\\Coding\\\\JAVA\\\\Headless-Browser\\\\headless-browser\\\\user-data',\n" +
                    "   '--headless',\n" +
                    "   '--hide-scrollbars',\n" +
                    "   '--mute-audio',\n" +
                    "   '--evaluate_capability=capabilities:{" +
                    "\"browserName\":\"chrome\"," +
                    "\"acceptInsecureCerts\":true," +
                    "\"chromeOptions\":{" +
                    "\"prefs\":{" +
                    "\"download\":{" +
                    "\"prompt_for_download\":false," +
                    "\"download_restrictions\":0," +
                    "\"directory_upgrade\":true," +
                    "\"default_directory\":\"D:\\\\Coding\\\\JAVA\\\\Headless-Browser\\\\headless-browser\\\\user-data\"," +
                    "\"safebrowsing.enabled\":false," +
                    "\"safebrowsing.disable_download_protection\":true," +
                    "}," +
                    "\"safebrowsing\":{" +
                    "\"enabled\":true" +
                    "}" +
                    "}" +
                    "}" +
                    "}',\n" +
                    "   'about:blank'\n" +
                    " ]\n" +
                    "});\n");
            jsInitCode.append("page = await browser.newPage();\n");
            jsContext.executeJavaScript(jsInitCode.toString(), 30, false);

            // To be able to download files:
            downloadTempDir = new File(userDataDir + "/downloads-temp");
            if (!downloadTempDir.exists()) downloadTempDir.mkdirs();
            jsContext.executeJavaScript("var cdpSession = await page.target().createCDPSession();\n" +
                    "await cdpSession.send('Page.setDownloadBehavior', {'behavior': 'allow', 'downloadPath': " +
                    "'" + downloadTempDir.getAbsolutePath().replace("\\", "\\\\") + "'});\n"); // Windows path must stay windows path

            setEnableJavaScript(enableJavaScript);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        // Running js: browser.close() here causes a weird exception: https://github.com/isaacs/rimraf/issues/221
        // Since it's not mandatory we just don't do it.
        jsContext.close();
    }

    /**
     * Load the contents from the provided url into the current {@link PuppeteerWindow}.
     *
     * @param url Examples: https://www.wikipedia.org or wikipedia.org.
     * @return the current {@link PuppeteerWindow} for chained method calls.
     * @throws IOException
     */
    public PuppeteerWindow load(String url) throws IOException, NodeJsCodeException {
        if (!url.startsWith("http"))
            url = "https://" + url;

        jsContext.executeJavaScript("" +
                "var response = await page.goto('" + url + "');\n");
        this.url = url;
        return this;
    }

    /**
     * Returns a copy of the currently loaded html document. <br>
     */
    public Document getDocument() {
        String rawHtml = jsContext.executeJavaScriptAndGetResult("" +
                "var result = await page.evaluate(() => document.body.innerHTML);\n");
        return Jsoup.parse(rawHtml);
    }

    public String getTitle() {
        return jsContext.executeJavaScriptAndGetResult("" +
                "var result = await page.title();\n");
    }

    /**
     * Executes the provided JavaScript code in the current pages/windows context. <br>
     * If you want to execute JavaScript code in the current Node.js context however use {@link NodeContext#executeJavaScript(String)}. <br>
     * Note that the current {@link NodeContext} must have been initialised with a debugOutputStream to see JavaScript console output. <br>
     */
    public PuppeteerWindow executeJS(String jsCode) throws NodeJsCodeException {
        jsContext.executeJavaScript("await page.evaluate(() => {\n" +
                jsCode +
                "});\n");
        return this;
    }


    /**
     * See {@link #makeScreenshot(File, boolean)} for details.
     */
    public PuppeteerWindow makeScreenshot(String filePath, boolean captureFullPage) throws IOException, NodeJsCodeException {
        return makeScreenshot(new File(filePath), captureFullPage);
    }

    /**
     * Takes a screenshot of the currently loaded page and saves it to the provided file. <br>
     *
     * @param file            should be a .png file. If not created yet gets created. <br>
     * @param captureFullPage should the complete page (top to bottom) be captured, or only the currently visible part?
     */
    public PuppeteerWindow makeScreenshot(File file, boolean captureFullPage) throws IOException, NodeJsCodeException {
        if (!file.exists()) file.createNewFile();
        String path = file.getAbsolutePath().replace("\\", "/"); // Windows paths don't work that's why we do this
        jsContext.executeJavaScript("await page.screenshot({ path: '" + path + "', fullPage: " + captureFullPage + " });");
        return this;
    }

    public PuppeteerWindow setScreenSize(String width, String height) throws NodeJsCodeException {
        return setScreenSize(Integer.parseInt(width), Integer.parseInt(height));
    }

    public PuppeteerWindow setScreenSize(int width, int height) throws NodeJsCodeException {
        jsContext.executeJavaScript("await page.setViewport({ width: " + width + ", height: " + height + " })");
        return this;
    }

    /**
     * See {@link #setCookie(String, String, String, boolean, boolean)} for details.
     */
    public PuppeteerWindow setCookie(HttpCookie cookie) throws MalformedURLException, NodeJsCodeException {
        return setCookie(cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.isHttpOnly(), cookie.getSecure());
    }

    /**
     * Note that this also works before loading a page. <br>
     *
     * @param urlOrDomain Can be the domain or complete url. Example: https://example.com or .example.com
     */
    public PuppeteerWindow setCookie(String name, String value, String urlOrDomain, boolean isHttpOnly, boolean isSecure) throws MalformedURLException, NodeJsCodeException {
        String domain;
        URL url;
        if (urlOrDomain.contains("/")) {
            url = new URL(urlOrDomain);
            domain = "." + url.getAuthority();
        } else {
            domain = urlOrDomain;
            url = new URL("https//" + domain + "/");
        }
        jsContext.executeJavaScript("" +
                "var cookie = {\n" +
                "  name: '" + name + "',\n" +
                "  value: '" + value + "',\n" +
                "  domain: '" + domain + "',\n" +
                "  url: '" + url + "',\n" +
                "  path: '" + url.getPath() + "',\n" +
                "  httpOnly: " + isHttpOnly + ",\n" +
                "  secure: " + isSecure + "\n" +
                "}" +
                "await page.setCookie(cookie)");
        return this;
    }

    public List<HttpCookie> getCookies() throws NodeJsCodeException, IOException {
        return getCookies((String[]) null);
    }

    /**
     * If no URLs are specified, this method returns cookies for the current page URL. If URLs are specified, only cookies for those URLs are returned.
     */
    public List<HttpCookie> getCookies(String... urls) throws NodeJsCodeException, IOException {
        String jsCodeForConvertingCookiesToPlainText = "" +
                "var result = ''\n" +
                "var cookie = null;\n" +
                "for (var i = 0; i < cookiesArray.length; i++) {\n" +
                "    cookie = cookiesArray[i];\n" +
                "    result = result + cookie.name +'\\n';\n" + // \\n equals \n in jsCode
                "    result = result + cookie.value +'\\n';\n" +
                "    result = result + cookie.domain +'\\n';\n" +
                "    result = result + cookie.path +'\\n';\n" +
                "    result = result + cookie.expires +'\\n';\n" +
                "    result = result + cookie.size +'\\n';\n" +
                "    result = result + cookie.httpOnly +'\\n';\n" +
                "    result = result + cookie.secure +'\\n';\n" +
                "    result = result + cookie.session +'\\n';\n" +
                "    result = result + cookie.sameSite +'\\n';\n" +
                "}";
        String rawCookies;
        if (urls == null || urls.length == 0) {
            rawCookies = jsContext.executeJavaScriptAndGetResult("" +
                    "var cookiesArray = await page.cookies();\n" +
                    jsCodeForConvertingCookiesToPlainText);
        } else {
            String urlsString = "";
            for (int i = 0; i < urls.length; i++) {
                String url = urls[i];
                if (i != (urls.length - 1)) {
                    urlsString = urlsString + url + ", ";
                } else
                    urlsString = urlsString + url;
            }
            rawCookies = jsContext.executeJavaScriptAndGetResult("" +
                    "var cookiesArray = await page.cookies(" + urlsString + ");\n" +
                    jsCodeForConvertingCookiesToPlainText);
        }

        // Read the cookies in plain text and convert them to actual Java HttpCookie objects:
        String line;
        int lineIndex = -1;
        HttpCookie cookie = null;
        List<HttpCookie> cookies = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new StringReader(rawCookies))) {
            while ((line = br.readLine()) != null) {
                lineIndex++;
                if (lineIndex == 0) cookie = new HttpCookie(line, null);
                else if (lineIndex == 1) cookie.setValue(line);
                else if (lineIndex == 2) cookie.setDomain(line);
                else if (lineIndex == 3) cookie.setPath(line);
                else if (lineIndex == 4) cookie.setMaxAge((long) (0L + Double.parseDouble(line)));
                else if (lineIndex == 5) ; // There is no method for setting the 'size' in java
                else if (lineIndex == 6) cookie.setHttpOnly(Boolean.parseBoolean(line));
                else if (lineIndex == 7) cookie.setSecure(Boolean.parseBoolean(line));
                else if (lineIndex == 8) ; // There is no method for setting the 'session' in java
                else if (lineIndex == 9) { // There is no method for setting the 'sameSite' in java
                    lineIndex = -1;
                    cookies.add(cookie);
                }
            }
        }
        return cookies;
    }

    public PuppeteerWindow printCookiesAsJsonArray() throws NodeJsCodeException, IOException {
        return printCookiesAsJsonArray(System.out);
    }

    public PuppeteerWindow printCookiesAsJsonArray(PrintStream out) throws NodeJsCodeException, IOException {
        out.println(new GsonBuilder().setPrettyPrinting().create().toJson(getCookiesAsJsonArray()));
        return this;
    }

    public JsonArray getCookiesAsJsonArray() throws NodeJsCodeException, IOException {
        JsonArray array = new JsonArray();
        for (HttpCookie cookie :
                getCookies()) {
            JsonObject obj = new JsonObject();
            array.add(obj);
            obj.addProperty("name", cookie.getName());
            obj.addProperty("value", cookie.getValue());
            obj.addProperty("domain", cookie.getDomain());
            obj.addProperty("path", cookie.getPath());
            obj.addProperty("max_age", cookie.getMaxAge());
            obj.addProperty("http_only", cookie.isHttpOnly());
            obj.addProperty("secure", cookie.getSecure());
        }
        return array;
    }

    /**
     * List of all available devices here: <br>
     * https://github.com/puppeteer/puppeteer/blob/main/src/common/DeviceDescriptors.ts
     */
    public PuppeteerWindow setDevice(String deviceName) throws NodeJsCodeException {
        jsContext.executeJavaScript("var device = puppeteer.devices['" + deviceName + "'];\n" +
                "await page.emulate(device);\n");
        return this;
    }

    /**
     * Performs one left-click on the element found by the selector. <br>
     * See {@link #click(String, String, int, int)} for details. <br>
     */
    public PuppeteerWindow leftClick(String selector) throws NodeJsCodeException {
        return click(selector, "left");
    }

    /**
     * Performs one right-click on the element found by the selector. <br>
     * See {@link #click(String, String, int, int)} for details. <br>
     */
    public PuppeteerWindow rightClick(String selector) throws NodeJsCodeException {
        return click(selector, "right");
    }

    /**
     * Performs one middle-click on the element found by the selector. <br>
     * See {@link #click(String, String, int, int)} for details. <br>
     */
    public PuppeteerWindow middleClick(String selector) throws NodeJsCodeException {
        return click(selector, "middle");
    }

    /**
     * See {@link #click(String, String, int, int)} for details.
     */
    public PuppeteerWindow click(String selector, String type) throws NodeJsCodeException {
        int max = 1000;
        int min = 200;
        return click(selector, type, 1, new Random().nextInt(max + 1 - min) + min);
    }

    /**
     * Performs an actual click on the specified element.
     *
     * @param selector   A selector to search for the element to click. If there are multiple elements satisfying the selector, the first will be clicked.
     *                   More infos about selectors here: <br>
     *                   https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_Selectors
     * @param type       "left", "right" or "middle"
     * @param clickCount the amount of clicks.
     * @param delay      the time to wait between mousedown and mouseup in milliseconds.
     */
    public PuppeteerWindow click(String selector, String type, int clickCount, int delay) throws NodeJsCodeException {
        jsContext.executeJavaScript("var options = {\n" +
                "button: '" + type + "',\n" +
                "clickCount: " + clickCount + ",\n" +
                "delay: " + delay + "\n" +
                "}\n" +
                "await page.click(" + selector + ", options);\n");
        return this;
    }

    /*
    TODO Somehow causes the code to freeze thus currently disabled:
    public int getStatusCode() throws NodeJsCodeException {
        return Integer.parseInt(jsContext.executeJavaScriptAndGetResult("" +
                "var result = await currentPageResponse.headers().status;\n"));
    }
     */

    /**
     * TODO
     * Downloads the currently loaded page/resource to the specified file. <br>
     * Creates the file if not existing. <br>
     * <p>
     * public NodeWindow download(File downloadedFile) throws NodeJsCodeException, IOException {
     * <p>
     * return download(url, downloadedFile);
     * }
     */

    public PuppeteerWindow download(String url) throws NodeJsCodeException {
        executeJS("var downloadWindow = window.open('" + url + "');\n" +
                "downloadWindow.focus();\n");
        return this;
    }

    /**
     * TODO
     * Note that the url won't get loaded into the current window.
     * <p>
     * public NodeWindow download(String url, File downloadedFile) throws NodeJsCodeException, IOException {
     * String filePath = downloadedFile.getAbsolutePath().replace("\\", "/");
     * jsContext.executeJavaScript("downloadFile = '"+filePath+"';");
     * load(url);
     * jsContext.executeJavaScript("downloadFile = null;");
     * <p>
     * jsContext.executeJavaScript("" +
     * "" +
     * "var buffer = await response.buffer();\n" +
     * "fs.writeFileSync(\""+filePath+"\", buffer);");
     * <p>
     * <p>
     * return this;
     * }
     */

    public HBrowser getParentBrowser() {
        return parentBrowser;
    }

    public void setParentBrowser(HBrowser parentBrowser) {
        this.parentBrowser = parentBrowser;
    }

    public boolean isEnableJavaScript() {
        return enableJavaScript;
    }

    public PuppeteerWindow setEnableJavaScript(boolean enableJavaScript) throws NodeJsCodeException {
        jsContext.executeJavaScript("await page.setJavaScriptEnabled(" + enableJavaScript + ");");
        this.enableJavaScript = enableJavaScript;
        return this;
    }

    public NodeContext getJsContext() {
        return jsContext;
    }

    public OutputStream getDebugOutput() {
        return debugOutput;
    }

    /**
     * Returns "chrome" or "firefox".
     */
    public String getBrowserType() {
        return jsContext.executeJavaScriptAndGetResult("var result = puppeteer.product;");
    }

    public String getUrl() {
        return url;
    }

    public boolean isHeadless() {
        return isHeadless;
    }

    public File getUserDataDir() {
        return userDataDir;
    }

    public boolean isDevTools() {
        return isDevTools;
    }

    public int getDebuggingPort() {
        return debuggingPort;
    }

    public String[] getAdditionalStartupArgs() {
        return additionalStartupArgs;
    }
}