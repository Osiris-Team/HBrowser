package com.osiris.headlessbrowser.utils;

import com.osiris.headlessbrowser.HBrowser;
import com.osiris.headlessbrowser.windows.PuppeteerWindow;

import java.io.*;

public class UtilsChrome {

    /**
     * <h1>WORK IN PROGRESS!</h1> <br>
     * Generates the 'add_missing_objects.js' script in your current working dir. <br>
     * The script is meant to be run in the headless window/page instance. <br>
     * It will add all Json objects that are missing to that window/page <br>
     * and thus increase its stealth points ^o^. <br>
     */
    public File generateMissingObjects() throws IOException {
        File scriptFile = new File("add_missing_objects.js");
        if (scriptFile.exists()) scriptFile.delete();
        scriptFile.createNewFile();

        HBrowser hBrowser = new HBrowser();
        try (PuppeteerWindow hWindow = hBrowser.openCustomWindow()
                .headless(true).makeUndetectable(true).debugOutputStream(System.out).buildPuppeteerWindow()) {

            // ...
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (PuppeteerWindow hWindow = hBrowser.openCustomWindow()
                .headless(false).makeUndetectable(true).debugOutputStream(System.out).buildPuppeteerWindow()) {
            String varNames = hWindow.executeJSAndGetResult("var variables = \"\"\n" +
                    "        for (var name in this)\n" +
                    "        variables += name + \"\\n\";\n" +
                    "return variables;\n");
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(scriptFile))) {
                String var = "";
                try (BufferedReader reader = new BufferedReader(new StringReader(varNames))) {
                    while ((var = reader.readLine()) != null) {
                        try {
                            String object = hWindow.executeJSAndGetResult("" +
                                    "if (" + var + "==null) return 'null';\n" +
                                    "else return JSON.stringify(" + var + ", null, 4);");
                            System.out.println(object);
                        } catch (Exception e) {
                            System.err.println("Ignoring exception: " + e.getMessage());
                        }
                    }
                }
            }

            System.out.println(varNames);
            // ...
        } catch (Exception e) {
            e.printStackTrace();
        }
        return scriptFile;
    }

}
