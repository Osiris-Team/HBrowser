package com.osiris.headlessbrowser;

import com.osiris.headlessbrowser.javascript.JS_API;
import org.graalvm.polyglot.HostAccess;

public class JS_Object {

    @HostAccess.Export
    public void printOut(){
        System.out.println("I was run from printOut() of a JS_Object!");
    }
}