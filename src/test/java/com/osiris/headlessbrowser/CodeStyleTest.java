package com.osiris.headlessbrowser;

import org.junit.jupiter.api.Test;

import java.io.IOException;

public class CodeStyleTest {

    @Test
    void test() throws IOException {
        HBrowser hBrowser = new HBrowser();
        try (NodeWindow hWindow = hBrowser.openCustomWindow().debugOutputStream(System.out).buildNodeJSWindow()) {
            hWindow.load("example.com");
            System.out.println(hWindow.getDocument().toString());
            hWindow.download("https://file-examples-com.github.io/uploads/2017/04/file_example_MP4_1920_18MG.mp4");
            System.out.println("Downloaded!");
            // ...
            while (true)
                Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    void testPlaywright() {
        HBrowser hBrowser = new HBrowser();
        try (PlaywrightWindow hWindow = hBrowser.openCustomWindow().debugOutputStream(System.out).buildPlaywrightWindow()) {
            hWindow.load("example.com");
            hWindow.download("https://file-examples-com.github.io/uploads/2017/02/zip_2MB.zip", null);
            System.out.println("Downloaded!");
            // ...
            while (true)
                Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
