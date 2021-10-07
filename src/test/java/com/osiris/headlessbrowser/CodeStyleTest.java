package com.osiris.headlessbrowser;

import org.junit.jupiter.api.Test;

import java.io.File;

public class CodeStyleTest {

    @Test
    void testPlaywright() {
        HBrowser hBrowser = new HBrowser();
        try (PlaywrightWindow hWindow = hBrowser.openCustomWindow().debugOutputStream(System.out).buildPlaywrightWindow()) {
            hWindow.load("example.com");
            try {
                hWindow.download("https://file-examples-com.github.io/uploads/2017/02/zip_2MB.zip", new File(System.getProperty("user.dir") + "/download.zip"));
                System.out.println("Downloaded!");
            } catch (Exception e) {
                e.printStackTrace();
            }
            // ...
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
