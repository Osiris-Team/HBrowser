package com.osiris.headlessbrowser.javascript;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Implementation of: https://developer.mozilla.org/en-US/docs/Web/API/Console_API <br>
 * WORK IN PROGRESS <br>
 *
 * @author Osiris-Team
 */
public class JS_API_Console implements JavaScriptAPI {
    // This class gets loaded into the JSContext and assigned to a variable with the name from getJSVarName().
    // That means that all public methods/functions and variables/fields in this class are available inside of actual JavaScript code.
    // The method log(String msg) below for example, can be accessed in JavaScript via console.log('Hello!');
    private final PrintStream out;

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
        return this;
    }

    public void log(String msg) {
        out.println(msg);
    }
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

}
