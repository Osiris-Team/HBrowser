package com.osiris.headlessbrowser;

/**
 * Headless-Browser.
 *
 * @author Osiris-Team
 */
public class HBrowser {

    /**
     * Creates and returns a new {@link HWindow}, built with defaults.
     */
    public HWindow openNewWindow() {
        return new HWindowBuilder(this).build();
    }

    /**
     * Returns the {@link HWindowBuilder} to build custom window.
     */
    public HWindowBuilder openNewCustomWindow() {
        return new HWindowBuilder(this);
    }

    /**
     * Closes the provided {@link HWindow}. <br>
     * A {@link HWindow} can automatically be closed like this:
     * <pre>
     * try(HWindow hWindow = openNewWindow()){
     *     // Do stuff here...
     * }
     * </pre>
     *
     * @param HWindow
     */
    public void closeWindow(HWindow HWindow) {
        HWindow.close();
    }
}
