package com.osiris.headlessbrowser;


import com.osiris.headlessbrowser.data.chrome.ChromeHeaders;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Headless-Window.
 *
 * @author Osiris-Team
 */
public class HWindow implements AutoCloseable {
    // Options
    private HBrowser parentBrowser;
    private boolean enableJavaScript;
    private Map<String, String> customHeaders;

    private final JSContext jsContext = new JSContext(this);
    private Document document;
    private String authority;
    private String javaScriptCode;

    public HWindow(HBrowser parentBrowser, boolean enableJavaScript, Map<String, String> customHeaders) {
        this.parentBrowser = parentBrowser;
        this.enableJavaScript = enableJavaScript;
        this.customHeaders = customHeaders;
    }

    public HWindow load(String url) throws IOException {
        if (!url.startsWith("http"))
            url = "https://" + url;

        Map<String, String> headers = null;
        if (this.customHeaders == null)
            headers = new ChromeHeaders().get();
        else
            headers = this.customHeaders;

        authority = new URL(url).getAuthority();
        document = Jsoup.connect(url).headers(headers)
                .get();

        if (enableJavaScript){
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
                jsContext.eval(javaScriptCode);
            }
        }
        return this;
    }

    public Document getDocument() {
        return document;
    }

    public JSContext getJsContext() {
        return jsContext;
    }

    public String getAuthority() {
        return authority;
    }

    public String getJavaScriptCode() {
        return javaScriptCode;
    }

    @Override
    public void close() {
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
}
