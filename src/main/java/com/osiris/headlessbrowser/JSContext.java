package com.osiris.headlessbrowser;

import com.osiris.headlessbrowser.javascript.JS_API;
import com.osiris.headlessbrowser.javascript.apis.console.JS_API_Console;
import com.osiris.headlessbrowser.javascript.exceptions.DuplicateFoundException;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * JavaScript-Context.
 *
 * @author Osiris-Team
 */
public class JSContext implements AutoCloseable {

    private final HWindow window;
    private final Context rawContext = Context.newBuilder("js")
            .allowHostClassLookup(s -> true)
            .build();

    // Currently used for debugging
    private final PrintStream out = System.out;
    // Web-APIs:
    private final JS_API_Console console = new JS_API_Console(System.out);
    private List<String> globalVariables = new ArrayList<>();

    public JSContext(HWindow window) {
        out.println("Created new JavaScript context for '" + window + "'.");
        this.window = window;

        // Register all JavaScript Web-APIs:
        // APIs in this list get loaded into this JSContext in the order they were added to this list.
        // If you want to add an api that depends on another one make sure to add it after that one.
        // Note that override should be false.
        try {
            out.println("Loading JS Web-APIs into context...");
            registerAndLoad(console, true); // If true overrides any existing variable with the same name
            // Add future apis here:
            //registerAndLoad(example, false);
            //...

            globalVariables.clear();
            out.println("Loaded all JS Web-APIs into successfully.");
        } catch (Exception exception) {
            System.err.println("Failed to load one/multiple JavaScript Web-API(s) into the current JavaScript-Context! Details:");
            throw new RuntimeException(exception);
        }

    }

    @Override
    public void close() {
        rawContext.close();
    }

    /**
     * Registers and loads this API into the provided {@link JSContext}. <br>
     */
    public JSContext registerAndLoad(JS_API jsAPI, boolean override) throws DuplicateFoundException {
        out.println("Loading JS Web-API: " + jsAPI.getClass().getName() + " into context...");

        String globalVarName = jsAPI.getGlobalVariableName();

        if (!override && globalVariables.contains(globalVarName))
            throw new DuplicateFoundException("Duplicate global variable name found for '" + globalVarName + "'. Global variable names must be unique!");

        globalVariables.add(globalVarName);

        if (!override && rawContext.getBindings("js").getMember(globalVarName) != null)
            throw new DuplicateFoundException("Failed to register because of already existing/registered id '" + globalVarName + "'.");

        rawContext.getBindings("js").putMember(globalVarName, jsAPI);

        if (jsAPI.getOptionalJSCode() != null)
            eval(jsAPI.getOptionalJSCode());

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
