package com.osiris.headlessbrowser;


import com.osiris.headlessbrowser.data.chrome.ChromeHeaders;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Map;

/**
 * Headless-Window.
 *
 * @author Osiris-Team
 */
public class NodeWindow implements AutoCloseable {
    private final NodeContext jsContext = new NodeContext();
    private HBrowser parentBrowser;
    private boolean enableJavaScript;
    private Map<String, String> customHeaders;

    public static void main(String[] args) throws IOException {
        System.out.println(new HBrowser().openWindow().getDocument().outerHtml());
    }

    public NodeWindow(HBrowser parentBrowser, boolean enableJavaScript, Map<String, String> customHeaders) {
        this.parentBrowser = parentBrowser;
        this.enableJavaScript = enableJavaScript;
        this.customHeaders = customHeaders;
        try {
            jsContext.npmInstall("puppeteer");
            jsContext.executeJavaScript("const browser = await puppeteer.launch();\n" +
                    "  const page = await browser.newPage();\n");
        } catch (Exception e) {
            System.err.println("Error during install/start of Puppeteer! Details:");
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
    public NodeWindow load(String url) throws IOException {
        if (!url.startsWith("http"))
            url = "https://" + url;

        Map<String, String> headers = null;
        if (this.customHeaders == null)
            headers = new ChromeHeaders().get();
        else
            headers = this.customHeaders;

        jsContext.executeJavaScript("page.load('" + url + "');");
        return this;
    }

    public Document getDocument() throws IOException {
        String rawHtml = jsContext.executeJavaScriptAndGetResult("" +
                "var result = null;" +
                "await page.content().then((html) => {" +
                "result = html;" +
                "});");
        return Jsoup.parse(rawHtml);
    }


    /**
     * Executes the provided JavaScript code in the current context. <br>
     * See {@link GraalContext} for details. <br>
     */
    public NodeWindow executeJS(String jsCode) throws IOException {
        jsContext.executeJavaScript(jsCode);
        return this;
    }

    @Override
    public void close() throws Exception {
        jsContext.executeJavaScript("await browser.close();");
        jsContext.close();
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

    public void setEnableJavaScript(boolean enableJavaScript) {
        this.enableJavaScript = enableJavaScript;
    }

    public Map<String, String> getCustomHeaders() {
        return customHeaders;
    }

    public void setCustomHeaders(Map<String, String> customHeaders) {
        this.customHeaders = customHeaders;
    }
}
