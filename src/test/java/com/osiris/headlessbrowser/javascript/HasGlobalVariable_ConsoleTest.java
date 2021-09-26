package com.osiris.headlessbrowser.javascript;

import com.osiris.headlessbrowser.HBrowser;
import com.osiris.headlessbrowser.HWindow;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class HasGlobalVariable_ConsoleTest {

    @Test
    void test() throws IOException {
        HWindow hWindow = new HBrowser().openNewWindow();
        hWindow.getJavaScriptContext().getConsole().onLog(msg -> Assertions.assertEquals("log", msg));
        hWindow.getJavaScriptContext().getConsole().onDebug(msg -> Assertions.assertEquals("debug", msg));
        hWindow.getJavaScriptContext().getConsole().onError(msg -> Assertions.assertEquals("error", msg));
        hWindow.getJavaScriptContext().getConsole().onWarn(msg -> Assertions.assertEquals("warn", msg));
        hWindow.getJavaScriptContext()
                .eval("console.log('log');" +
                        "console.debug('debug');" +
                        "console.error('error');" +
                        "console.warn('warn');");
    }
}