package com.osiris.headlessbrowser;

import org.graalvm.polyglot.HostAccess;

public class MyJavaClass {

    @HostAccess.Export
    public static String HELLO = "HI!";

}
