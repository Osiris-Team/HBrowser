package com.osiris.headlessbrowser;

import com.osiris.headlessbrowser.utils.UtilsChrome;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class Playground {

    @Test
    void testPlaywright() {
        HBrowser hBrowser = new HBrowser();
        try (PlaywrightWindow hWindow = hBrowser.openCustomWindow()
                .headless(true).makeUndetectable(true).debugOutputStream(System.out).buildPlaywrightWindow()) {
            try {
                hWindow.load("https://spigotmc.org"); // spigotmc.org
                Thread.sleep(15000); // So see result
                System.out.println(hWindow.getDocument().toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            // ...
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testHeadlessVsHead() throws IOException {
        new UtilsChrome().generateMissingObjects();
    }

    @Test
    void makeUndetectable() {
        HBrowser hBrowser = new HBrowser();
        try (PuppeteerWindow hWindow = hBrowser.openCustomWindow()
                .headless(true).makeUndetectable(true).debugOutputStream(System.out).buildPuppeteerWindow()) {
            try {
                hWindow.load("https://infosimples.github.io/detect-headless/");
                hWindow.makeScreenshot(new File("screenshot1.png"), true);
                hWindow.load("https://spigotmc.org");// Cloudflare test //"https://intoli.com/blog/not-possible-to-block-chrome-headless/chrome-headless-test.html");
                Thread.sleep(7000); // So see result
                hWindow.makeScreenshot(new File("screenshot2.png"), true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // ...
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    void testPuppeteer() {
        HBrowser hBrowser = new HBrowser();
        try (PuppeteerWindow hWindow = hBrowser.openCustomWindow()
                .headless(true).makeUndetectable(true).debugOutputStream(System.out).buildPuppeteerWindow()) {
            try {
                hWindow.load("https://intoli.com/blog/not-possible-to-block-chrome-headless/chrome-headless-test.htm");
                Thread.sleep(15000); // So see result
                hWindow.makeScreenshot(new File("screenshot.png"), true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // ...
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testLightWindow() {
        HBrowser hBrowser = new HBrowser();
        try (LightWindow hWindow = hBrowser.openCustomWindow().headless(false).debugOutputStream(System.out).buildLightWindow()) {
            try {
                hWindow.load("https://google.com");
                while (true)
                    Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // ...
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
