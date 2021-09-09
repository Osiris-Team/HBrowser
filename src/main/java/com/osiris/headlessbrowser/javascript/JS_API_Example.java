package com.osiris.headlessbrowser.javascript;

import org.graalvm.polyglot.HostAccess;

public class JS_API_Example implements JS_API {

    // Provided by the JS_API interface.
    @Override
    public String getGlobalVariableName() {
        return "example";
    }

    // Provided by the JS_API interface.
    @Override
    public String getOptionalJSCode() {
        return null;
    }

    // This annotation creates the method below in JavaScript code.
    // It will be accessible like this: example.doSomething()
    @HostAccess.Export
    public void doSomething() {
        // Java code
    }

    // This method is only accessible from Java code, because
    // it misses the @HostAccess.Export annotation
    public void doAnotherThing() {

    }

    // If a JS Web-API has static methods or fields do not make
    // them static in Java code. Instead create a new class like below in the same package. (nested only for demonstration)
    class JS_API_Example_S implements JS_API {

        @HostAccess.Export
        public String STATIC_FIELD = "Hello world!"; // Example.STATIC_FIELD in JavaScript code

        @Override
        public String getGlobalVariableName() {
            return "Example";
        }

        @Override
        public String getOptionalJSCode() {
            return null;
        }

        @HostAccess.Export
        public void staticMethod() { // Example.staticMethod() in JavaScript code

        }

    }

    // If a JS Web-API needs special objects create them like below in the same package. (nested only for demonstration)
    class JS_CustomObject implements JS_API {

        @Override
        public String getGlobalVariableName() {
            return "CustomObject";
        }

        @Override
        public String getOptionalJSCode() {
            return null;
        }

        @HostAccess.Export
        public void doSomething() {

        }
    }

    /**
     * Finally register/load your JS_API into the {@link com.osiris.headlessbrowser.JSContext}.
     * Remember to load any custom objects it needs first.
     */

}
