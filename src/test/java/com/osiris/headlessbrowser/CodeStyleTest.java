package com.osiris.headlessbrowser;

import org.junit.jupiter.api.Test;

import java.io.IOException;

public class CodeStyleTest {

    @Test
    void test() throws IOException {
        HBrowser hBrowser = new HBrowser();
        HWindow hWindow = hBrowser.openNewCustomWindow().enableJavaScript(false).build()
        .load("wikipedia.org");
        System.out.println(hWindow.getDocument().toString());
    }
}
