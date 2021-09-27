package com.osiris.headlessbrowser;

import com.osiris.betterthread.BetterThreadManager;
import com.osiris.headlessbrowser.utils.AsyncInputStream;
import com.osiris.headlessbrowser.utils.DownloadTask;
import net.lingala.zip4j.ZipFile;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Objects;

public class NodeJSContext {
    private File installationDir = new File(System.getProperty("user.dir") + "/NodeJS-Installation");
    private File executableFile;
    private Process process;
    private AsyncInputStream processInput;

    private PrintStream out = System.out;

    public static void main(String[] args) {
        new NodeJSContext();
    }

    public NodeJSContext() {
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
                    installationDir.listFiles()[0].listFiles()) {
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
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
            Objects.requireNonNull(process.getInputStream());
            Objects.requireNonNull(process.getOutputStream());
            // Somehow the I/O from the node.exe cannot be read?
            // Tried multiple things without success.
            // Update: Node.exe must be started with this flag to get correct I/O: --interactive
            processInput = new AsyncInputStream(process.getInputStream());
            processInput.listeners.add(line -> out.println("[Node-JS] " + line));
            out.println(" SUCCESS!");
            out.println("Node-JS was started from: " + executableFile);

            Thread t = new Thread(() -> {
                // Keeps the java application running until NodeJs closes
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
            // TODO check for existing installation
            out.println("Installing Puppeteer. This may take a bit.");
            File npm = new File(installationDir.listFiles()[0] + "/npm.cmd");
            Process process = new ProcessBuilder(
                    npm.getAbsolutePath(),
                    "install",
                    "Puppeteer").start();
            new AsyncInputStream(process.getInputStream()).listeners.add(line -> out.println("[NPM] " + line));
            while (process.isAlive())
                Thread.sleep(500);
            if (process.exitValue() != 0)
                throw new Exception("Failed to install Puppeteer!" +
                        " Npm finished with exit code '" + process.exitValue() + "'. Npm path: " + npm.getName());
        } catch (Exception e) {
            System.err.println("Error during Puppeteer installation! Details: ");
            e.printStackTrace();
        }

    }
}
