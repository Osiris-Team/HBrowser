package com.osiris.headlessbrowser.javascript.apis;

import com.osiris.headlessbrowser.javascript.JS_API;
import com.osiris.headlessbrowser.javascript.interfaces.Sendable;
import org.graalvm.polyglot.HostAccess;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementation of: https://developer.mozilla.org/en-US/docs/Web/API/Console_API <br>
 *
 * @author Osiris-Team
 */
public class JS_API_Console implements JS_API {
    // This class gets loaded into the JSContext and assigned to a variable with the given name in the JSContext.
    // That means that all methods/functions and variables/fields
    // annotated with @HostAccess.Export are available inside of actual JavaScript code.
    // The method log(...) below for example, can be accessed in JavaScript via console.log('Hello!');
    private final PrintStream out;
    private final List<Sendable> onLog = new ArrayList<>();
    private final List<Sendable> onInfo = new ArrayList<>();
    private final List<Sendable> onDebug = new ArrayList<>();
    private final List<Sendable> onError = new ArrayList<>();
    private final List<Sendable> onWarn = new ArrayList<>();

    // Only accessible from Java:

    public JS_API_Console(OutputStream out) {
        this(new PrintStream(out));
    }

    public JS_API_Console(PrintStream out) {
        this.out = out;
    }

    public void onLog(Sendable runnable) {
        onLog.add(runnable);
    }

    public void onInfo(Sendable runnable) {
        onInfo.add(runnable);
    }

    public void onDebug(Sendable runnable) {
        onDebug.add(runnable);
    }

    public void onError(Sendable runnable) {
        onError.add(runnable);
    }

    public void onWarn(Sendable runnable) {
        onWarn.add(runnable);
    }

    @Override
    public String getGlobalVariableName() {
        return "console";
    }

    @Override
    public String getOptionalJSCode() {
        // Since the function console.assert() is named assert we cannot define it in Java
        // and we must do it this way:
        return "" +
                "function myAssertFunc(bol, ...data) {\n" +
                "    if (bol === true) return;\n" +
                "    let message = 'Assertion failed!';\n" +
                "    if (data.length === 0) data[0] = message;\n" +
                "    console.log(data);\n" +
                "}" +
                "console.assert = myAssertFunc;";
    }

    // Accessible from Java and JavaScript:

    @HostAccess.Export
    public void table(String tableData, String... properties) {
        // Do nothing.
    }

    @HostAccess.Export
    public void trace(String... data) {
        log(data);
    }

    @HostAccess.Export
    public void dir(Object objString, String... options) {
        // Do nothing.
    }

    @HostAccess.Export
    public void dirxml(String... data) {
        log(data);
    }

    @HostAccess.Export
    public void clear() {
        // Do nothing.
    }

    private String formatData(String... data) {
        if (data != null)
            if (data.length == 1)
                return data[0];
            else
                return Arrays.toString(data);
        else return null;
    }

    @HostAccess.Export
    public void debug(String... data) {
        String msg = formatData(data);
        if (out != null) out.println(msg);
        for (Sendable sendable : onDebug) {
            sendable.send(msg);
        }
    }

    @HostAccess.Export
    public void error(String... data) {
        String msg = formatData(data);
        if (out != null) out.println(msg);
        for (Sendable sendable : onError) {
            sendable.send(msg);
        }
    }

    @HostAccess.Export
    public void info(String... data) {
        String msg = formatData(data);
        if (out != null) out.println(msg);
        for (Sendable sendable : onInfo) {
            sendable.send(msg);
        }
    }

    @HostAccess.Export
    public void log(String... data) {
        String msg = formatData(data);
        if (out != null) out.println(msg);
        for (Sendable sendable : onLog) {
            sendable.send(msg);
        }
    }

    @HostAccess.Export
    public void warn(String... data) {
        String msg = formatData(data);
        if (out != null) out.println(msg);
        for (Sendable sendable : onWarn) {
            sendable.send(msg);
        }
    }

    @HostAccess.Export
    public void count(String... args) {
        // Do nothing
    }

    @HostAccess.Export
    public void countReset(String... args) {
        // Do nothing
    }

    @HostAccess.Export
    public void group(String... args) {
        // Do nothing
    }

    @HostAccess.Export
    public void groupCollapsed(String... args) {
        // Do nothing
    }

    @HostAccess.Export
    public void groupEnd(String... args) {
        // Do nothing
    }

    @HostAccess.Export
    public void time(String... args) {
        // Do nothing
    }

    @HostAccess.Export
    public void timeLog(String... args) {
        // Do nothing
    }

    @HostAccess.Export
    public void timeEnd(String... args) {
        // Do nothing
    }

}
