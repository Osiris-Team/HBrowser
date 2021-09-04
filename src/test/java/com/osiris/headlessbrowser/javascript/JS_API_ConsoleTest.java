package com.osiris.headlessbrowser.javascript;

import com.osiris.headlessbrowser.HBrowser;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class JS_API_ConsoleTest {

    @Test
    void test() throws IOException {
        new HBrowser().openNewWindow().getJsContext()
                .eval("console.log('hello');");
    }
}