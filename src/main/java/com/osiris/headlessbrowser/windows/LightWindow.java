package com.osiris.headlessbrowser.windows;


import com.osiris.headlessbrowser.HBrowser;
import com.osiris.headlessbrowser.data.chrome.ChromeHeaders;
import com.osiris.headlessbrowser.exceptions.NodeJsCodeException;
import com.osiris.headlessbrowser.js.contexts.GraalContext;
import com.osiris.headlessbrowser.js.contexts.NodeContext;
import com.osiris.headlessbrowser.utils.HtmlView;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Headless-Window with Node.js/V8 as JavaScript engine and Node modules replacing the regular web-apis.
 *
 * @author Osiris-Team
 */
public class LightWindow implements HWindow {
    private final NodeContext jsContext;
    private final OutputStream debugOutput;
    private final HBrowser parentBrowser;
    private final boolean isHeadless;
    private final File userDataDir;
    private final boolean isDevTools;
    private final File downloadTempDir;
    private boolean enableJavaScript;
    private String url;
    private Map<String, String> customHeaders;
    private Document document;
    private String authority;
    private String javaScriptCode;

    /**
     * <p style="color: red;">Note that this is not the recommended way of creating a NodeWindow object.</p>
     * Use the {@link WindowBuilder} instead. The {@link HBrowser} has a shortcut method for creating custom windows: {@link HBrowser#openCustomWindow()}.
     */
    public LightWindow(HBrowser parentBrowser, boolean enableJavaScript, Map<String, String> customHeaders,
                       OutputStream debugOutput, boolean isHeadless, File userDataDir, boolean isDevTools, int jsTimeout, boolean makeUndetectable) {
        this.parentBrowser = parentBrowser;
        this.enableJavaScript = enableJavaScript;
        this.customHeaders = customHeaders;
        this.debugOutput = debugOutput;
        this.isHeadless = isHeadless;
        this.userDataDir = userDataDir;
        this.isDevTools = isDevTools;
        try {
            this.jsContext = new NodeContext(new File(userDataDir.getParentFile() + "/node-js"), debugOutput, jsTimeout);
            jsContext.npmInstall("jsdom");
            jsContext.executeJavaScript("" +
                    "const jsdom = require(\"jsdom\");\n" +
                    "const { JSDOM } = jsdom;\n", 30, false);


            // Define global variables/constants
            if (userDataDir == null) {
                userDataDir = new WindowBuilder(null).userDataDir; // Get the default value
            }
            if (userDataDir.isFile())
                throw new Exception("userDataDir must be a directory and cannot be a file (" + userDataDir.getAbsolutePath() + ")!");
            if (!userDataDir.exists()) userDataDir.mkdirs();

            // To be able to download files:
            downloadTempDir = new File(userDataDir + "/downloads-temp");
            if (!downloadTempDir.exists()) downloadTempDir.mkdirs();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load the contents from the provided url into the current {@link LightWindow}.
     *
     * @param url Examples: https://www.wikipedia.org or wikipedia.org.
     * @return the current {@link LightWindow} for chained method calls.
     * @throws IOException
     */
    public LightWindow load(String url) throws IOException, NodeJsCodeException {
        if (!url.startsWith("http"))
            url = "https://" + url;

        Map<String, String> headers = null;
        if (this.customHeaders == null)
            headers = new ChromeHeaders().getMap();
        else
            headers = this.customHeaders;

        authority = new URL(url).getAuthority();
        document = Jsoup.connect(url).ignoreHttpErrors(true).headers(headers)
                .get();

        jsContext.executeJavaScript("var document = new JSDOM(`" + document + "`);\n" +
                "var { window } = document;\n");

        if (enableJavaScript) {
            int scriptElements = 0;
            javaScriptCode = "";
            for (Element e :
                    document.getElementsByTag("script")) {
                if (e.hasAttr("src")) {
                    String externalScriptUrl = e.attr("src");
                    if (!externalScriptUrl.startsWith("http")) {
                        if (externalScriptUrl.startsWith("/"))
                            externalScriptUrl = "https://" + authority + externalScriptUrl;
                        else
                            externalScriptUrl = "https://" + authority + "/" + externalScriptUrl;
                    }

                    javaScriptCode = javaScriptCode + "\n" +
                            "//\n" +
                            "// Following lines are external JS-Code from " + externalScriptUrl + "\n" +
                            "//\n" +
                            "\n" +
                            "" + new String(Jsoup.connect(externalScriptUrl).ignoreContentType(true)
                            .get()
                            .connection().response().bodyAsBytes(), StandardCharsets.UTF_8);
                } else {
                    javaScriptCode = javaScriptCode + "\n" +
                            "//\n" +
                            "// Following lines are JS-Code from <script> number " + (scriptElements++) + "\n" +
                            "//\n" +
                            "\n" +
                            "" + e.data();
                }

                // Execute code
                jsContext.executeJavaScript(javaScriptCode);
            }
        }

        if (!isHeadless) {
            String finalUrl = url;
            SwingUtilities.invokeLater(() -> {
                JFrame j = new HtmlView().getFrame(finalUrl, document.toString());
                j.setLocationRelativeTo(null);
                j.setVisible(true);
            });
        }

        return this;
    }

    /**
     * Returns the current HTML-Document. <br>
     * If no page has been loaded this will return null. <br>
     */
    public Document getDocument() {
        return document;
    }

    public NodeContext getJavaScriptContext() {
        return jsContext;
    }

    /**
     * Returns the JavaScript code extracted from the pages script elements. <br>
     * If no page has been loaded this will return null. <br>
     */
    public String getLoadedJavaScriptCode() {
        return javaScriptCode;
    }

    /**
     * Executes the provided JavaScript code in the current context. <br>
     * See {@link GraalContext} for details. <br>
     */
    public LightWindow executeJS(String jsCode) throws NodeJsCodeException {
        jsContext.executeJavaScript(jsCode);
        return this;
    }

    public String getAuthority() {
        return authority;
    }

    @Override
    public void close() throws Exception {
        jsContext.close();
    }

    public HBrowser getParentBrowser() {
        return parentBrowser;
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
