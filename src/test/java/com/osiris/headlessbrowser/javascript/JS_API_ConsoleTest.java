package com.osiris.headlessbrowser.javascript;

import com.osiris.headlessbrowser.HeadlessBrowser;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class JS_API_ConsoleTest {

    @Test
    void test() throws IOException {
        new HeadlessBrowser().openNewWindow().getJsContext()
                .eval("console.log('hello');");
    }
}