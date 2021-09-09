package com.osiris.headlessbrowser;

import com.osiris.headlessbrowser.javascript.JS_API;
import com.osiris.headlessbrowser.javascript.apis.JS_API_Console;
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
            globalVariables = null;
            out.println("Loaded all JS Web-APIs into successfully.");
        } catch (Exception exception) {
            System.err.println("Failed to load one/multiple JavaScript Web-API(s) into the current JavaScript-Context! Details:");
            throw new RuntimeException(exception);
        }

    }

    /**
     * Recursively convert a JavaScript value to a Java object
     *
     * @param v the value to convert
     * @return the object
     */
    private static Object convert(Value v) {
        Object o = v;
        if (v.isNull()) {
            o = null;
        } else if (v.isBoolean()) {
            o = v.asBoolean();
        } else if (v.isDate()) {
            o = v.asDate();
        } else if (v.isDuration()) {
            o = v.asDuration();
        } else if (v.isHostObject()) {
            o = v.asHostObject();
        } else if (v.isInstant()) {
            o = v.asInstant();
        } else if (v.isNativePointer()) {
            o = v.asNativePointer();
        } else if (v.isNumber()) {
            if (v.fitsInInt()) {
                o = v.asInt();
            } else if (v.fitsInLong()) {
                o = v.asLong();
            } else if (v.fitsInDouble()) {
                o = v.asDouble();
            } else {
                throw new IllegalStateException("Unknown type of number");
            }
        } else if (v.isProxyObject()) {
            o = v.asProxyObject();
        } else if (v.isString()) {
            o = v.asString();
        } else if (v.isTime()) {
            o = v.asTime();
        } else if (v.isTimeZone()) {
            o = v.asTimeZone();
        } else if (v.hasArrayElements()) {
            o = convertArray(v);
        } else if (v.hasMembers()) {
            o = convertObject(v);
        }
        return o;
    }

    /**
     * Recursively convert a JavaScript array to a list
     *
     * @param arr the array to convert
     * @return the list
     */
    private static List<Object> convertArray(Value arr) {
        List<Object> l = new ArrayList<>();
        for (int i = 0; i < arr.getArraySize(); ++i) {
            Value v = arr.getArrayElement(i);
            Object o = convert(v);
            l.add(o);
        }
        return l;
    }

    /**
     * Recursively convert a JavaScript object to a map
     *
     * @param obj the object to convert
     * @return the map
     */
    private static Map<String, Object> convertObject(Value obj) {
        Map<String, Object> r = new LinkedHashMap<>();
        for (String k : obj.getMemberKeys()) {
            Value v = obj.getMember(k);
            Object o = convert(v);
            r.put(k, o);
        }
        return r;
    }

    @Override
    public void close() {
        rawContext.close();
    }

    /**
     * Registers and loads this API into the provided {@link JSContext}. <br>
     */
    public void registerAndLoad(JS_API jsWebApiObj, boolean override) throws DuplicateFoundException {
        out.println("Loading JS Web-API: " + jsWebApiObj.getClass().getName() + " into context...");

        String globalVarName = jsWebApiObj.getGlobalVariableName();

        if (!override && globalVariables.contains(globalVarName))
            throw new DuplicateFoundException("Duplicate global variable name found for '" + globalVarName + "'. Global variable names must be unique!");

        globalVariables.add(globalVarName);

        if (!override && rawContext.getBindings("js").getMember(globalVarName) != null)
            throw new DuplicateFoundException("Failed to register because of already existing/registered id '" + globalVarName + "'.");

        rawContext.getBindings("js").putMember(globalVarName, jsWebApiObj);

        if (jsWebApiObj.getOptionalJSCode() != null)
            eval(jsWebApiObj.getOptionalJSCode());
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

    @SuppressWarnings("unchecked")
    public <T> T convert(Object o, Class<T> type) {
        if (type != Object.class && o instanceof Value) {
            o = convert((Value) o);
        }
        return (T) o;
    }

}
