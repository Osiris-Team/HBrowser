package com.osiris.headlessbrowser.utils;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class Utils {

    public static void moveDirectoryContent(File source, File target) throws IOException {
        File[] files = source.listFiles();
        if (files != null) {
            for (File file : files) {
                File destFile = new File(target, file.getName());
                if (file.isDirectory()) {
                    moveDirectoryContent(file, destFile);
                    FileUtils.deleteDirectory(file); // Optionally delete the source directory after moving its content
                } else {
                    if(!destFile.getParentFile().exists()) destFile.getParentFile().mkdirs();
                    Files.move(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    public static <T> String toString(T[] arr){
        StringBuilder sb = new StringBuilder("[ ");
        for (T t : arr) {
            sb.append("'").append(t).append("' ");
        }
        sb.append("]");
        return sb.toString();
    }

    public static <T> String toString(List<T> list){
        StringBuilder sb = new StringBuilder("[ ");
        for (T t : list) {
            sb.append("'").append(t).append("' ");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Finds the wanted file inside this jar file and returns its {@link InputStream}.
     *
     * @param filePathInJar The wanted files relative path inside of the jar.
     * @return the files {@link InputStream}.
     */
    public static InputStream getFileFromThisJar(String filePathInJar) throws Exception {
        return getFileFromJar(filePathInJar, Utils.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI()
                .getPath());
    }

    /**
     * Reads the file and returns its content as string.
     */
    public static String getFileContentFromThisJar(String filePathInJar) throws Exception {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(getFileFromThisJar(filePathInJar)));
            StringBuilder result = new StringBuilder();
            String line = "";
            while ((line = reader.readLine()) != null) {
                result.append(line + "\n");
            }
            reader.close();
            return result.toString();
        } catch (Exception e) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
            throw e;
        }
    }

    /**
     * Finds the wanted file inside a jar file and returns its {@link InputStream}.
     *
     * @param filePathInJar The wanted files relative path inside of the jar.
     * @param pathOfJar     The jars absolute path.
     * @return the files {@link InputStream}.
     */
    public static InputStream getFileFromJar(String filePathInJar, String pathOfJar) throws FileNotFoundException, MalformedURLException {
        Objects.requireNonNull(pathOfJar);
        File jarFile = new File(pathOfJar);
        if (jarFile.exists()) {
            Collection<URL> urls = new ArrayList<URL>();
            urls.add(jarFile.toURI().toURL());
            URLClassLoader fileClassLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]));
            return fileClassLoader.getResourceAsStream(filePathInJar);
        } else
            throw new FileNotFoundException("Provided jar file with path '" + pathOfJar + "' doesnt exist!");
    }


}
