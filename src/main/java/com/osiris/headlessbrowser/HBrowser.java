package com.osiris.headlessbrowser;

import com.osiris.headlessbrowser.exceptions.NodeJsCodeException;

import java.io.IOException;

/**
 * Headless-Browser.
 *
 * @author Osiris-Team
 */
public class HBrowser {

    /**
     * Creates and returns a new window, built with defaults. <br>
     * By using the {@link WindowBuilder} or the {@link #openCustomWindow()} method <br>
     * you can decide between {@link NodeWindow} and {@link GraalWindow} windows. <br>
     * Since the {@link GraalWindow} has only partial JavaScript support, due to <br>
     * currently ongoing Web-APIs implementation, its recommended to use the {@link NodeWindow} instead. <br>
     * Its powered by the latest NodeJS-Engine with the help of Puppeteer. <br>
     * NodeJS, Puppeteer and Chromiumg get installed into the current working directory automatically (~300mb). <br>
     */
    public NodeWindow openWindow() {
        return new WindowBuilder(this).buildNodeJSWindow();
    }

    /**
     * Shortcut for opening a window and loading a page into it. <br>
     * See {@link #openWindow()} for details. <br>
     */
    public NodeWindow openWindowAndLoad(String url) throws IOException, NodeJsCodeException {
        return openWindow().load(url);
    }

    /**
     * Returns the {@link WindowBuilder} to build custom window.
     */
    public WindowBuilder openCustomWindow() {
        return new WindowBuilder(this);
    }

    /**
     * Closes the provided {@link GraalWindow}. <br>
     * A {@link GraalWindow} can automatically be closed like this:
     * <pre>
     * try(HWindow hWindow = openNewWindow()){
     *     // Do stuff here...
     * } // Window gets automatically closed when leaving the try/catch block.
     * </pre>
     */
    public void closeWindow(NodeWindow window) throws Exception {
        window.close();
    }
}
