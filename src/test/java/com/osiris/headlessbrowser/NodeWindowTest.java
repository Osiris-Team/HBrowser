package com.osiris.headlessbrowser;

import org.junit.jupiter.api.Test;

class NodeWindowTest {

    public static void main(String[] args) throws Exception {
        HBrowser hBrowser = new HBrowser();
        try (NodeWindow window = hBrowser.openCustomWindow().debugOutputStream(System.out).buildNodeJSWindow()) {
            window.load("https://github.com/Osiris-Team");
            System.out.println(window.getBrowserType());
            window.printCookiesAsJsonArray();
        }
    }

    @Test
    void testMethods() throws Exception {

    }
}