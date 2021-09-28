package com.osiris.headlessbrowser;

import org.graalvm.polyglot.Context;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.Callable;

class GraalContextTest {

    public static void main(String[] args) {
        try (Context context = Context.newBuilder()
                .allowAllAccess(true)
                .build()) {
            context.getBindings("js").putMember("javaObj", new MyClass());
            boolean valid = context.eval("js",
                            "    javaObj.id         == 42" +
                                    " && javaObj.text       == '42'" +
                                    " && javaObj.arr[1]     == 42" +
                                    " && javaObj.ret42()    == 42")
                    .asBoolean();
            context.eval("js", "javaObj.print('HELLO!!!');");
            assert valid == true;
        }
    }

    @Test
    void testConsoleApi() throws IOException {
        HBrowser hBrowser = new HBrowser();
        GraalContext graalContext = hBrowser.openCustomWindow().buildGraalJSWindow().getJavaScriptContext();
        graalContext.getConsole().onLog(msg -> System.out.println("JavaScript message received: " + msg));
        graalContext.eval("console.log('john stamos');");
    }

    @Test
    void testContextWebApis() throws IOException {
        HBrowser browser = new HBrowser();
        GraalContext graalContext = browser.openCustomWindow().buildGraalJSWindow().getJavaScriptContext();
        graalContext.eval("console.log('hi!');");
    }

    public static class MyClass {
        public int id = 42;
        public String text = "42";
        public int[] arr = new int[]{1, 42, 3};
        public Callable<Integer> ret42 = () -> 42;

        public void print(String msg) {
            System.out.println(msg);
        }
    }
}