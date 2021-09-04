package com.osiris.headlessbrowser;

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
