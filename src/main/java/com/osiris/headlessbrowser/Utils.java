package com.osiris.headlessbrowser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class Utils {

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
