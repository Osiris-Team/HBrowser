package com.osiris.headlessbrowser.windows;

import com.osiris.headlessbrowser.HBrowser;
import com.osiris.headlessbrowser.exceptions.NodeJsCodeException;
import org.junit.jupiter.api.Test;

import java.io.File;

class PlaywrightWindowTest {

    /**
     * Perform tests on all methods.
     */
    @Test
    void fullTest() throws Exception {
        HBrowser hBrowser = new HBrowser();
        try (PlaywrightWindow window = hBrowser.openCustomWindow().debugOutputStream(System.out).headless(false).buildPlaywrightWindow()) {
            window.load(new File(System.getProperty("user.dir") + "/test.html"));
            clickTest(window);
            Thread.sleep(30000);
            //formFillingTest(window);
            //window.load("example.com");
            //System.out.println(window.getResponseHeaders().toString());
        }
    }

    private void clickTest(PlaywrightWindow window) throws NodeJsCodeException {
        window.leftClick("a[href=\"#text\"]");
    }

    private void formFillingTest(PlaywrightWindow window) throws NodeJsCodeException, InterruptedException {
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