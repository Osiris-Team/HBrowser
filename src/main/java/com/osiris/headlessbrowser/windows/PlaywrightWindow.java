package com.osiris.headlessbrowser.windows;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.osiris.headlessbrowser.HBrowser;
import com.osiris.headlessbrowser.data.chrome.ChromeHeaders;
import com.osiris.headlessbrowser.exceptions.NodeJsCodeException;
import com.osiris.headlessbrowser.js.contexts.NodeContext;
import com.osiris.headlessbrowser.js.raw.EvasionsInside;
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

/**
 * Headless-Window with Node.js/V8 as JavaScript engine wrapping the Playwright JS API.
 *
 * @author Osiris-Team
 */
public class PlaywrightWindow implements HWindow {
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
     * <p style="color: red;">Note that this is not the recommended way of creating the window object.</p>
     * Use the {@link WindowBuilder} instead. The {@link HBrowser} has a shortcut method for creating custom windows: {@link HBrowser#openCustomWindow()}.
     */
    public PlaywrightWindow(HBrowser parentBrowser, boolean enableJavaScript, OutputStream debugOutput, int jsTimeout,
                            boolean isHeadless, File userDataDir, boolean isDevTools, boolean makeUndetectable) {
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
                            "var page = null;\n" +
                            "var response = null;\n", 30, false);

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
                    "browserCtx = await playwright['chromium'].launchPersistentContext('" + userDataDir.getAbsolutePath().replace("\\", "/") + "', {\n" +
                            "  acceptDownloads: true,\n" +
                            "  headless : " + isHeadless + ",\n" +
                            "  javaScriptEnabled: " + enableJavaScript + ",\n" +
                            //"  downloadsPath: '" + downloadTempDir.getAbsolutePath().replace("\\", "/") + "',\n" + // Active issue at: https://github.com/microsoft/playwright/issues/9279
                            "  devtools: " + isDevTools + ",\n" +
                            //"  ignoreDefaultArgs: true,\n" +
                            "  args: ['--disable-blink-features=AutomationControlled'],\n" + // '--enable-automation=false'
                            "  extraHTTPHeaders: " + new GsonBuilder().setPrettyPrinting().create().toJson(new ChromeHeaders().getJson()) + ",\n" +
                            "  userAgent: '" + new ChromeHeaders().user_agent + "'\n" + // Just to make sure...
                            "});\n" +
                            "browser = browserCtx.browser();\n" +
                            "page = await browserCtx.newPage();\n", 30, false);

            if (makeUndetectable) {
                EvasionsInside evasionsInside = new EvasionsInside();
                jsContext.executeJavaScript("" +
                        "await page.evaluateOnNewDocument(() => {\n" +
                        evasionsInside.getAll() +
                        "\n});\n");
            }

