package com.osiris.headlessbrowser;

import com.osiris.headlessbrowser.exceptions.NodeJsCodeException;
import com.osiris.headlessbrowser.windows.PlaywrightWindow;
import org.junit.jupiter.api.Test;

import java.io.File;

public class Bugs {
    @Test
    void weirdPathSupported() throws NodeJsCodeException {
        File dir = new File(System.getProperty("user.dir") + "/headless-browser(1)");
        try(PlaywrightWindow win = new HBrowser().setMainDirectory(dir)
                .openCustomWindow().debugOutputStream(System.out)
                .buildPlaywrightWindow()){
            win.load(TestConst.htmlTestFile);
            win.leftClick("body");
        }
    }

    @Test
    void issue9(){
        HBrowser hBrowser = new HBrowser();
        try (PlaywrightWindow window = hBrowser.openCustomWindow().headless(false).buildPlaywrightWindow()) {
            for (int i = 0; i < 20; i++) {
                window.load("www.google.it");
                window.fill("[aria-label=\"Suche\"]", "searching something");
                window.holdKey("Tab");
                // click and wait...
                window.getJsContext().executeJavaScript("" +
                        "await Promise.all([\n" +
                        " page.click(':nth-match(:text(\"Google suche\"), 2)'),\n" +
                        " page.waitForNavigation()\n" +
                        "]);");
                System.out.println("Run: " + i);
            }
        } catch (NodeJsCodeException e) {
            e.printStackTrace();
        }
    }
}
