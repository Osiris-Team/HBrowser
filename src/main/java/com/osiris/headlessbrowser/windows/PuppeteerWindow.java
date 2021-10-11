package com.osiris.headlessbrowser.windows;


import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.osiris.headlessbrowser.HBrowser;
import com.osiris.headlessbrowser.data.chrome.ChromeHeaders;
import com.osiris.headlessbrowser.exceptions.NodeJsCodeException;
import com.osiris.headlessbrowser.js.contexts.NodeContext;
import com.osiris.headlessbrowser.js.raw.EvasionsInside;
import com.osiris.headlessbrowser.js.raw.EvasionsOutside;
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
    final NodeContext jsContext;
    final OutputStream debugOutput;
    final boolean isHeadless;
    final File userDataDir;
    final boolean isDevTools;
    final int debuggingPort;
    final String[] additionalStartupArgs;
    HBrowser parentBrowser;
    boolean enableJavaScript;
    String url;

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
     * @param makeUndetectable      Makes this window indistinguishable from 'real', user operated windows, by using the npm packages puppeteer-extra and puppeteer-extra-plugin-stealth. <br><br>
     * @param additionalStartupArgs Default is null. Additional arguments to pass to the browser instance. The list of Chromium flags can be found here: https://peter.sh/experiments/chromium-command-line-switches/ <br><br>
     */
    public PuppeteerWindow(HBrowser parentBrowser, boolean enableJavaScript, OutputStream debugOutput, int jsTimeout,
                           boolean isHeadless, File userDataDir, boolean isDevTools, int debuggingPort, boolean makeUndetectable, String... additionalStartupArgs) {
        this.parentBrowser = parentBrowser;
        this.debugOutput = debugOutput;
        this.isHeadless = isHeadless;
        this.userDataDir = userDataDir;
        this.isDevTools = isDevTools;
        this.debuggingPort = debuggingPort;
        this.additionalStartupArgs = additionalStartupArgs;
        try {
            this.jsContext = new NodeContext(new File(userDataDir.getParentFile() + "/node-js"), debugOutput, jsTimeout);

            // Define global variables/constants
            if (makeUndetectable) {
                /* // Doesnt work
                jsContext.npmInstall("puppeteer");
                jsContext.npmInstall("puppeteer-extra");
                jsContext.npmInstall("puppeteer-extra-plugin-stealth");
                jsContext.executeJavaScript(
                        "const puppeteer = require('puppeteer-extra');\n" +
                                "const StealthPlugin = require('puppeteer-extra-plugin-stealth');\n" +
                                "puppeteer.use(StealthPlugin());\n" +
                                "var browser = null;\n" +
                                "var page = null;\n" +
                                "var downloadFile = null;\n", 30, false);
                 */
            } else {

            }
            jsContext.npmInstall("puppeteer");
            jsContext.executeJavaScript(
                    "const puppeteer = require('puppeteer');\n" +
                            "var browser = null;\n" +
                            "var page = null;\n" +
                            "var downloadFile = null;\n", 30, false);


            StringBuilder jsInitCode = new StringBuilder();
            jsInitCode.append("var defaultArgs = {\n");
            jsInitCode.append("  headless : " + isHeadless + ",\n");
            List<String> argsAsList = new ArrayList<>();
            if (additionalStartupArgs != null) {
                argsAsList.addAll(Arrays.asList(additionalStartupArgs));
            }
            if (!argsAsList.contains("--disable-blink-features=AutomationControlled"))
                argsAsList.add("--disable-blink-features=AutomationControlled");
            jsInitCode.append("  args: " + jsContext.parseJavaListToJSArray(argsAsList) + ",\n");
            if (userDataDir == null) {
                userDataDir = new WindowBuilder(null).userDataDir; // Get the default value
            }
            if (userDataDir.isFile())
                throw new Exception("userDataDir must be a directory and cannot be a file (" + userDataDir.getAbsolutePath() + ")!");
            if (!userDataDir.exists()) userDataDir.mkdirs();
            jsInitCode.append("  userDataDir: \"" + userDataDir.getAbsolutePath().replace("\\", "/") + "\",\n");
            jsInitCode.append("  devtools: " + isDevTools + ",\n");
            jsInitCode.append("  debuggingPort: " + debuggingPort + "};\n");
            jsInitCode.append("var argsAsArray = puppeteer.defaultArgs(defaultArgs);\n");
            //if (makeUndetectable){
            //    jsInitCode.append(new EvasionsOutside().navigator_webdriver);
            //}
            jsInitCode.append("console.log(puppeteer.defaultArgs(defaultArgs));\n");
            jsInitCode.append("browser = await puppeteer.launch(defaultArgs);\n");
            jsInitCode.append("page = await browser.newPage();\n");
            jsInitCode.append("await page.setExtraHTTPHeaders(" + new GsonBuilder().setPrettyPrinting().create().toJson(new ChromeHeaders().getJson()) + ");\n");
            jsContext.executeJavaScript(jsInitCode.toString(), 30, false);
            setEnableJavaScript(enableJavaScript);


            if (makeUndetectable) {
                jsContext.executeJavaScript(new EvasionsOutside().getAll());
                EvasionsInside evasionsInside = new EvasionsInside();
                jsContext.executeJavaScript("" +
                        "await page.evaluateOnNewDocument(() => {\n" +
                        evasionsInside.getAll() +
                        "\n});\n");
            }
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
        String rawHtml = jsContext.executeJSAndGetResult("" +
                "var result = await page.evaluate(() => document.body.innerHTML);\n");
        return Jsoup.parse(rawHtml);
    }

    public String getTitle() {
        return jsContext.executeJSAndGetResult("" +
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
     * Note that the provided code must return a string variable. Example:
     * <pre>
     *     var result = 'no results yet!';
     *     //... do stuff
     *     return result;
     * </pre>
     * That variable gets then returned by this method.
     */
    public String executeJSAndGetResult(String jsCode) throws NodeJsCodeException {
        return jsContext.executeJSAndGetResult("var result = await page.evaluate(() => {\n" +
                jsCode + "\n" +
                "});\n");
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
        if (file.exists()) file.delete();
        file.createNewFile();
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
            rawCookies = jsContext.executeJSAndGetResult("" +
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
            rawCookies = jsContext.executeJSAndGetResult("" +
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

    public int getStatusCode() throws NodeJsCodeException {
        return Integer.parseInt(jsContext.executeJSAndGetResult("" +
                "var result = await currentPageResponse.headers().status;\n"));
    }

    /* TODO https://github.com/puppeteer/puppeteer/issues/7618
    // Current not supported due to puppeteer not supporting it
    public PuppeteerWindow download(String url) throws NodeJsCodeException {
        executeJS("var downloadWindow = window.open('" + url + "');\n" +
                "downloadWindow.focus();\n");
        return this;
    }*/

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
        return jsContext.executeJSAndGetResult("var result = puppeteer.product;");
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
