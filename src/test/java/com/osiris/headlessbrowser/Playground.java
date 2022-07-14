package com.osiris.headlessbrowser;

import com.osiris.headlessbrowser.exceptions.NodeJsCodeException;
import com.osiris.headlessbrowser.utils.UtilsChrome;
import com.osiris.headlessbrowser.windows.LightWindow;
import com.osiris.headlessbrowser.windows.PlaywrightWindow;
import com.osiris.headlessbrowser.windows.PuppeteerWindow;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class Playground {

    @Test
    void testPlaywright() throws NodeJsCodeException, InterruptedException {
        HBrowser hBrowser = new HBrowser();
        try (PlaywrightWindow hWindow = hBrowser.openCustomWindow()
                .headless(true).makeUndetectable(false).debugOutputStream(System.out).buildPlaywrightWindow()) {
            hWindow.load("https://spigotmc.org"); // spigotmc.org
            Thread.sleep(15000); // So see result
            System.out.println(hWindow.getOuterHtml().toString());
            // ...
        }
    }

    @Test
    void testHeadlessVsHead() throws IOException {
        new UtilsChrome().generateMissingObjects();
    }

    @Test
    void makeUndetectable() throws NodeJsCodeException, IOException, InterruptedException {
        HBrowser hBrowser = new HBrowser();
        try (PuppeteerWindow hWindow = hBrowser.openCustomWindow()
                .headless(true).makeUndetectable(true).debugOutputStream(System.out).buildPuppeteerWindow()) {
            //hWindow.load("https://infosimples.github.io/detect-headless/");
            //hWindow.makeScreenshot(new File("screenshot1.png"), true);
            hWindow.load("https://spigotmc.org");// Cloudflare test //"https://intoli.com/blog/not-possible-to-block-chrome-headless/chrome-headless-test.html");
            Thread.sleep(7000); // So see result
            hWindow.makeScreenshot(new File("screenshot2.png"), true);
            // ...
        }
    }

    @Test
    void makeUndetectablePlaywright() throws NodeJsCodeException, IOException {
        HBrowser hBrowser = new HBrowser();
        try (PlaywrightWindow hWindow = hBrowser.openCustomWindow()
                .headless(true).makeUndetectable(true).debugOutputStream(System.out).buildPlaywrightWindow())
        {
            hWindow.load("https://infosimples.github.io/detect-headless/");
            hWindow.makeScreenshot(new File("screenshot.png"), true);
        }
    }

    @Test
    void testPuppeteer() throws NodeJsCodeException, IOException {
        HBrowser hBrowser = new HBrowser();
        try (PuppeteerWindow hWindow = hBrowser.openCustomWindow()
                .headless(true).makeUndetectable(true).debugOutputStream(System.out).buildPuppeteerWindow()) {
            hWindow.load("https://intoli.com/blog/not-possible-to-block-chrome-headless/chrome-headless-test.html");
            //Thread.sleep(15000); // So see result
            hWindow.makeScreenshot(new File("screenshot.png"), true);
            System.out.println(hWindow.executeJSAndGetResult("return ''+JSON.stringify(navigator.plugins)"));
            // ...
        }
    }

    @Test
    void testLightWindow() throws NodeJsCodeException, IOException, InterruptedException {
        HBrowser hBrowser = new HBrowser();
        try (LightWindow hWindow = hBrowser.openCustomWindow().headless(false).debugOutputStream(System.out).buildLightWindow()) {
            hWindow.load("https://google.com");
            while (true)
                Thread.sleep(1000);
            // ...
        }
    }
}
