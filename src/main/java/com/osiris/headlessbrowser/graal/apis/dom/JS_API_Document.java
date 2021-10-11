package com.osiris.headlessbrowser.graal.apis.dom;

import com.osiris.headlessbrowser.graal.JS_API;

/**
 * Implementation of: https://developer.mozilla.org/en-US/docs/Web/API/Console_API <br>
 *
 * @author Osiris-Team
 */
public class JS_API_Document implements JS_API {


    @Override
    public String getJSGlobalVarName() {
        return "document";
    }

    @Override
    public String getOptionalJSCode() {
        return null;
    }

}
