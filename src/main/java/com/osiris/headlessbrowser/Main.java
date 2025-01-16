package com.osiris.headlessbrowser;

import com.osiris.headlessbrowser.windows.PlaywrightWindow;

public class Main {
    public static void main(String[] args) throws Exception{
        try(PlaywrightWindow win = HB.newWinBuilder().debugOutputStream(System.out).buildPlaywrightWindow()){
            win.load("https://google.com");
            System.out.println("SUCCESS!");
        }
    }
}
