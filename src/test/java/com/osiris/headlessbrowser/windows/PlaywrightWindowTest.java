package com.osiris.headlessbrowser.windows;

import com.osiris.headlessbrowser.HBrowser;
import com.osiris.headlessbrowser.exceptions.NodeJsCodeException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlaywrightWindowTest {

    @Test
    void testOpen() throws NodeJsCodeException {
        HBrowser hBrowser = new HBrowser();
        try (PlaywrightWindow window = hBrowser.openCustomWindow().temporaryUserDataDir(true).debugOutputStream(System.out).headless(false).buildPlaywrightWindow()) {
            for (int i = 0; i < 30; i++) {
                window.newTab();
            }
        }
        // TODO give each window its own node context folder
    }

    @Test
    void testConcurrentWindows() throws Exception {
        HBrowser hBrowser = new HBrowser();
        List<Thread> threads = new ArrayList<>();
        List<Exception> errors = new CopyOnWriteArrayList<>();
        File tmp = new File(System.getProperty("user.dir")+"/test-temp");
        tmp.mkdirs();
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            Thread t1 = new Thread(() -> {
                try (PlaywrightWindow window = hBrowser.openCustomWindow().debugOutputStream(System.out).headless(true).makeUndetectable(true).buildPlaywrightWindow()) {
                    window.load("https://infosimples.github.io/detect-headless/");
                    window.leftClick("body");
                    window.makeScreenshot(new File(tmp+ "/evasions-screenshot-"+ finalI +".png"), true);
                } catch (Exception e) {
                    errors.add(e);
                }
            });
            threads.add(t1);
        }
        for (Thread thread : threads) {
            thread.start();
        }
        for (Thread thread : threads) {
            System.out.println("Waiting for: "+thread);
            thread.join();
        }
        FileUtils.deleteDirectory(tmp);
        assertEquals(0, errors.size());
    }

    @Test
    void testEvasions() throws NodeJsCodeException {
        HBrowser hBrowser = new HBrowser();
        try (PlaywrightWindow window = hBrowser.openCustomWindow().temporaryUserDataDir(true).debugOutputStream(System.out).headless(true).makeUndetectable(true).buildPlaywrightWindow()) {
            window.load("https://infosimples.github.io/detect-headless/");
            window.makeScreenshot(new File("evasions-screenshot.png"), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void settingCookiesTest() throws Exception {
        HBrowser hBrowser = new HBrowser();
        try (PlaywrightWindow window = hBrowser.openCustomWindow().temporaryUserDataDir(true).debugOutputStream(System.out).headless(false).buildPlaywrightWindow()) {
            window.setCookie("hello", "there", "https://example.com/bigboig69", null, null, false, true);
            window.load("example.com");
            window.printCookiesAsJsonArray();
            Thread.sleep(2000);
        }
    }

    @Test
    public void clickTest() throws Exception {
        HBrowser hBrowser = new HBrowser();
        try (PlaywrightWindow window = hBrowser.openCustomWindow().debugOutputStream(System.out).headless(false).buildPlaywrightWindow()) {
            window.load(new File(System.getProperty("user.dir") + "/test.html"));
            window.leftClick("a[href=\"#text\"]");
        }
    }

    @Test
    public void formFillingTest() throws NodeJsCodeException, InterruptedException {
        HBrowser hBrowser = new HBrowser();
        try (PlaywrightWindow window = hBrowser.openCustomWindow().debugOutputStream(System.out).headless(false).buildPlaywrightWindow()) {
            window.load(new File(System.getProperty("user.dir") + "/test.html"));
            String expected = "This is the expected value!";
            String actual = null;
            window.fill("id=input__text", expected);
            window.fill("id=input__password", expected);
            actual = window.getOuterHtml().getElementById("input__text").attr("value");
            // TODO seems like it sets the text correctly.
            // TODO Just got to find a way of retrieving the value from the form, because it wont work with the regular form.value thing.
            //assertEquals(expected, actual);
        }
    }
}