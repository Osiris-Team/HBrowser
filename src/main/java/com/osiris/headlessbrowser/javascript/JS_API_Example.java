package com.osiris.headlessbrowser.javascript;

import org.graalvm.polyglot.HostAccess;

public class JS_API_Example implements JS_API {
    
    @Override
    public String getJSGlobalVarName() {
        return "example";
    }
    
    @Override
    public String getOptionalJSCode() {
        return null;
    }

    @HostAccess.Export // Makes this method accessible from JavaScript: example.doSomething()
    public void doSomething() {
        // Do something in Java code.
    }

    @HostAccess.Export
    public String returnSomething() {
        return "Hello world!"; // As you can see your methods can return variables/objects too.
    }

    // This method is only accessible from Java code, because
    // it misses the @HostAccess.Export annotation
    public void doAnotherThing() {
       // Do something in Java code.
    }

    // If a JS Web-API has static methods or fields do not make
    // them static in Java code. Instead, create a new class like below in the same package. (nested only for demonstration)
    class JS_API_Example_S implements JS_API {

        @HostAccess.Export
        public String STATIC_FIELD = "Hello world!"; // Example.STATIC_FIELD in JavaScript code

        @Override
        public String getJSGlobalVarName() {
            return "Example";
        }

        @Override
        public String getOptionalJSCode() {
            return null;
        }

        @HostAccess.Export
        public void staticMethod() { // Example.staticMethod() in JavaScript code
            // Do something in Java code.
        }

    }

    /**
     * Finally register/load your JS_API into the {@link com.osiris.headlessbrowser.JSContext}.
     */

}
