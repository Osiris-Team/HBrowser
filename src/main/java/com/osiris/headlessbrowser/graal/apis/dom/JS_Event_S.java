package com.osiris.headlessbrowser.graal.apis.dom;

import com.osiris.headlessbrowser.graal.JS_API;
import org.graalvm.polyglot.HostAccess;

/**
 * Fake statics class. <br>
 * Standard from: https://dom.spec.whatwg.org/ <br>
 * Checked: 07.09.2021 <br>
 */
public class JS_Event_S implements JS_API {

    @HostAccess.Export
    public final short NONE = 0;
    @HostAccess.Export
    public final short CAPTURING_PHASE = 1;
    @HostAccess.Export
    public final short AT_TARGET = 2;
    @HostAccess.Export
    public final short BUBBLING_PHASE = 3;

    @Override
    public String getJSGlobalVarName() {
        return "Event";
    }

    @Override
    public String getOptionalJSCode() {
        return null;
    }
}
