package com.osiris.headlessbrowser;

import org.graalvm.polyglot.Context;
import org.junit.jupiter.api.Test;

public class GraalJSPlayground {

    @Test
    void testStaticAccessFromJSCode() {

        Context ctx = Context.newBuilder("js").build();

        Object obj = new MyJavaClass();

        ctx.getBindings("js").putMember("myJavaClass", new MyJavaClass());

        ctx.eval("js", "var MyJavaClass = Java.type('MyJavaClass');" +
                "console.log(MyJavaClass.HELLO);");

        ctx.eval("js", "console.log(MyJavaClass.HELLO);");
        // Expected behaviour:
        // GraalJS detects the static field HELLO in the MyJavaClass and creates the JavaScript class
        // MyJavaClass with the static field HELLO so it can be accessed via MyJavaClass.HELLO.
    }

}
