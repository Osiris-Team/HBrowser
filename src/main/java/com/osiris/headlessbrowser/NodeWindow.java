package com.osiris.headlessbrowser;


import com.osiris.headlessbrowser.data.chrome.ChromeHeaders;
import com.osiris.headlessbrowser.exceptions.NodeJsCodeException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Random;

/**
 * Headless-Window.
 *
 * @author Osiris-Team
 */
public class NodeWindow implements AutoCloseable {
    private final NodeContext jsContext;
    private HBrowser parentBrowser;
    private boolean enableJavaScript;
    private OutputStream debugOutput;

    public NodeWindow(HBrowser parentBrowser, boolean enableJavaScript, OutputStream debugOutput) {
        this.parentBrowser = parentBrowser;
        this.debugOutput = debugOutput;
        this.jsContext = new NodeContext(debugOutput);
        try {
            jsContext.npmInstall("puppeteer");
            jsContext.executeJavaScript("const puppeteer = require('puppeteer');\n" +
                    "const browser = await puppeteer.launch();\n" +
                    "const page = await browser.newPage();\n");
            setEnableJavaScript(enableJavaScript);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load the contents from the provided url into the current {@link NodeWindow}.
     *
     * @param url Examples: https://www.wikipedia.org or wikipedia.org.
     * @return the current {@link NodeWindow} for chained method calls.
     * @throws IOException
     */
    public NodeWindow load(String url) throws IOException, NodeJsCodeException {
        if (!url.startsWith("http"))
            url = "https://" + url;

        jsContext.executeJavaScript("await page.goto('" + url + "');");
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
     * Executes the provided JavaScript code in the current context. <br>
     * See {@link NodeContext#executeJavaScript(String)} for details. <br>
     */
    public NodeWindow executeJS(String jsCode) throws NodeJsCodeException {
        jsContext.executeJavaScript(jsCode);
        return this;
    }

    @Override
    public void close() throws Exception {
        // Running js: browser.close() here causes a weird exception: https://github.com/isaacs/rimraf/issues/221
        // Since it's not mandatory we just don't do it.
        jsContext.close();
    }

    /**
     * See {@link #makeScreenshot(File, boolean)} for details.
     */
    public NodeWindow makeScreenshot(String filePath, boolean captureFullPage) throws IOException, NodeJsCodeException {
        return makeScreenshot(new File(filePath), captureFullPage);
    }

    /**
     * Takes a screenshot of the currently loaded page and saves it to the provided file. <br>
     * @param file should be a .png file. If not created yet gets created. <br>
     * @param captureFullPage should the complete page (top to bottom) be captured, or only the currently visible part?
     */
    public NodeWindow makeScreenshot(File file, boolean captureFullPage) throws IOException, NodeJsCodeException {
        if (!file.exists()) file.createNewFile();
        String path = file.getAbsolutePath().replace("\\", "/"); // Windows paths don't work that's why we do this
        jsContext.executeJavaScript("await page.screenshot({ path: '"+path+"', fullPage: "+captureFullPage+" });");
        return this;
    }

    public NodeWindow setScreenSize(String width, String height) throws NodeJsCodeException {
        return setScreenSize(Integer.parseInt(width), Integer.parseInt(height));
    }

    public NodeWindow setScreenSize(int width, int height) throws NodeJsCodeException {
        jsContext.executeJavaScript("await page.setViewport({ width: "+width+", height: "+height+" })");
        return this;
    }

    /**
     * See {@link #setCookie(String, String, String, boolean, boolean)} for details.
     */
    public NodeWindow setCookie(HttpCookie cookie) throws MalformedURLException, NodeJsCodeException {
        return setCookie(cookie.getName(), cookie.getValue(), cookie.getDomain(), cookie.isHttpOnly(), cookie.getSecure());
    }

    /**
     * Note that this also works before loading a page. <br>
     * @param urlOrDomain Can be the domain or complete url. Example: https://example.com or .example.com
     */
    public NodeWindow setCookie(String name, String value, String urlOrDomain, boolean isHttpOnly, boolean isSecure) throws MalformedURLException, NodeJsCodeException {
        String domain;
        String url;
        if (urlOrDomain.contains("/")){
            url = urlOrDomain;
            domain = "."+new URL(url).getAuthority();
        }else{
            domain = urlOrDomain;
            url = "https//"+domain+"/";
        }
        jsContext.executeJavaScript("" +
                "var cookie = {\n" +
                "  name: '"+name+"',\n" +
                "  value: '"+value+"',\n" +
                "  domain: '"+domain+"',\n" +
                "  url: '"+url+"',\n" +
                "  path: '/',\n" +
                "  httpOnly: "+isHttpOnly+",\n" +
                "  secure: "+isSecure+"\n" +
                "}" +
                "await page.setCookie(cookie)");
        return this;
    }

    /**
     * List of all available devices here: <br>
     * https://github.com/puppeteer/puppeteer/blob/main/src/common/DeviceDescriptors.ts
     */
    public NodeWindow setDevice(String deviceName) throws NodeJsCodeException {
        jsContext.executeJavaScript("var device = puppeteer.devices['"+deviceName+"'];\n" +
                "await page.emulate(device);\n");
        return this;
    }

    /**
     * Performs one left-click on the element found by the selector. <br>
     * See {@link #click(String, String, int, int)} for details. <br>
     */
    public NodeWindow leftClick(String selector) throws NodeJsCodeException {
        return click(selector, "left");
    }

    /**
     * Performs one right-click on the element found by the selector. <br>
     * See {@link #click(String, String, int, int)} for details. <br>
     */
    public NodeWindow rightClick(String selector) throws NodeJsCodeException {
        return click(selector, "right");
    }

    /**
     * Performs one middle-click on the element found by the selector. <br>
     * See {@link #click(String, String, int, int)} for details. <br>
     */
    public NodeWindow middleClick(String selector) throws NodeJsCodeException {
        return click(selector, "middle");
    }

    /**
     * See {@link #click(String, String, int, int)} for details.
     */
    public NodeWindow click(String selector, String type) throws NodeJsCodeException {
        int max = 1000;
        int min = 200;
        return click(selector, type, 1, new Random().nextInt(max + 1 - min) + min);
    }

    /**
     * Performs an actual click on the specified element.
     * @param selector A selector to search for the element to click. If there are multiple elements satisfying the selector, the first will be clicked.
     *                 More infos about selectors here: <br>
     *                 https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_Selectors
     * @param type "left", "right" or "middle"
     * @param clickCount the amount of clicks.
     * @param delay the time to wait between mousedown and mouseup in milliseconds.
     */
    public NodeWindow click(String selector, String type, int clickCount, int delay) throws NodeJsCodeException {
        jsContext.executeJavaScript("var options = {\n" +
                "button: '"+type+"',\n" +
                "clickCount: "+clickCount+",\n" +
                "delay: "+delay+"\n"+
                "}\n" +
                "await page.click("+selector+", options);\n");
        return this;
    }

    public HBrowser getParentBrowser() {
        return parentBrowser;
    }

    public void setParentBrowser(HBrowser parentBrowser) {
        this.parentBrowser = parentBrowser;
    }

    public boolean isEnableJavaScript() {
        return enableJavaScript;
    }

    public NodeWindow setEnableJavaScript(boolean enableJavaScript) throws NodeJsCodeException {
        jsContext.executeJavaScript("await page.setJavaScriptEnabled("+enableJavaScript+");");
        this.enableJavaScript = enableJavaScript;
        return this;
    }
}
