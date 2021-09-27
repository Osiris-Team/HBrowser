package com.osiris.headlessbrowser;

import org.junit.jupiter.api.Test;

import java.io.IOException;

public class CodeStyleTest {

    @Test
    void test() throws IOException {
        HBrowser hBrowser = new HBrowser();
        GraalWindow graalWindow = hBrowser.openCustomWindow().enableJavaScript(false).buildGraalJSWindow()
                .load("wikipedia.org");
        System.out.println(graalWindow.getDocument().toString());
    }
}
