package com.osiris.headlessbrowser;

import com.osiris.betterthread.BetterThreadManager;
import com.osiris.headlessbrowser.utils.AsyncInputStream;
import com.osiris.headlessbrowser.utils.DownloadTask;
import net.lingala.zip4j.ZipFile;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class NodeContext implements AutoCloseable {
    private final File installationDir = new File(System.getProperty("user.dir") + "/NodeJS-Installation");
    private final Process process;
    private final AsyncInputStream processInput;
    private final OutputStream processOutput;
    private final PrintStream out = System.out;
    private File executableFile;
    private File lastJsCodeExecutionResultFile;

    public NodeContext() {
        // Download and install NodeJS into current working directory if no installation found
        try {
            if (!installationDir.exists()
                    || Objects.requireNonNull(installationDir.listFiles()).length == 0) {
                String url = "https://nodejs.org/dist/latest/";
                out.println("Installing latest NodeJS release from '" + url + "'...");
                Document docLatest = Jsoup.connect(url).get();
                String downloadUrl = null;
                for (Element e :
                        docLatest.getElementsByTag("a")) {
                    String attr = e.attr("href");
                    // TODO add support for other OS
                    if (attr.contains("win") && attr.contains("x64") && attr.contains(".zip")) {
                        downloadUrl = url + attr;
                        break;
                    }
                }

                if (downloadUrl == null)
                    throw new FileNotFoundException("Failed to find latest NodeJS download url at '" + url + "'.");

                // Download the zip file and extract its contents
                if (!installationDir.exists()) installationDir.mkdirs();
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
            processInput.listeners.add(line -> out.println("[" + this + "] " + line));
            new AsyncInputStream(process.getErrorStream()).listeners.add(line -> System.err.println("[Node-JS-ERROR] " + line));
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
        } catch (Exception e) {
            System.err.println("Error during start of NodeJS! Details:");
            throw new RuntimeException(e);
        }

        try {
            Thread.sleep(3000);
            lastJsCodeExecutionResultFile = new File(executableFile.getParentFile() + "/JavaScriptCodeResult.txt");
            if (!lastJsCodeExecutionResultFile.exists()) lastJsCodeExecutionResultFile.createNewFile();
            String resultFilePath = lastJsCodeExecutionResultFile.getAbsolutePath().replace("\\", "/"); // To avoid issues with indows file path formats
            executeJavaScript("var writeResultToJava = function(result) {\n" +
                    "var fs = require('fs')\n" +
                    "fs.writeFile('" + resultFilePath + "', result, err => {\n" + // the result var must be defined in the provided jsCode
                    "  if (err) {\n" +
                    "    console.error(err)\n" +
                    "    return\n" +
                    "  }\n" +
                    "  //file written successfully\n" +
                    "})\n" +
                    "};" +
                    "console.log('Context initialised!');");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() throws Exception {
        process.destroy();
    }

    public NodeContext writeLine(String line) throws IOException {
        if (line.contains("\n")) {
            synchronized (out) {
                out.println("Writing multiple lines to NodeJS context:");
                out.println("START >>>>>>>>>");
            }
            int lineNumber = 1;
            String singleLine = null;
            try (BufferedReader br = new BufferedReader(new StringReader(line))) {
                while ((singleLine = br.readLine()) != null) {
                    synchronized (out) {
                        out.println(lineNumber + "| " + singleLine);
                        lineNumber++;
                    }
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
            synchronized (out) {
                out.println("END <<<<<<<<<<<");
            }
        } else {
            synchronized (out) {
                out.println("Writing line to NodeJS context: " + line);
            }
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
     * Executes JavaScript code from the provided {@link String} in the <br>
     * current {@link NodeContext}.
     */
    public synchronized NodeContext executeJavaScript(String jsCode) {
        try {
            if (jsCode.contains("\n")) {
                synchronized (out) {
                    out.println("Executing following JS-Code: ");
                    out.println("JS-CODE START >");
                    String singleLine = null;
                    try (BufferedReader br = new BufferedReader(new StringReader(jsCode))) {
                        while ((singleLine = br.readLine()) != null) {
                            synchronized (out) {
                                out.println(singleLine);
                            }
                        }
                    }
                    out.println("JS-CODE END <");
                }
            } else {
                synchronized (out) {
                    out.println("Executing following JS-Code: " + jsCode);
                }
            }

            AtomicBoolean wasExecuted = new AtomicBoolean();
            Consumer<String> listener = line -> wasExecuted.set(true);
            processInput.listeners.add(listener);

            // Writing stuff directly to the process output/NodeJs REPL console somehow is very error-prone.
            // That's why instead we create a temp file with the js code in it and load it using the .load command.
            File tmpJs = new File(executableFile.getParentFile() + "/temp" + new Random().nextInt() + ".js");
            if (!tmpJs.exists()) tmpJs.createNewFile();
            Files.write(tmpJs.toPath(), jsCode.getBytes(StandardCharsets.UTF_8));
            executeJavaScript(tmpJs);

            // Wait until we receive a response, like undefined
            while (!wasExecuted.get()) {
                Thread.sleep(100);
            }

            processInput.listeners.remove(listener);
            tmpJs.delete();
        } catch (Exception e) {
            System.err.println("Error during JavaScript execution! Details: ");
            throw new RuntimeException(e);
        }
        return this;
    }

    /**
     * The provided jsCode must have this at the end: <br>
     * <pre>
     *     var result = InsertYourFunctionsResultHere;
     * </pre>
     * That result will get returned to this Java method.
     */
    public String executeJavaScriptAndGetResult(String jsCode) {
        try {
            executeJavaScript(jsCode);
            executeJavaScript("writeResultToJava(result);\n");

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
    public NodeContext executeJavaScript(File jsFile) throws IOException {
        writeLine(".load " + jsFile.getAbsolutePath());
        return this;
    }


    public NodeContext npmInstall(String packageName) throws IOException, InterruptedException {
        synchronized (out) {
            out.println("[NPM-INSTALL] Installing '" + packageName + "'...");
        }
        Process result = executeNpmWithArgs("install", packageName);
        if (result.exitValue() != 0)
            throw new IOException("Failed to install/download " + packageName + "!" +
                    " Npm finished with exit code '" + process.exitValue() + "'.");
        synchronized (out) {
            out.println("[NPM-INSTALL] Installed '" + packageName + "' successfully!");
        }
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
        new AsyncInputStream(process.getInputStream()).listeners.add(line -> out.println("[NPM] " + line));
        new AsyncInputStream(process.getErrorStream()).listeners.add(line -> System.err.println("[NPM-ERROR] " + line));
        while (process.isAlive())
            Thread.sleep(500);
        return process;
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

    public PrintStream getOut() {
        return out;
    }
}
