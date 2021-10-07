package com.osiris.headlessbrowser;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class CodeStyleTest {

    @Test
    void test() throws IOException {
        HBrowser hBrowser = new HBrowser();
        try (PuppeteerWindow hWindow = hBrowser.openCustomWindow().debugOutputStream(System.out).buildNodeJSWindow()) {
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
        try (PlaywrightWindow hWindow = hBrowser.openCustomWindow().headless(false).debugOutputStream(System.out).buildPlaywrightWindow()) {
            hWindow.load("example.com");
            try{
                hWindow.download("https://file-examples-com.github.io/uploads/2017/02/zip_2MB.zip", new File(System.getProperty("user.dir")+"/download.zip"));
                System.out.println("Downloaded!");
            } catch (Exception e) {
                e.printStackTrace();
            }
            // ...
            while (true)
                Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
