package com.osiris.headlessbrowser.issues;

import com.osiris.headlessbrowser.HBrowser;
import com.osiris.headlessbrowser.exceptions.NodeJsCodeException;
import com.osiris.headlessbrowser.windows.PlaywrightWindow;

public class Issue9 {
    public static void main(String[] args) {
        HBrowser hBrowser = new HBrowser();
        try(PlaywrightWindow window = hBrowser.openCustomWindow().headless(false).buildPlaywrightWindow()){
            for (int i = 0; i < 20; i++) {
                window.load("www.google.it");
                window.fill("[aria-label=\"Suche\"]", "searching something");
                window.holdKey("Tab");
                // click and wait...
                window.getJsContext().executeJavaScript(""+
                        "await Promise.all([\n"+
                        " page.click(':nth-match(:text(\"Google suche\"), 2)'),\n"+
                        " page.waitForNavigation()\n"+
                        "]);");
                System.out.println("Run: "+i);
            }
        } catch (NodeJsCodeException e) {
            e.printStackTrace();
        }
    }
}
