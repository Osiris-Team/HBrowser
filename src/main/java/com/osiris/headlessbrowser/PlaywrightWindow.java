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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlaywrightWindow implements AutoCloseable {
    private final NodeContext jsContext;
    private final OutputStream debugOutput;
    private final HBrowser parentBrowser;
    private final boolean isHeadless;
    private final File userDataDir;
    private final boolean isDevTools;
    private final File downloadTempDir;
    private boolean enableJavaScript;
    private String url;

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
                            "var browserCtx = null;\n" +
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
                    "browserCtx = await playwright['chromium'].launchPersistentContext('" + userDataDir.getAbsolutePath().replace("\\", "/") + "', " +
                            "{ acceptDownloads: true,\n" +
                            "  headless : " + isHeadless + ",\n" +
                            "  javaScriptEnabled: " + enableJavaScript + ",\n" +
                            //"  downloadsPath: '" + downloadTempDir.getAbsolutePath().replace("\\", "/") + "',\n" + // Active issue at: https://github.com/microsoft/playwright/issues/9279
                            "  devtools: " + isDevTools + "\n" +
                            "});\n" +
                            "browser = browserCtx.browser();\n" +
                            "page = await browserCtx.newPage();\n", 30, false);
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

    /**
     * Returns a copy of the currently loaded html document. <br>
     */
    public Document getDocument() {
        String rawHtml = jsContext.executeJavaScriptAndGetResult("" +
                "var result = await page.evaluate(() => document.body.innerHTML);\n");
        return Jsoup.parse(rawHtml);
    }

    /**
     * Executes the provided JavaScript code in the current pages/windows context. <br>
     * If you want to execute JavaScript code in the current Node.js context however use {@link NodeContext#executeJavaScript(String)}. <br>
     * Note that the current {@link NodeContext} must have been initialised with a debugOutputStream to see JavaScript console output. <br>
     */
    public PlaywrightWindow executeJS(String jsCode) throws NodeJsCodeException {
        jsContext.executeJavaScript("await page.evaluate(() => {\n" +
                jsCode +
                "});\n");
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
        if (dest != null) {
            if (download.exists()) download.delete();
            Files.copy(download.toPath(), dest.toPath());
            download.delete();
        }
        this.url = "about:blank";
        return this;
    }

    /**
     * See {@link #setCookie(String, String, String, boolean, boolean)} for details.
     */
    public PlaywrightWindow setCookie(HttpCookie cookie) throws MalformedURLException, NodeJsCodeException {
        return setCookie(cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.isHttpOnly(), cookie.getSecure());
    }

    /**
     * Note that this also works before loading a page. <br>
     *
     * @param urlOrDomain Can be the domain or complete url. Example: https://example.com or .example.com
     */
    public PlaywrightWindow setCookie(String name, String value, String urlOrDomain, boolean isHttpOnly, boolean isSecure) throws MalformedURLException, NodeJsCodeException {
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
                "await browserCtx.addCookies([cookie])");
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
                    "var cookiesArray = await browserCtx.cookies();\n" +
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
                    "var cookiesArray = await browserCtx.cookies(" + urlsString + ");\n" +
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

    public PlaywrightWindow printCookiesAsJsonArray() throws NodeJsCodeException, IOException {
        return printCookiesAsJsonArray(System.out);
    }

    public PlaywrightWindow printCookiesAsJsonArray(PrintStream out) throws NodeJsCodeException, IOException {
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
     * Performs one left-click on the element found by the selector. <br>
     * See {@link #click(String, String, int, int)} for details. <br>
     */
    public PlaywrightWindow leftClick(String selector) throws NodeJsCodeException {
        return click(selector, "left");
    }

    /**
     * Performs one right-click on the element found by the selector. <br>
     * See {@link #click(String, String, int, int)} for details. <br>
     */
    public PlaywrightWindow rightClick(String selector) throws NodeJsCodeException {
        return click(selector, "right");
    }

    /**
     * Performs one middle-click on the element found by the selector. <br>
     * See {@link #click(String, String, int, int)} for details. <br>
     */
    public PlaywrightWindow middleClick(String selector) throws NodeJsCodeException {
        return click(selector, "middle");
    }

    /**
     * See {@link #click(String, String, int, int)} for details.
     */
    public PlaywrightWindow click(String selector, String type) throws NodeJsCodeException {
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
    public PlaywrightWindow click(String selector, String type, int clickCount, int delay) throws NodeJsCodeException {
        jsContext.executeJavaScript("var options = {\n" +
                "button: '" + type + "',\n" +
                "clickCount: " + clickCount + ",\n" +
                "delay: " + delay + "\n" +
                "}\n" +
                "await page.click(" + selector + ", options);\n");
        return this;
    }

    @Override
    public void close() throws Exception {
        // Running js: browser.close() here causes a weird exception: https://github.com/isaacs/rimraf/issues/221
        // Since it's not mandatory we just don't do it.
        jsContext.close();
    }

}
