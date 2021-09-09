package com.osiris.headlessbrowser.javascript;

public interface JS_API {

    /**
     * - Can NOT return null. <br>
     * If not null, then a global variable gets created in the JS context <br>
     * with the provided global variable name, for the implementing object. <br>
     * For details see {@link com.osiris.headlessbrowser.JSContext#registerAndLoad(JS_API, boolean)}.
     */
    String getGlobalVariableName();

    /**
     * - Can return null. <br>
     * If not null, then the provided raw JavaScript code  <br>
     * will be executed in the JavaScript context after loading this JS API. <br>
     */
    String getOptionalJSCode();
}
