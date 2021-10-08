package com.osiris.headlessbrowser;

import com.osiris.betterthread.BetterThreadManager;
import com.osiris.headlessbrowser.exceptions.NodeJsCodeException;
import com.osiris.headlessbrowser.utils.AsyncInputStream;
import com.osiris.headlessbrowser.utils.DownloadTask;
import com.osiris.headlessbrowser.utils.TrashOutput;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.rauschig.jarchivelib.ArchiverFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class NodeContext implements AutoCloseable {
    private final Process process;
    private final AsyncInputStream processInput;
    private final AsyncInputStream processErrorInput;
    private final OutputStream processOutput;
    private final PrintStream debugOutput;
    private final File lastJsCodeExecutionResultFile;
    private final int timeout;
    private final File installationDir;
    private File executableFile;
    private OperatingSystemArchitectureType osArchitectureType = null;
    private OperatingSystemType osType;

    public NodeContext() {
        this(null, null, 30);
    }

    /**
     * @param installationDir path of an empty directory (can also not exist yet) to install the latest Node.js into if needed.
     *                        If null Node.js will get installed into ./NodeJS-Installation ("." is the current working directory).
     * @param debugOutput     if null, debug output won't be written/printed, otherwise it gets printed/written to the provided {@link OutputStream}.
     * @param timeout         the max time in seconds to wait for JavaScript code to finish. Set to 0 to disable.
     */
    public NodeContext(File installationDir, OutputStream debugOutput, int timeout) {
        this.timeout = timeout;
        if (debugOutput == null)
            this.debugOutput = new PrintStream(new TrashOutput());
        else
            this.debugOutput = new PrintStream(debugOutput);
        PrintStream out = this.debugOutput;
        if (installationDir == null) {
            this.installationDir = new File(System.getProperty("user.dir") + "/NodeJS-Installation");
            installationDir = this.installationDir;
        } else
            this.installationDir = installationDir;
        // Download and install NodeJS into current working directory if no installation found
        try {
            determineArchAndOs();
            if (!installationDir.exists()
                    || Objects.requireNonNull(installationDir.listFiles()).length == 0) {
                String url = "https://nodejs.org/dist/latest/";
                out.println("Installing latest NodeJS release from '" + url + "'...");
                Document docLatest = Jsoup.connect(url).get();

                String downloadUrl = null;
                for (Element e :
                        docLatest.getElementsByTag("a")) {
                    String attr = e.attr("href");
                    if (isCorrectFileForOs(attr)) {
                        downloadUrl = url + attr;
                        break;
                    }
                }

                if (downloadUrl == null)
                    throw new FileNotFoundException("Failed to find latest NodeJS download url at '" + url + "' for OS '" + osType.name() + "' with ARCH '" + osArchitectureType.name() + "'.");

                // Download the zip file and extract its contents
                if (!installationDir.exists()) installationDir.mkdirs();
                if (osType.equals(OperatingSystemType.WINDOWS)) {
                    File downloadZip = new File(installationDir + "/download.zip");
                    if (downloadZip.exists()) downloadZip.delete();
                    downloadZip.createNewFile();
                    DownloadTask downloadTask = new DownloadTask("Download", new BetterThreadManager(), downloadUrl, downloadZip, "zip");
                    downloadTask.start();
                    while (!downloadTask.isFinished()) {
                        out.println("Download-Task > " + downloadTask.getStatus());
                        Thread.sleep(1000);
                    }
                    out.println("Download-Task > " + downloadTask.getStatus());

                    out.print("Extracting NodeJS files...");
                    out.flush();
                    ZipFile zipFile = new ZipFile(downloadZip);
                    zipFile.extractFile(zipFile.getFileHeaders().get(0).getFileName(), installationDir.getPath());
                    downloadZip.delete();
                    out.println(" SUCCESS!");
                } else {
                    File downloadFile = new File(installationDir + "/download.tar.gz");
                    if (downloadFile.exists()) downloadFile.delete();
                    downloadFile.createNewFile();
                    DownloadTask downloadTask = new DownloadTask("Download", new BetterThreadManager(), downloadUrl, downloadFile, "gzip", "tar.gz", "tar", "tar+gzip", "x-gtar", "x-gzip", "x-tgz");
                    downloadTask.start();
                    while (!downloadTask.isFinished()) {
                        out.println("Download-Task > " + downloadTask.getStatus());
                        Thread.sleep(1000);
                    }
                    out.println("Download-Task > " + downloadTask.getStatus());
                    out.print("Extracting NodeJS files...");
                    out.flush();
                    ArchiverFactory.createArchiver(downloadFile).extract(downloadFile, installationDir);
                    // Result should be .../headless-browser/node-js/node<version>/...
                    downloadFile.delete();
                    out.println(" SUCCESS!");
                }
            }

            for (File f :
                    Objects.requireNonNull(
                            Objects.requireNonNull(installationDir.listFiles())[0].listFiles())) {
                if (f.getName().contains(".exe")) { // TODO add support for other OS
                    executableFile = f;
                }
            }
        } catch (Exception e) {
            System.err.println("Error during installation of NodeJS. Details:");
            throw new RuntimeException(e);
        }

        // Start NodeJS and get the processes I/O
        try {
            out.print("Initialising NodeJS...");
            out.flush();
            ProcessBuilder processBuilder = new ProcessBuilder(Arrays.asList(
                    executableFile.getAbsolutePath(), "--interactive"));
            // Must be inherited so that NodeJS closes when this application closes.
            // Wrong! It seems like NodeJS closes if the parent process dies, even if its Piped I/O.
            process = processBuilder.start();
            Objects.requireNonNull(process.getInputStream());
            Objects.requireNonNull(process.getOutputStream());
            // Somehow the I/O from the node.exe cannot be read?
            // Tried multiple things without success.
            // Update: Node.exe must be started with this flag to get correct I/O: --interactive
            processInput = new AsyncInputStream(process.getInputStream());
            processInput.listeners.add(line -> out.println("[" + getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + "|LOG] " + line));
            processErrorInput = new AsyncInputStream(process.getErrorStream());
            processOutput = process.getOutputStream();
            out.println(" SUCCESS!");
            out.println("Node-JS was started from: " + executableFile);

            Thread t = new Thread(() -> {
                // Keeps the java application running until NodeJS closes
                try {
                    while (process.isAlive()) {
                        Thread.sleep(5000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            t.start();

            executeJavaScript("function sleep(ms) {\n" +
                    "  return new Promise(resolve => setTimeout(resolve, ms));\n" +
                    "}");

            lastJsCodeExecutionResultFile = new File(executableFile.getParentFile() + "/JavaScriptCodeResult.txt");
            if (!lastJsCodeExecutionResultFile.exists()) lastJsCodeExecutionResultFile.createNewFile();
            String resultFilePath = lastJsCodeExecutionResultFile.getAbsolutePath().replace("\\", "/"); // To avoid issues with indows file path formats
            executeJavaScript("function writeToJava(result) {\n" +
                    "var fs = require('fs');\n" +
                    "var data = fs.writeFileSync('" + resultFilePath + "', result);\n" +
                    "};\n" +
                    "console.log('Context initialised!');\n");

        } catch (Exception e) {
            System.err.println("Error during start of NodeJS! Details:");
            throw new RuntimeException(e);
        }
    }

    /**
     * Note that {@link #determineArchAndOs()} must have been called before executing this method. <br>
     *
     * @param fileName example: node-v16.10.0-darwin-arm64.tar.gz
     * @return true if the provided file name contains details matching the current system.
     */
    private boolean isCorrectFileForOs(String fileName) {
        if (!fileName.contains("."))
            return false; // cannot be a directory
        if (osType.equals(OperatingSystemType.MAC)) {
            if (!fileName.contains(".tar.gz"))
                return false;
            // Mac has another name: darwin instead of mac
            if (StringUtils.containsIgnoreCase(fileName, "darwin")
                    && (StringUtils.containsIgnoreCase(fileName, osArchitectureType.name()) || StringUtils.containsIgnoreCase(fileName, osArchitectureType.altName)))
                return true;
        } else if (osType.equals(OperatingSystemType.WINDOWS)) {
            // Must be a zip file
            if (!fileName.contains(".zip"))
                return false;
            // Mac has another name: win instead of windows
            if (StringUtils.containsIgnoreCase(fileName, "win")
                    && (StringUtils.containsIgnoreCase(fileName, osArchitectureType.name()) || StringUtils.containsIgnoreCase(fileName, osArchitectureType.altName)))
                return true;
        } else {
            if (!fileName.contains(".tar.gz"))
                return false;
        }
        return StringUtils.containsIgnoreCase(fileName, osType.name)
                && (StringUtils.containsIgnoreCase(fileName, osArchitectureType.name()) || StringUtils.containsIgnoreCase(fileName, osArchitectureType.altName));
    }

    private void determineArchAndOs() {
        // First set the details we need
        // Start by setting the operating systems architecture type
        String actualOsArchitecture = System.getProperty("os.arch").toLowerCase();
        for (OperatingSystemArchitectureType type :
                OperatingSystemArchitectureType.values()) {
            if (actualOsArchitecture.equals(type.toString().toLowerCase())) // Not comparing the actual names because the enum has more stuff matching one name
                osArchitectureType = type;
        }
        if (osArchitectureType == null) {
            // Do another check.
            // On windows it can be harder to detect the right architecture that's why we do the stuff below:
            // Source: https://stackoverflow.com/questions/4748673/how-can-i-check-the-bitness-of-my-os-using-java-j2se-not-os-arch/5940770#5940770
            String arch = System.getenv("PROCESSOR_ARCHITECTURE");
            String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
            boolean is64 = arch != null && arch.endsWith("64")
                    || wow64Arch != null && wow64Arch.endsWith("64"); // Otherwise its 32bit
            if (is64)
                osArchitectureType = OperatingSystemArchitectureType.X64;
            else
                osArchitectureType = OperatingSystemArchitectureType.X32;
            debugOutput.println("The current operating systems architecture '" + actualOsArchitecture +
                    "' was not found in the architectures list '" + Arrays.toString(OperatingSystemArchitectureType.values()) + "'." +
                    " Defaulting to '" + osArchitectureType + "'.");
        }
        debugOutput.println("Determined '" + osArchitectureType.name() + "' as operating systems architecture.");

        // Set the operating systems type

        String actualOsType = System.getProperty("os.name").toLowerCase();
        if (actualOsType.contains("alpine"))
            osType = OperatingSystemType.ALPINE_LINUX;
        if (actualOsType.contains("win"))
            osType = OperatingSystemType.WINDOWS;
        else if (actualOsType.contains("mac"))
            osType = OperatingSystemType.MAC;
        else if (actualOsType.contains("aix"))
            osType = OperatingSystemType.AIX;
        else if (actualOsType.contains("nix")
                || actualOsType.contains("nux"))
            osType = OperatingSystemType.LINUX;
        else if (actualOsType.contains("sunos"))
            osType = OperatingSystemType.SOLARIS;
        else {
            osType = OperatingSystemType.LINUX;
            debugOutput.println("The current operating system '" + actualOsType + "' was not found in the supported operating systems list." +
                    " Defaulting to '" + OperatingSystemType.LINUX.name() + "'.");
        }
        debugOutput.println("Determined '" + osType.name() + "' as operating system.");
    }

    @Override
    public void close() throws Exception {
        //executeJavaScript("process.exit();", 0);
        debugOutput.println("CLOSING " + this);
        process.destroy();
    }

    public synchronized NodeContext writeLine(String line) throws IOException {
        if (line.contains("\n")) {
            debugOutput.println("Writing multiple lines to NodeJS context:");
            debugOutput.println("START >>>>>>>>>");
            int lineNumber = 1;
            String singleLine = null;
            try (BufferedReader br = new BufferedReader(new StringReader(line))) {
                while ((singleLine = br.readLine()) != null) {
                    debugOutput.println(lineNumber + "| " + singleLine);
                    lineNumber++;
                    processOutput.write("\n".getBytes(StandardCharsets.UTF_8)); // To ensure that multi-lined code from before doesn't affect the next lines
                    processOutput.flush();
                    processOutput.write(".break\n".getBytes(StandardCharsets.UTF_8)); // To ensure that multi-lined code from before doesn't affect the next lines
                    processOutput.flush();
                    processOutput.write((singleLine + "\n").getBytes(StandardCharsets.UTF_8));
                    processOutput.flush();
                    processOutput.write(".break\n".getBytes(StandardCharsets.UTF_8)); // To ensure that multi-lined code from before doesn't affect the next lines
                    processOutput.flush();

                }
            }
            debugOutput.println("END <<<<<<<<<<<");
        } else {
            debugOutput.println("Writing line to NodeJS context: " + line);
            processOutput.write("\n".getBytes(StandardCharsets.UTF_8)); // To ensure that multi-lined code from before doesn't affect the next lines
            processOutput.flush();
            processOutput.write(".break\n".getBytes(StandardCharsets.UTF_8)); // To ensure that multi-lined code from before doesn't affect the next lines
            processOutput.flush();
            processOutput.write((line + "\n").getBytes(StandardCharsets.UTF_8));
            processOutput.flush();
            processOutput.write(".break\n".getBytes(StandardCharsets.UTF_8)); // To ensure that multi-lined code from before doesn't affect the next lines
            processOutput.flush();
        }
        return this;
    }

    /**
     * See {@link #executeJavaScript(String, int, boolean)} for details.
     */
    public NodeContext executeJavaScript(String jsCode) throws NodeJsCodeException {
        return executeJavaScript(jsCode, timeout, true);
    }

    /**
     * Executes JavaScript code from the provided {@link String} in the <br>
     * current {@link NodeContext}. <br>
     * Note that everything related to JavaScript gets printed/written to the {@link #debugOutput}, which <br>
     * means that you must have provided this {@link NodeContext} a {@link #debugOutput} at initialisation to see your codes output. <br>
     */
    public synchronized NodeContext executeJavaScript(String jsCode, int timeout, boolean wrapInTryCatch) throws NodeJsCodeException {
        try {
            // Writing stuff directly to the process output/NodeJs REPL console somehow is very error-prone.
            // That's why instead we create a temp file with the js code in it and load it using the .load command.
            long msStart = System.currentTimeMillis();
            File tmpJs = new File(executableFile.getParentFile() + "/temp" + msStart + ".js");
            if (!tmpJs.exists()) tmpJs.createNewFile();

            if (wrapInTryCatch) {
                jsCode = "try{\n" + // Just to make sure that errors get definitively caught
                        jsCode + "\n" +
                        "console.log('Execution of JS-Code(" + msStart + ") finished!');\n" +
                        "} catch (e){\n" +
                        "  console.error('CAUGHT JS-EXCEPTION: ' + e.name + '\\n'" +
                        "                 + 'MESSAGE: ' + e.message + '\\n'" +
                        "                 + 'LINE: ' + e.lineNumber + '\\n'" +
                        "                 + 'STACK: ' + e.stack+'\\n');\n" +
                        "}\n"
                ;
            } else {
                jsCode = jsCode + "\n" +
                        "console.log('Execution of JS-Code(" + msStart + ") finished!');\n";
            }

            if (jsCode.contains("\n")) {
                debugOutput.println("Executing following JS-Code: ");
                debugOutput.println("JS-CODE START >");
                String singleLine = null;
                try (BufferedReader br = new BufferedReader(new StringReader(jsCode))) {
                    while ((singleLine = br.readLine()) != null) {
                        debugOutput.println(singleLine);
                    }
                }
                debugOutput.println("JS-CODE END <");
            } else {
                debugOutput.println("Executing following JS-Code: " + jsCode);
            }

            AtomicBoolean wasExecuted = new AtomicBoolean();
            List<String> errors = new ArrayList<>(2);
            Consumer<String> consoleLogListener = line -> {
                if (line.contains("" + msStart)) wasExecuted.set(true);
            };
            processInput.listeners.add(consoleLogListener);

            Consumer<String> consoleErrorLogListener = line -> errors.add(line);
            processErrorInput.listeners.add(consoleErrorLogListener);

            Files.write(tmpJs.toPath(), jsCode.getBytes(StandardCharsets.UTF_8));
            executeJavaScriptFromFile(tmpJs);
            debugOutput.println("Waiting for JavaScript result...");
            // Wait until we receive a response, like undefined

            for (int i = 0; i < timeout * 10; i++) { // Example timeout = 30s * 10 = 300loops * 100ms = 30000ms = 30s
                if (wasExecuted.get() || !errors.isEmpty())
                    break;
                else
                    Thread.sleep(100); // Total of 30 seconds
            }

            if (timeout == 0) { // Since the timeout is 0 we wait indefinitely for the script to finish
                while (!wasExecuted.get() && errors.isEmpty()) {
                    Thread.sleep(100);
                }
            }

            debugOutput.println("Took " + (System.currentTimeMillis() - msStart) + "ms.");

            if (!errors.isEmpty()) {
                throw new NodeJsCodeException("Error during JavaScript code execution! Details: ", errors);
            }

            if (!wasExecuted.get())
                throw new NodeJsCodeException("Script execution timeout of 30 seconds reached! This means that the script didn't finish within the last 30 seconds.", null);


            processErrorInput.listeners.remove(consoleErrorLogListener);
            processInput.listeners.remove(consoleLogListener);
            tmpJs.delete();
        } catch (NodeJsCodeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * See {@link #executeJavaScript(String, int, boolean)} for details.
     */
    public synchronized String executeJavaScriptAndGetResult(String jsCode) {
        return executeJavaScriptAndGetResult(jsCode, timeout, true);
    }

    /**
     * The provided jsCode must have this at the end: <br>
     * <pre>
     *     var result = InsertYourFunctionsResultHere;
     * </pre>
     * That result will get returned to this Java method. <br>
     * See {@link #executeJavaScript(String)} for details.
     *
     * @param timeout 30 seconds is the default, set to 0 to disable.
     */
    public synchronized String executeJavaScriptAndGetResult(String jsCode, int timeout, boolean wrapInTryCatch) {
        try {
            executeJavaScript(jsCode + "\n"
                    + "writeToJava(result);\n", timeout, wrapInTryCatch);

            StringBuilder result = new StringBuilder();
            String line = null;
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(lastJsCodeExecutionResultFile))) {
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line + "\n");
                }
            }

            // Clear the files content because we already got what we need
            try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(lastJsCodeExecutionResultFile))) {
                bufferedWriter.write("");
            }
            return result.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes JavaScript code from the provided {@link File} in the <br>
     * current {@link NodeContext}.
     */
    public NodeContext executeJavaScriptFromFile(File jsFile) throws IOException {
        writeLine(".load " + jsFile.getAbsolutePath());
        return this;
    }

    /**
     * See <br>
     * https://docs.npmjs.com/cli/v6/commands/npm-install <br>
     * for details. <br>
     */
    public NodeContext npmInstall() throws IOException, InterruptedException {
        return npmInstall(null);
    }

    public NodeContext npmInstall(String packageName) throws IOException, InterruptedException {
        debugOutput.println("[NPM-INSTALL] Installing '" + packageName + "'...");
        Process result;
        if (packageName != null)
            result = executeNpmWithArgs("install", packageName);
        else
            result = executeNpmWithArgs("install");
        if (result.exitValue() != 0)
            throw new IOException("Failed to install/download " + packageName + "!" +
                    " Npm finished with exit code '" + process.exitValue() + "'.");
        debugOutput.println("[NPM-INSTALL] Installed '" + packageName + "' successfully!");
        return this;
    }

    /**
     * Executes the "npm" command with the provided arguments. <br>
     * Waits until it finishes and then returns the {@link Process}. <br>
     * You can check if it was successful by checking if {@link Process#exitValue()} returns 0. <br>
     *
     * @param args
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public Process executeNpmWithArgs(String... args) throws IOException, InterruptedException {
        Objects.requireNonNull(args);
        File npm = new File(installationDir.listFiles()[0] + "/npm.cmd");
        List<String> commands = new ArrayList<>();
        commands.add(npm.getAbsolutePath());
        commands.addAll(Arrays.asList(args));
        Process process = new ProcessBuilder(commands).start();
        new AsyncInputStream(process.getInputStream()).listeners.add(line -> debugOutput.println("[NPM] " + line));
        new AsyncInputStream(process.getErrorStream()).listeners.add(line -> System.err.println("[NPM-ERROR] " + line));
        while (process.isAlive())
            Thread.sleep(500);
        return process;
    }

    /**
     * Events get fired when a line gets printed to the <br>
     * Node.js consoles {@link OutputStream}.<br>
     */
    public NodeContext onPrintLine(Consumer<String> listener) {
        processInput.listeners.add(listener);
        return this;
    }

    public File getInstallationDir() {
        return installationDir;
    }

    public File getExecutableFile() {
        return executableFile;
    }

    public Process getProcess() {
        return process;
    }

    public AsyncInputStream getProcessInput() {
        return processInput;
    }

    public OutputStream getProcessOutput() {
        return processOutput;
    }

    public PrintStream getDebugOutput() {
        return debugOutput;
    }

    public AsyncInputStream getProcessErrorInput() {
        return processErrorInput;
    }

    public File getLastJsCodeExecutionResultFile() {
        return lastJsCodeExecutionResultFile;
    }

    public int getTimeout() {
        return timeout;
    }

    public OperatingSystemArchitectureType getOsArchitectureType() {
        return osArchitectureType;
    }

    public OperatingSystemType getOsType() {
        return osType;
    }

    public enum OperatingSystemArchitectureType {
        X64("x64"),
        X86("x86"),
        X32("x32"),
        PPC64("ppc64"),
        PPC64LE("ppc64le"),
        S390X("s390x"),
        AARCH64("aarch64"),
        ARM("arm"),
        SPARCV9("sparcv9"),
        RISCV64("riscv64"),
        // x64 with alternative names:
        AMD64("x64"),
        X86_64("x64"),
        // x32 with alternative names:
        I386("x32");

        /**
         * Alternative name.
         */
        private final String altName;

        OperatingSystemArchitectureType(String altName) {
            this.altName = altName;
        }
    }

    public enum OperatingSystemType {
        LINUX("linux"),
        WINDOWS("windows"),
        MAC("mac"),
        SOLARIS("solaris"),
        AIX("aix"),
        ALPINE_LINUX("alpine-linux");

        private final String name;

        OperatingSystemType(String name) {
            this.name = name;
        }
    }
}
