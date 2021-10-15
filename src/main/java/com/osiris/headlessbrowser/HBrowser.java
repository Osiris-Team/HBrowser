package com.osiris.headlessbrowser;

import com.osiris.headlessbrowser.exceptions.NodeJsCodeException;
import com.osiris.headlessbrowser.windows.HWindow;
import com.osiris.headlessbrowser.windows.PlaywrightWindow;
import com.osiris.headlessbrowser.windows.WindowBuilder;

import java.io.IOException;

/**
 * Headless-Browser.
 *
 * @author Osiris-Team
 */
public class HBrowser {

    /**
     * Creates and returns a new window, built with defaults. <br>
     * Remember to close the window either by {@link #closeWindow(HWindow)} or
     * by creating the window in a try/catch blocks method. <br>
     * By using the {@link WindowBuilder} or the {@link #openCustomWindow()} method <br>
     * you can decide between other windows/browsers. <br>
     * Some windows are based on Node.js and other Node modules,
     * which get installed into the current working directory automatically
     * and thus required additional disk space(~300mb). <br>
     */
    public PlaywrightWindow openWindow() {
        return new WindowBuilder(this).buildPlaywrightWindow();
    }

    /**
     * Shortcut for opening a window and loading a page into it. <br>
     * See {@link #openWindow()} for details. <br>
     */
    public PlaywrightWindow openWindowAndLoad(String url) throws IOException, NodeJsCodeException {
        return openWindow().load(url);
    }

    /**
     * Returns the {@link WindowBuilder} to build custom window.
     */
    public WindowBuilder openCustomWindow() {
        return new WindowBuilder(this);
    }

    /**
     * Closes the provided {@link HWindow}. <br>
     * Note that a {@link HWindow} can automatically be closed like this:
     * <pre>
     * try(HWindow hWindow = openNewWindow()){
     *     // Do stuff here...
     * } // Gets automatically closed when leaving the try/catch block.
     * </pre>
     */
    public void closeWindow(HWindow window) throws Exception {
        window.close();
    }
}
