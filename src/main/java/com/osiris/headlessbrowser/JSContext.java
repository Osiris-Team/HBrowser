package com.osiris.headlessbrowser;

import de.undercouch.citeproc.script.ScriptRunner;
import de.undercouch.citeproc.script.ScriptRunnerException;
import de.undercouch.citeproc.script.ScriptRunnerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class JSContext {
    private HeadlessWindow window;
    private ScriptRunner scriptRunner;

    public JSContext(HeadlessWindow window) {
        this.window = window;

        ScriptRunnerFactory.setRunnerType(ScriptRunnerFactory.RunnerType.GRAALJS);
        this.scriptRunner = ScriptRunnerFactory.createRunner();
    }

    /**
     * Executes the given jsCode in the current context. <br>
     * This means that all the jsCode that has been ran before in this {@link JSContext} is accessible
     * for the given jsCode.
     * @param jsCode JavaScript code to run in the current {@link JSContext}.
     * @throws ScriptRunnerException
     * @throws IOException
     */
    public void execute(String jsCode) throws ScriptRunnerException, IOException {
        scriptRunner.eval(new BufferedReader(new StringReader(jsCode)));
        scriptRunner.close();
    }

    public ScriptRunner getScriptRunner() {
        return scriptRunner;
    }

    public HeadlessWindow getWindow() {
        return window;
    }
}
