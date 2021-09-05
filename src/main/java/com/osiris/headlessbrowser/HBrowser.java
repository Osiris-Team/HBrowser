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
        return new HWindowBuilder(this).build();
    }

    /**
     * Returns the {@link HWindowBuilder} to build custom window.
     */
    public HWindowBuilder openNewCustomWindow() {
        return new HWindowBuilder(this);
    }

    public void closeWindow(HWindow HWindow) {
        HWindow.close();
    }
}
