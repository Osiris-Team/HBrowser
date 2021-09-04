package com.osiris.headlessbrowser;

import de.undercouch.citeproc.script.ScriptRunnerException;
import org.graalvm.polyglot.Context;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.Callable;

class JSContextTest {

    @Test
    void testCallMethods() throws ScriptRunnerException, IOException {
        HBrowser browser = new HBrowser();
        JSContext jsContext = browser.openNewWindow().getJsContext();
        jsContext.eval("" +
                "function myMethod(params) {\n" +
                "    return 'hi';\n" +
                "}");
        System.out.println("Someone says: "+ jsContext.callMethod("myMethod", String.class));
    }

    @Test
    void testContextWebApis() throws IOException {
        HBrowser browser = new HBrowser();
        JSContext jsContext = browser.openNewWindow().getJsContext();
        jsContext.eval("console.log('hi!');");
    }

    public static class MyClass {
        public int               id    = 42;
        public String            text  = "42";
        public int[]             arr   = new int[]{1, 42, 3};
        public Callable<Integer> ret42 = () -> 42;

        public void print(String msg){
            System.out.println(msg);
        }
    }

    public static void main(String[] args) {
        try (Context context = Context.newBuilder()
                .allowAllAccess(true)
                .build()) {
            context.getBindings("js").putMember("javaObj", new MyClass());
            boolean valid = context.eval("js",
                    "    javaObj.id         == 42"          +
                            " && javaObj.text       == '42'"        +
                            " && javaObj.arr[1]     == 42"          +
                            " && javaObj.ret42()    == 42")
                    .asBoolean();
            context.eval("js", "javaObj.print('HELLO!!!');");
            assert valid == true;
        }
    }
}