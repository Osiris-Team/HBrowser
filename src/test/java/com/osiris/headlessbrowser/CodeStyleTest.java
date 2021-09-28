package com.osiris.headlessbrowser;

import org.junit.jupiter.api.Test;

import java.io.IOException;

public class CodeStyleTest {

    @Test
    void test() throws IOException {
        HBrowser hBrowser = new HBrowser();
        try(NodeWindow hWindow = hBrowser.openWindowAndLoad("example.com")){
            System.out.println(hWindow.getDocument().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
