package com.osiris.headlessbrowser;

/**
 * Headless-Browser.
 *
 * @author Osiris-Team
 */
public class HBrowser {

    public HBrowser() {
    }

    public HWindow openNewWindow() {
        return new HWindow();
    }

    public void closeWindow(HWindow HWindow) {
        HWindow.close();
    }
}
