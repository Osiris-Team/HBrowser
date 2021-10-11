package com.osiris.headlessbrowser.javascript;

import com.osiris.headlessbrowser.HBrowser;
import com.osiris.headlessbrowser.windows.GraalWindow;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class HasGlobalVariable_ConsoleTest {

    @Test
    void test() throws IOException {
        GraalWindow graalWindow = new HBrowser().openCustomWindow().buildGraalJSWindow();
        graalWindow.getJavaScriptContext().getConsole().onLog(msg -> Assertions.assertEquals("log", msg));
        graalWindow.getJavaScriptContext().getConsole().onDebug(msg -> Assertions.assertEquals("debug", msg));
        graalWindow.getJavaScriptContext().getConsole().onError(msg -> Assertions.assertEquals("error", msg));
        graalWindow.getJavaScriptContext().getConsole().onWarn(msg -> Assertions.assertEquals("warn", msg));
        graalWindow.getJavaScriptContext()
                .eval("console.log('log');" +
                        "console.debug('debug');" +
                        "console.error('error');" +
                        "console.warn('warn');");
    }
}