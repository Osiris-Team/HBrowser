package com.osiris.headlessbrowser.javascript;

import com.osiris.headlessbrowser.HBrowser;
import com.osiris.headlessbrowser.HWindow;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class JS_API_ConsoleTest {

    @Test
    void test() throws IOException {
        HWindow hWindow = new HBrowser().openNewWindow();
        hWindow.getJsContext().getConsole().onLog(msg -> Assertions.assertEquals("log", msg));
        hWindow.getJsContext().getConsole().onDebug(msg -> Assertions.assertEquals("debug", msg));
        hWindow.getJsContext().getConsole().onError(msg -> Assertions.assertEquals("error", msg));
        hWindow.getJsContext().getConsole().onWarn(msg -> Assertions.assertEquals("warn", msg));
        hWindow.getJsContext()
                .eval("console.log('log');" +
                        "console.debug('debug');" +
                        "console.error('error');" +
                        "console.warn('warn');");
    }
}