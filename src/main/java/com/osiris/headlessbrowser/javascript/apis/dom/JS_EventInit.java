package com.osiris.headlessbrowser.javascript.apis.dom;

import com.oracle.truffle.js.runtime.builtins.JSDictionary;
import org.graalvm.polyglot.HostAccess;

import java.util.Dictionary;
import java.util.Enumeration;

public class JS_EventInit {

    @HostAccess.Export
    boolean bubbles = false;

    @HostAccess.Export
    boolean cancelable = false;

    @HostAccess.Export
    boolean composed = false;
}
