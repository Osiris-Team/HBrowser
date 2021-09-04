package com.osiris.headlessbrowser.javascript;

import com.osiris.headlessbrowser.javascript.defaults.Default_JS_API;
import com.osiris.headlessbrowser.javascript.defaults.JavaScriptAPI;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Implementation of: https://developer.mozilla.org/en-US/docs/Web/API/Console_API <br>
 * WORK IN PROGRESS <br>
 * @author Osiris-Team
 */
public class JS_API_Console extends Default_JS_API implements JavaScriptAPI {
    private PrintStream out;

    public JS_API_Console(OutputStream out) {
        this(new PrintStream(out));
    }
    public JS_API_Console(PrintStream out) {
        this.out = out;
    }

    @Override
    public String getJSVarName() {
        // This is the global variable name for this api.
        // Other JavaScript code will be able to access it over this name.
        // Must be unique.
        return "console";
    }

    @Override
    public Object getObject() {
        return new Console();
    }

    class Console{
        /*
        TODO IMPLEMENT THESE
        TODO GENERATE TEST FOR EACH METHOD.
        TODO Remove from list when implemented.
namespace console { // but see namespace object requirements below
  // Logging
  undefined assert(optional boolean condition = false, any... data);
  undefined clear();
  undefined debug(any... data);
  undefined error(any... data);
  undefined info(any... data);
  undefined log(any... data);
  undefined table(optional any tabularData, optional sequence<DOMString> properties);
  undefined trace(any... data);
  undefined warn(any... data);
  undefined dir(optional any item, optional object? options);
  undefined dirxml(any... data);

  // Counting
  undefined count(optional DOMString label = "default");
  undefined countReset(optional DOMString label = "default");

  // Grouping
  undefined group(any... data);
  undefined groupCollapsed(any... data);
  undefined groupEnd();

  // Timing
  undefined time(optional DOMString label = "default");
  undefined timeLog(optional DOMString label = "default", any... data);
  undefined timeEnd(optional DOMString label = "default");
};
         */

        public void log(String msg){
            out.println(msg);
        }
    }


}
