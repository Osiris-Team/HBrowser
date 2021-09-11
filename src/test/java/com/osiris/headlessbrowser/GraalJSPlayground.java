package com.osiris.headlessbrowser;

import com.osiris.headlessbrowser.javascript.JS_API;
import com.osiris.headlessbrowser.javascript.exceptions.DuplicateFoundException;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.junit.jupiter.api.Test;

public class GraalJSPlayground {

    @Test
    void testStaticAccessFromJSCode() {

        Context ctx = Context.newBuilder("js").allowHostClassLookup(s->true).build();

        Object obj = new MyJavaClass();

        ctx.getBindings("js").putMember("myJavaClass", new MyJavaClass());

        ctx.eval("js", "var MyJavaClass = Java.type('"+MyJavaClass.class.getCanonicalName()+"');" +
                "console.log(MyJavaClass.HELLO);");

        ctx.eval("js", "console.log(MyJavaClass.HELLO);");
        // Expected behaviour:
        // GraalJS detects the static field HELLO in the MyJavaClass and creates the JavaScript class
        // MyJavaClass with the static field HELLO so it can be accessed via MyJavaClass.HELLO.
    }

    @Test
    void testDependentObjects() throws DuplicateFoundException {

        JSContext context = new HBrowser().openNewWindow().getJsContext()
                //.registerAndLoad(new JS_Object(), false)
                .registerAndLoad(new API_1(), false);

        context.eval("api1.getJsObject().printOut();");
    }
}
