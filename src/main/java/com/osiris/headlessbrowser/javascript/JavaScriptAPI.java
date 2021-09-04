package com.osiris.headlessbrowser.javascript;

import com.osiris.headlessbrowser.JSContext;

import java.io.InputStream;

public interface JavaScriptAPI {

    /**
     * The global variable name used in the JavaScript code to access the APIs methods/functions. <br>
     * Example: console.log('Hello'); <br>
     * JSVarName: console <br>
     * Should be unique. <br>
     */
    String getJSVarName();

    /**
     * The object containing all methods/functions of the API. <br>
     */
    Object getObject();

}
