package com.osiris.headlessbrowser;

/**
 * Headless-Browser.
 *
 * @author Osiris-Team
 */
public class HBrowser {
    private Type type;

    enum Type{
        /**
         * The default browser completely written in Java. <br>
         * Not recommended, since its currently in development and has only partial JavaScript support.
         */
        DEFAULT,
        /**
         * Downloads/Installs NodeJS into the current working directory along with Puppeteer <br>
         * which will download a compatible version of Chromium.
         */
        PUPPETEER
    }

    /**
     * Initialises this browser as {@link Type#PUPPETEER}.
     */
    public HBrowser(){
        this(Type.PUPPETEER);
    }

    public HBrowser(Type type) {
        this.type = type;
    }

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
