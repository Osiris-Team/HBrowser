package com.osiris.headlessbrowser;

import com.osiris.headlessbrowser.javascript.JS_API_Console;
import com.osiris.headlessbrowser.javascript.exceptions.DuplicateRegisteredId;
import de.undercouch.citeproc.VariableWrapper;
import de.undercouch.citeproc.VariableWrapperParams;
import de.undercouch.citeproc.helper.json.JsonBuilder;
import de.undercouch.citeproc.helper.json.JsonObject;
import de.undercouch.citeproc.helper.json.StringJsonBuilder;
import de.undercouch.citeproc.script.AbstractScriptRunner;
import de.undercouch.citeproc.script.GraalScriptRunner;
import de.undercouch.citeproc.script.ScriptRunnerException;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;

/**
 * JavaScript-Context.
 *
 * @author Osiris-Team
 */
public class JSContext extends AbstractScriptRunner {
    private final HWindow window;
    private final Context rawContext = Context.newBuilder("js").allowAllAccess(true).build();

    // Web-APIs:
    private final JS_API_Console console = new JS_API_Console(System.out);

    public JSContext(HWindow window) {
        this.window = window;

        // Register all JavaScript Web-APIs:
        // APIs in this list get loaded into this JSContext in the order they were added to this list.
        // If you want to add an api that depends on another one make sure to add it after that one.
        // Note that override should be false by default.
        try {
            registerAndLoad("console", console, true); // If true overrides any existing variable with the same name
        } catch (Exception exception) {
            System.err.println("Failed to load one/multiple JavaScript Web-API(s) into the current JavaScript-Context! Details:");
            throw new RuntimeException(exception);
        }

    }

    public JS_API_Console getConsole() {
        return console;
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

    /**
     * Registers and loads this API into the provided {@link JSContext}. <br>
     *
     * @param id
     */
    public void registerAndLoad(String id, Object object, boolean override) throws DuplicateRegisteredId, IOException {
        if (!override && rawContext.getBindings("js").getMember(id) != null)
            throw new DuplicateRegisteredId("Failed to register because of already existing/registered id '" + id + "'.");
        rawContext.getBindings("js").putMember(id, object);
    }

    /**
     * Executes the given jsCode in the current context. <br>
     * This means that all the jsCode that has been ran before in this {@link JSContext} is accessible
     * for the given jsCode.
     *
     * @param jsCode JavaScript code to run in the current {@link JSContext}.
     * @throws ScriptRunnerException
     * @throws IOException
     */
    public void eval(String jsCode) throws IOException {
        rawContext.eval("js", jsCode);
    }

    public void eval(InputStream jsCodesInputStream) throws IOException {
        eval(new InputStreamReader(jsCodesInputStream));
    }

    public HWindow getWindow() {
        return window;
    }

    public Context getRawContext() {
        return rawContext;
    }

    /**
     * ALL METHODS BELOW ARE TAKEN FROM {@link GraalScriptRunner}. <br>
     * Some methods were added to provide extra functionality.
     */


    @Override
    public String getName() {
        return rawContext.getEngine().getImplementationName();
    }

    @Override
    public String getVersion() {
        return rawContext.getEngine().getVersion();
    }

    @Override
    public void eval(Reader reader) throws IOException {
        rawContext.eval(Source.newBuilder("js", reader, null).cached(false).build());
    }

    @Override
    public <T> T callMethod(String name, Class<T> resultType, Object... args)
            throws ScriptRunnerException {
        try {
            return convert(rawContext.getBindings("js").getMember(name)
                    .execute(convertArguments(args)), resultType);
        } catch (IOException | PolyglotException e) {
            throw new ScriptRunnerException("Could not call method", e);
        }
    }

    @Override
    public void callMethod(String name, Object... args) throws ScriptRunnerException {
        try {
            rawContext.getBindings("js").getMember(name).executeVoid(convertArguments(args));
        } catch (IOException | PolyglotException e) {
            throw new ScriptRunnerException("Could not call method", e);
        }
    }

    @Override
    public <T> T callMethod(Object obj, String name, Class<T> resultType, Object... args)
            throws ScriptRunnerException {
        try {
            return convert(((Value) obj).getMember(name)
                    .execute(convertArguments(args)), resultType);
        } catch (IOException | PolyglotException e) {
            throw new ScriptRunnerException("Could not call method", e);
        }
    }

    @Override
    public void callMethod(Object obj, String name, Object... args)
            throws ScriptRunnerException {
        try {
            ((Value) obj).getMember(name).executeVoid(convertArguments(args));
        } catch (IOException | PolyglotException e) {
            throw new ScriptRunnerException("Could not call method", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T convert(Object o, Class<T> type) {
        if (type != Object.class && o instanceof Value) {
            o = convert((Value) o);
        }
        return (T) o;
    }

    /**
     * Convert arguments that should be passed to a JavaScript function
     *
     * @param args the arguments
     * @return the converted arguments or `args` if conversion was not necessary
     * @throws IOException if an argument could not be converted
     */
    private Object[] convertArguments(Object[] args) throws IOException {
        Object[] copy = args;
        for (int i = 0; i < args.length; ++i) {
            Object v = args[i];
            Object o = v;
            if (v instanceof JsonObject || v instanceof Collection ||
                    (v != null && v.getClass().isArray()) || v instanceof Map) {
                String so = createJsonBuilder().toJson(v).toString();
                Source src = Source.newBuilder("js", "(" + so + ")", "parseMyJSON")
                        .cached(false) // we'll most likely never convert the same object again
                        .build();
                o = rawContext.eval(src);
            } else if (v instanceof VariableWrapper) {
                o = new VariableWrapperWrapper((VariableWrapper) o);
            }
            if (o != v) {
                if (copy == args) {
                    copy = Arrays.copyOf(args, args.length);
                }
                copy[i] = o;
            }
        }
        return copy;
    }

    @Override
    public void release(Object o) {
        // nothing to do here
    }

    @Override
    public JsonBuilder createJsonBuilder() {
        return new StringJsonBuilder(this);
    }

    @Override
    public void close() {
        rawContext.close();
    }

    /**
     * Wraps around {@link VariableWrapper} and converts
     * {@link VariableWrapperParams} objects to JSON objects. This class is
     * public because Graal JavaScript needs to be able to access it.
     *
     * @author Michel Kraemer
     */
    public static class VariableWrapperWrapper {
        private final VariableWrapper wrapper;

        private VariableWrapperWrapper(VariableWrapper wrapper) {
            this.wrapper = wrapper;
        }

        /**
         * Call the {@link VariableWrapper} with the given parameters
         *
         * @param params    the context in which an item should be rendered
         * @param prePunct  the text that precedes the item to render
         * @param str       the item to render
         * @param postPunct the text that follows the item to render
         * @return the string to be rendered
         */
        @SuppressWarnings("unused")
        public String wrap(Value params, String prePunct,
                           String str, String postPunct) {
            Map<String, Object> m = convertObject(params);
            VariableWrapperParams p = VariableWrapperParams.fromJson(m);
            return wrapper.wrap(p, prePunct, str, postPunct);
        }
    }

}
