package com.osiris.headlessbrowser;

import com.osiris.headlessbrowser.exceptions.NodeJsCodeException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class HBrowserTest {

    @Test
    void test() throws IOException, NodeJsCodeException {
        HBrowser hBrowser = new HBrowser();
        PuppeteerWindow puppeteerWindow = hBrowser.openWindow().load("https://wikipedia.org");
    }
}