            /*
            if (makeUndetectable) {
                jsContext.executeJavaScript("" +
                        "  // Credits go to: https://intoli.com/blog/not-possible-to-block-chrome-headless/\n" +
                        "  // Pass the Webdriver Test.\n" +
                        "await page.addInitScript(() => {\n" +
                        "    Object.defineProperty(navigator, 'webdriver', {\n" +
                        "      get: () => false,\n" +
                        "    });\n" +
                        "    if(navigator.webdriver) throw new Error('Failed to set navigator.webdriver to false!');\n" +
                        "    window.chrome = {\n" +
                        "  \"app\": {\n" +
                        "    \"isInstalled\": false,\n" +
                        "    \"InstallState\": {\n" +
                        "      \"DISABLED\": \"disabled\",\n" +
                        "      \"INSTALLED\": \"installed\",\n" +
                        "      \"NOT_INSTALLED\": \"not_installed\"\n" +
                        "    },\n" +
                        "    \"RunningState\": {\n" +
                        "      \"CANNOT_RUN\": \"cannot_run\",\n" +
                        "      \"READY_TO_RUN\": \"ready_to_run\",\n" +
                        "      \"RUNNING\": \"running\"\n" +
                        "    }\n" +
                        "  },\n" +
                        "  \"runtime\": {\n" +
                        "    \"OnInstalledReason\": {\n" +
                        "      \"CHROME_UPDATE\": \"chrome_update\",\n" +
                        "      \"INSTALL\": \"install\",\n" +
                        "      \"SHARED_MODULE_UPDATE\": \"shared_module_update\",\n" +
                        "      \"UPDATE\": \"update\"\n" +
                        "    },\n" +
                        "    \"OnRestartRequiredReason\": {\n" +
                        "      \"APP_UPDATE\": \"app_update\",\n" +
                        "      \"OS_UPDATE\": \"os_update\",\n" +
                        "      \"PERIODIC\": \"periodic\"\n" +
                        "    },\n" +
                        "    \"PlatformArch\": {\n" +
                        "      \"ARM\": \"arm\",\n" +
                        "      \"ARM64\": \"arm64\",\n" +
                        "      \"MIPS\": \"mips\",\n" +
                        "      \"MIPS64\": \"mips64\",\n" +
                        "      \"X86_32\": \"x86-32\",\n" +
                        "      \"X86_64\": \"x86-64\"\n" +
                        "    },\n" +
                        "    \"PlatformNaclArch\": {\n" +
                        "      \"ARM\": \"arm\",\n" +
                        "      \"MIPS\": \"mips\",\n" +
                        "      \"MIPS64\": \"mips64\",\n" +
                        "      \"X86_32\": \"x86-32\",\n" +
                        "      \"X86_64\": \"x86-64\"\n" +
                        "    },\n" +
                        "    \"PlatformOs\": {\n" +
                        "      \"ANDROID\": \"android\",\n" +
                        "      \"CROS\": \"cros\",\n" +
                        "      \"LINUX\": \"linux\",\n" +
                        "      \"MAC\": \"mac\",\n" +
                        "      \"OPENBSD\": \"openbsd\",\n" +
                        "      \"WIN\": \"win\"\n" +
                        "    },\n" +
                        "    \"RequestUpdateCheckStatus\": {\n" +
                        "      \"NO_UPDATE\": \"no_update\",\n" +
                        "      \"THROTTLED\": \"throttled\",\n" +
                        "      \"UPDATE_AVAILABLE\": \"update_available\"\n" +
                        "    }\n" +
                        "  }\n" +
                        "}\n" +
                        " // Overwrite the `plugins` property to use a custom getter.\n" +
                        "    Object.defineProperty(navigator, 'plugins', {\n" +
                        "      // This just needs to have `length > 0` for the current test,\n" +
                        "      // but we could mock the plugins too if necessary.\n" +
                        "      get: () => [1, 2, 3, 4, 5],\n" +
                        "    });\n" +
                        "try{ // Inside try/catch to ensure variables are only accessible from here" +
                        "  var originalQuery = window.navigator.permissions.query;\n" +
                        "  return window.navigator.permissions.query = (parameters) => (\n" +
                        "    parameters.name === 'notifications' ?\n" +
                        "      Promise.resolve({ state: Notification.permission }) :\n" +
                        "      originalQuery(parameters)\n" +
                        "  );\n" +
                        "}catch(e){throw e;}\n" +
                        "  });\n");
            }

             */
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public PlaywrightWindow load(String url) throws NodeJsCodeException {
        if (!url.startsWith("http"))
            url = "https://" + url;

        jsContext.executeJavaScript("" +
                "response = await page.goto('" + url + "');\n");
        this.url = url;
        return this;
    }

    /**
     * Load html from a file.
     */
    public PlaywrightWindow load(File file) throws NodeJsCodeException {
        String url = "file:///" + file.getAbsolutePath().replace("\\", "/");
        jsContext.executeJavaScript("" +
                "response = await page.goto('" + url + "');\n");
        this.url = url;
        return this;
    }

    /**
     * Returns the response headers represented in a Json object like this:
     * <pre>
     *     {
     *         header_name: header_value,
     *         ...
     *     }
     * </pre>
     */
    public JsonObject getResponseHeaders() {
        String rawJson = jsContext.executeJSAndGetResult("" +
                "var result = await response.allHeaders();\n" +
                "result = JSON.stringify(result);\n");
        return new Gson().fromJson(rawJson, JsonObject.class);
    }

