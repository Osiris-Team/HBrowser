package com.osiris.headlessbrowser;

import org.junit.jupiter.api.Test;

import java.io.IOException;

class HBrowserTest {

    @Test
    void test() throws IOException {
        HBrowser hBrowser = new HBrowser();
        HWindow hWindow = hBrowser.openNewWindow().load("https://wikipedia.org");
    }
}