package com.osiris.headlessbrowser;

import com.osiris.betterthread.BetterThreadManager;
import com.osiris.headlessbrowser.utils.DownloadTask;
import net.lingala.zip4j.ZipFile;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;

public class NodeInstaller {

    public static void main(String[] args) throws IOException, InterruptedException {
        new NodeInstaller().installLatestToWorkingDir();
    }

    public void installLatestToWorkingDir() throws IOException, InterruptedException {
        String url = "https://nodejs.org/dist/latest/";
        Document docLatest = Jsoup.connect(url).get();
        String downloadUrl = null;
        for (Element e :
                docLatest.getElementsByTag("a")) {
            String attr = e.attr("href");
            if (attr.contains("win") && attr.contains("x64") && attr.contains(".zip")){
                downloadUrl = url + attr;
                break;
            }
        }

        if (downloadUrl==null)
            throw new FileNotFoundException("Failed to find latest NodeJS download url at '"+url+"'.");

        // Download the zip file and extract its contents
        System.out.println(downloadUrl);
        File nodeJsDir = new File(System.getProperty("user.dir")+"/NodeJS-Installation");
        if (!nodeJsDir.exists()) nodeJsDir.mkdirs();
        File downloadZip = new File(nodeJsDir+"/download.zip");
        if (downloadZip.exists()) downloadZip.delete();
        downloadZip.createNewFile();
        DownloadTask downloadTask = new DownloadTask("Download", new BetterThreadManager(), downloadUrl, downloadZip, "zip");
        downloadTask.start();
        while(!downloadTask.isFinished())
            Thread.sleep(100);
        ZipFile zipFile = new ZipFile(downloadZip);
        zipFile.extractFile(zipFile.getFileHeaders().get(0).getFileName(), nodeJsDir.getPath());
        downloadZip.delete();
        System.out.println(downloadZip);

        ProcessBuilder processBuilder = new ProcessBuilder(commands); // The commands list contains all we need.
        processBuilder.redirectErrorStream(true);
        //processBuilder.inheritIO(); // BACK TO PIPED, BECAUSE OF MASSIVE ERRORS LIKE COMMANDS NOT BEEING EXECUTED, which affects the restarter
        processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);
        process = processBuilder.start();

        // TODO install pupeteer
        // TODO execute js code in nodeJS
    }

}