    public int getStatusCode() {
        String raw = jsContext.executeJSAndGetResult("" +
                "var result = '' + response.status();\n");
        return Integer.parseInt(raw);
    }

    public String getStatusText() {
        return jsContext.executeJSAndGetResult("" +
                "var result = response.statusText();\n");
    }

    /**
     * Note that a response must have been received first for this to work. <br>
     * Returns the request headers represented in a Json object like this:
     * <pre>
     *     {
     *         header_name: header_value,
     *         ...
     *     }
     * </pre>
     */
    public JsonObject getRequestHeaders() {
        String rawJson = jsContext.executeJSAndGetResult("" +
                "var result = await response.request().allHeaders();\n" +
                "result = JSON.stringify(result);\n");
        return new Gson().fromJson(rawJson, JsonObject.class);
    }

    /**
     * Note that a response must have been received first for this to work. <br>
     * Request's method (GET, POST, etc.). <br>
     */
    public String getRequestMethod() {
        return jsContext.executeJSAndGetResult("" +
                "var result = response.request().method();\n");
    }

    /**
     * Note that a response must have been received first for this to work <br>
     * Request's post body as String, if any. <br>
     */
    public String getRequestPostData() {
        return jsContext.executeJSAndGetResult("" +
                "var result = response.request().postData();\n");
    }


    /**
     * Note that this returns a copy and not the actual file, <br>
     * which means that changes done to the real html after returning this won't be reflected in the copy. <br>
     */
    public Document getOuterHtml() {
        String rawHtml = jsContext.executeJSAndGetResult("" +
                "var result = await page.evaluate(() => document.getElementsByTagName(\"html\")[0].outerHTML);\n");
        return Jsoup.parse(rawHtml);
    }

    /**
     * Note that this returns a copy and not the actual file, <br>
     * which means that changes done to the real html after returning this won't be reflected in the copy. <br>
     */
    public Document getInnerHtml() {
        String rawHtml = jsContext.executeJSAndGetResult("" +
                "var result = await page.evaluate(() => document.getElementsByTagName(\"html\")[0].innerHTML);\n");
        return Jsoup.parse(rawHtml);
    }

    /**
     * Note that this returns a copy and not the actual file, <br>
     * which means that changes done to the real html after returning this won't be reflected in the copy. <br>
     */
    public Document getHeadOuterHtml() {
        String rawHtml = jsContext.executeJSAndGetResult("" +
                "var result = await page.evaluate(() => document.head.outerHTML);\n");
        return Jsoup.parse(rawHtml);
    }

    /**
     * Note that this returns a copy and not the actual file, <br>
     * which means that changes done to the real html after returning this won't be reflected in the copy. <br>
     */
    public Document getHeadInnerHtml() {
        String rawHtml = jsContext.executeJSAndGetResult("" +
                "var result = await page.evaluate(() => document.head.innerHTML);\n");
        return Jsoup.parse(rawHtml);
    }

    /**
     * Note that this returns a copy and not the actual file, <br>
     * which means that changes done to the real html after returning this won't be reflected in the copy. <br>
     */
    public Document getBodyOuterHtml() {
        String rawHtml = jsContext.executeJSAndGetResult("" +
                "var result = await page.evaluate(() => document.body.outerHTML);\n");
        return Jsoup.parse(rawHtml);
    }

    /**
     * Note that this returns a copy and not the actual file, <br>
     * which means that changes done to the real html after returning this won't be reflected in the copy. <br>
     */
    public Document getBodyInnerHtml() {
        String rawHtml = jsContext.executeJSAndGetResult("" +
                "var result = await page.evaluate(() => document.body.innerHTML);\n");
        return Jsoup.parse(rawHtml);
    }

