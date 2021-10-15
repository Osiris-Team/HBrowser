package com.osiris.headlessbrowser.windows;

import com.osiris.headlessbrowser.HBrowser;
import com.osiris.headlessbrowser.exceptions.NodeJsCodeException;
import org.junit.jupiter.api.Test;

import java.io.File;

class PlaywrightWindowTest {

    @Test
    void settingCookiesTest() throws Exception {
        HBrowser hBrowser = new HBrowser();
        try (PlaywrightWindow window = hBrowser.openCustomWindow().debugOutputStream(System.out).headless(false).buildPlaywrightWindow()) {
            window.setCookie("hello", "there", "https://example.com/bigboig69", null, null,false,true);
            window.load("example.com");
            window.printCookiesAsJsonArray();
        }
    }

    @Test
    private void clickTest() throws Exception {
        HBrowser hBrowser = new HBrowser();
        try (PlaywrightWindow window = hBrowser.openCustomWindow().debugOutputStream(System.out).headless(false).buildPlaywrightWindow()) {
            window.load(new File(System.getProperty("user.dir") + "/test.html"));
            window.leftClick("a[href=\"#text\"]");
        }
    }

    @Test
    private void formFillingTest() throws NodeJsCodeException {
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