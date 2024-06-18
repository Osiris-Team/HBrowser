package com.osiris.headlessbrowser;

import com.osiris.headlessbrowser.exceptions.NodeJsCodeException;
import com.osiris.headlessbrowser.windows.HWindow;
import com.osiris.headlessbrowser.windows.PlaywrightWindow;
import com.osiris.headlessbrowser.windows.WindowBuilder;

import java.io.IOException;

public class HB {
    public static HBrowser globalHBrowserInstace = new HBrowser();

    /**
     * @see HBrowser#openWindow()
     */
    public static PlaywrightWindow newWin() {
        return new WindowBuilder(globalHBrowserInstace).buildPlaywrightWindow();
    }

    /**
     * @see HBrowser#openWindowAndLoad(String)
     */
    public PlaywrightWindow newWin(String url) throws IOException, NodeJsCodeException {
        return globalHBrowserInstace.openWindow().load(url);
    }

    /**
     * @see HBrowser#openCustomWindow()
     */
    public WindowBuilder newWinBuilder() {
        return new WindowBuilder(globalHBrowserInstace);
    }
}
