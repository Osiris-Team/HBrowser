package com.osiris.headlessbrowser;

import com.osiris.headlessbrowser.javascript.JS_API;
import com.osiris.headlessbrowser.javascript.apis.console.JS_API_Console;
import com.osiris.headlessbrowser.javascript.apis.dom.JS_Event_S;
import com.osiris.headlessbrowser.javascript.exceptions.DuplicateFoundException;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JavaScript-Context. <br>
 * Something like a long String containing all the previously executed JS code. <br>
 *
 * @author Osiris-Team
 */
public class JSContext implements AutoCloseable {

    private final HWindow window;
    private final Context rawContext = Context.newBuilder("js")
            .build();
    private List<String> globalVarNames = new ArrayList<>();
    // Currently used for debugging
    private final PrintStream out = System.out;
    // Web-APIs:
    private final JS_API_Console console = new JS_API_Console(System.out);


    public JSContext(HWindow window) {
        Objects.requireNonNull(window);
        out.println("Created new JavaScript context for window '" + window + "'.");
        this.window = window;

        // Register all JavaScript Web-APIs:
        // APIs in this list get loaded into this JSContext in the order they were added to this list.
        // If you want to add an api that depends on another one make sure to add it after that one.
        // Note that override should be false.
        try {
            out.println("Loading JS Web-APIs into context...");
            long start = System.currentTimeMillis();
            registerAndLoad(console, true); // If true overrides any existing variable with the same name
            // DOM API:
            registerAndLoad(new JS_Event_S(), false);

            // Add future apis here:
            //registerAndLoad(example, false);
            //...

            globalVarNames.clear();
            out.println("Loaded all JS Web-APIs successfully. Took " + (System.currentTimeMillis() - start) + "ms.");
        } catch (Exception exception) {
            System.err.println("Failed to load JavaScript Web-API into the current JavaScript-Context! Details:");
            throw new RuntimeException(exception);
        }

    }

    @Override
    public void close() {
        rawContext.close();
    }

    /**
     * Registers and loads the provided JS-API into the current {@link JSContext}. <br>
     * A new global variable gets created for the provided JS-API with its {@link JS_API#getJSGlobalVarName()}.
     *
     * @param jsAPI    the JavaScript API to add.
     * @param override if a global variable with the same name already exists, should it get overwritten?
     */
    public JSContext registerAndLoad(JS_API jsAPI, boolean override) throws DuplicateFoundException {
        out.print("Loading JS Web-API: '" + jsAPI.getClass().getName() + "' into context...");
        out.flush();

        String globalVarName = jsAPI.getJSGlobalVarName();
        Objects.requireNonNull(globalVarName);
        if (!override && globalVarNames.contains(globalVarName))
            throw new DuplicateFoundException("Duplicate global variable name found for '" + globalVarName + "'. Global variable names must be unique!");

        globalVarNames.add(globalVarName);

        if (!override && rawContext.getBindings("js").getMember(globalVarName) != null)
            throw new DuplicateFoundException("Failed to register because of already existing/registered global class name '" + globalVarName + "'.");

        rawContext.getBindings("js").putMember(globalVarName, jsAPI);

        if (jsAPI.getOptionalJSCode() != null)
            eval(jsAPI.getOptionalJSCode());

        out.println(" SUCCESS!");
        return this;
    }

    public JS_API_Console getConsole() {
        return console;
    }

    public HWindow getWindow() {
        return window;
    }

    public Context getRawContext() {
        return rawContext;
    }

    /**
     * Executes the given jsCode in the current context. <br>
     * This means that all the jsCode that has been ran before in this {@link JSContext} is accessible
     * for the given jsCode.
     *
     * @param jsCode JavaScript code to run in the current {@link JSContext}.
     */
    public void eval(String jsCode) {
        rawContext.eval("js", jsCode);
    }

    public void eval(InputStream jsCodesInputStream) throws IOException {
        eval(new InputStreamReader(jsCodesInputStream));
    }

    public void eval(Reader reader) throws IOException {
        rawContext.eval(Source.newBuilder("js", reader, null).cached(false).build());
    }

}
