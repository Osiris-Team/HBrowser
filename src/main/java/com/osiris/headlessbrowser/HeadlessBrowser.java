package com.osiris.headlessbrowser;

import com.eclipsesource.v8.NodeJS;

import java.util.ArrayList;
import java.util.List;

public class HeadlessBrowser {

    public HeadlessBrowser() {
    }

    public HeadlessWindow openNewWindow() {
        return new HeadlessWindow();
    }

    public void closeWindow(HeadlessWindow HeadlessWindow) {
        HeadlessWindow.close();
    }
}