    /**
     * Executes the provided JavaScript code in the current pages/windows context. <br>
     * If you want to execute JavaScript code in the current Node.js context however use {@link NodeContext#executeJavaScript(String)}. <br>
     * Note that the current {@link NodeContext} must have been initialised with a debugOutputStream to see JavaScript console output. <br>
     */
    public PlaywrightWindow executeJS(String jsCode) throws NodeJsCodeException {
        jsContext.executeJavaScript("await page.evaluate(`\n" +
                jsCode +
                "`);\n");
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
     * Downloads a file from the specified url.
     *
     * @param url  not null. The download url.
     * @param dest if null file gets downloaded to {@link #downloadTempDir}, otherwise to the provided destination/file.
     */
    public PlaywrightWindow download(String url, File dest) throws IOException {
        String[] results = jsContext.parseJSStringArrayToJavaStringArray(
                jsContext.executeJSAndGetResult("" +
                        "  await page.goto('about:blank');\n" +
                        "  var event = page.waitForEvent('download');\n" +
                        "  await page.evaluate(`var myCUSTel = document.createElement('a');myCUSTel.innerHTML = 'Download';myCUSTel.setAttribute('href', '" + url + "');myCUSTel.setAttribute('id', 'myCUSTel');document.getElementsByTagName('body')[0].appendChild(myCUSTel);`);\n" +
                        "  await page.click('id=myCUSTel');\n" +
                        "  var download = await event;\n" +
                        "  console.log(event);\n" +
                        "  var downloadError = await download.failure();\n" +
                        "  if (downloadError != null) throw new Error(downloadError);\n" +
                        "  var downloadPath = await download.path();\n" +
                        "  var downloadFileName = await download.suggestedFilename();\n" +
                        "  var result = downloadPath+','+downloadFileName;\n"));
        File download = new File(results[0]);
        String fileName = results[1]; // Contains extension
        /*
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

         */

        if (dest != null) {
            if (dest.exists()) dest.delete();
            Files.copy(download.toPath(), dest.toPath());
        } else {
            Files.copy(download.toPath(), new File(downloadTempDir + "/" + fileName).toPath());
        }
        download.delete();
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
            url = new URL("https://" + domain + "/");
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
            rawCookies = jsContext.executeJSAndGetResult("" +
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
            rawCookies = jsContext.executeJSAndGetResult("" +
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

    public PlaywrightWindow setScreenSize(String width, String height) throws NodeJsCodeException {
        return setScreenSize(Integer.parseInt(width), Integer.parseInt(height));
    }

    public PlaywrightWindow setScreenSize(int width, int height) throws NodeJsCodeException {
        jsContext.executeJavaScript("await page.setViewportSize({ width: " + width + ", height: " + height + " })");
        return this;
    }

    /**
     * See {@link #makeScreenshot(File, boolean)} for details.
     */
    public PlaywrightWindow makeScreenshot(String filePath, boolean captureFullPage) throws IOException, NodeJsCodeException {
        return makeScreenshot(new File(filePath), captureFullPage);
    }

    /**
     * Takes a screenshot of the currently loaded page and saves it to the provided file. <br>
     *
     * @param file            should be a .png file. If not created yet gets created. <br>
     * @param captureFullPage should the complete page (top to bottom) be captured, or only the currently visible part?
     */
    public PlaywrightWindow makeScreenshot(File file, boolean captureFullPage) throws IOException, NodeJsCodeException {
        if (file.exists()) file.delete();
        file.createNewFile();
        String path = file.getAbsolutePath().replace("\\", "/"); // Windows paths don't work that's why we do this
        jsContext.executeJavaScript("await page.screenshot({ path: '" + path + "', fullPage: " + captureFullPage + " });");
        return this;
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
        int max = 200;
        int min = 50;
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
        jsContext.executeJavaScript(""+
                "await page.click('" + selector + "', {button: '"+type+"', clickCount: "+clickCount+", delay:"+delay+"});\n");
        return this;
    }

    /**
     * Fills form fields. <br>
     * Defaults used: force=false,noWaitAfter=false,strict=false,timeout=30000. <br>
     * See {@link #fill(String, String, boolean, boolean, boolean, int)} for details. <br>
     *
     * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
     * @param value    Value to fill for the input, textarea or [contenteditable] element.
     */
    public PlaywrightWindow fill(String selector, String value) throws NodeJsCodeException {
        fill(selector, value, false, false, false, 30000);
        return this;
    }

    /**
     * Fills form fields. <br>
     * Note that strict is set to true which means the operation will fail if the selector returns more than one element. <br>
     * Defaults used: force=false,noWaitAfter=false,strict=true,timeout=30000. <br>
     * See {@link #fill(String, String, boolean, boolean, boolean, int)} for details. <br>
     *
     * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
     * @param value    Value to fill for the input, textarea or [contenteditable] element.
     */
    public PlaywrightWindow fillStrict(String selector, String value) throws NodeJsCodeException {
        fill(selector, value, false, false, true, 30000);
        return this;
    }

    /**
     * Fills form fields.
     *
     * @param selector    A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used. <br><br>
     * @param value       Value to fill for the input, textarea or [contenteditable] element. <br><br>
     * @param force       Whether to bypass the actionability checks. <br><br>
     * @param noWaitAfter Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to inaccessible pages. <br><br>
     * @param strict      When true, the call requires selector to resolve to a single element. If given selector resolves to more then one element, the call throws an exception. <br><br>
     * @param timeout     Maximum time in milliseconds, defaults to 30 seconds, pass 0 to disable timeout. The default value can be changed by using the browserContext.setDefaultTimeout(timeout) or page.setDefaultTimeout(timeout) methods. <br><br>
     */
    public PlaywrightWindow fill(String selector, String value,
                                 boolean force, boolean noWaitAfter, boolean strict, int timeout) throws NodeJsCodeException {
        jsContext.executeJavaScript("await page.fill('" + selector + "', '" + value + "', {force: " + force
                + ",noWaitAfter:" + noWaitAfter + ",strict:" + strict + ",timeout:" + timeout + "});");
        return this;
    }

    /**
     * Checks or unchecks a checkbox or radio button.
     *
     * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
     * @param checked  Whether to check or uncheck the checkbox.
     */
    public PlaywrightWindow setChecked(String selector, boolean checked) throws NodeJsCodeException {
        jsContext.executeJavaScript("await page.setChecked('" + selector + "', " + checked + ");");
        return this;
    }

    /**
     * Types the provided text with a random 100ms-300ms delay between each type, to simulate a real user.
     */
    public PlaywrightWindow typeReal(String text) throws NodeJsCodeException {
        int max = 300;
        int min = 100;
        type(text, new Random().nextInt(max + 1 - min) + min);
        return this;
    }

    /**
     * Types the provided text with a 0ms delay between each type.
     */
    public PlaywrightWindow type(String text) throws NodeJsCodeException {
        type(text, 0);
        return this;
    }

    /**
     * Types the provided text with the provided delay in ms between each type.
     */
    public PlaywrightWindow type(String text, int delay) throws NodeJsCodeException {
        jsContext.executeJavaScript("await page.keyboard.type('" + text + "', {delay:" + delay + "});");
        return this;
    }

    /**
     * Holds down the provided key until {@link #releaseKey(String)} is called. <br>
     * Full list of key names <a href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key/Key_Values">here</a>.
     */
    public PlaywrightWindow holdKey(String key) throws NodeJsCodeException {
        jsContext.executeJavaScript("await page.keyboard.down('" + key + "');");
        return this;
    }

    /**
     * Releases the provided key. Note that {@link #holdKey(String)} must have been called before on this key, for this to work. <br>
     * Full list of key names <a href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key/Key_Values">here</a>.
     */
    public PlaywrightWindow releaseKey(String key) throws NodeJsCodeException {
        jsContext.executeJavaScript("await page.keyboard.up('" + key + "');");
        return this;
    }

    /**
     * Press the provided key and hold for the provided milliseconds. <br>
     * There are better methods for typing longer texts though: {@link #type(String)} or {@link #typeReal(String)}. <br>
     * Full list of key names <a href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key/Key_Values">here</a>.
     */
    public PlaywrightWindow pressKey(String key, int ms) throws NodeJsCodeException {
        jsContext.executeJavaScript("await page.keyboard.press('" + key + "', {delay:" + ms + "});");
        return this;
    }

    @Override
    public void close() throws Exception {
        // Running js: browser.close() here causes a weird exception: https://github.com/isaacs/rimraf/issues/221
        // Since it's not mandatory we just don't do it.
        jsContext.close();
    }

}
