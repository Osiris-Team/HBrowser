package com.osiris.headlessbrowser;

import com.osiris.headlessbrowser.graal.JS_API;
import org.graalvm.polyglot.HostAccess;

public class API_1 implements JS_API {

    @Override
    public String getJSGlobalVarName() {
        return "api1";
    }

    @Override
    public String getOptionalJSCode() {
        return null;
    }

    @HostAccess.Export
    public JS_Object getJsObject() {
        return new JS_Object();
    }
}
