package com.osiris.headlessbrowser;

import org.junit.jupiter.api.Test;

import java.io.IOException;

class HBrowserTest {

    @Test
    void test() throws IOException {
        HBrowser hBrowser = new HBrowser();
        NodeWindow nodeWindow = hBrowser.openWindow().load("https://wikipedia.org");
    }
}