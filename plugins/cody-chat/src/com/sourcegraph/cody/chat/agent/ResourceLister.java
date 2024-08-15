package com.sourcegraph.cody.chat.agent;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.*;
import java.util.List;

public class ResourceLister {

    public static List<String> listResourceFiles(String path) {
        List<String> filenames = new ArrayList<>();

        // Adjusted path to ensure it's absolute
        String adjustedPath = path.startsWith("/") ? path : "/" + path;
        URL dirURL = ResourceLister.class.getResource(adjustedPath);

        if (dirURL != null && dirURL.getProtocol().equals("file")) {
            /* A file path: easy enough */
            try {
                filenames = listFilesUsingFile(dirURL);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (dirURL == null) {
        /* In case of a jar file, we can't actually find a directory.
           Have to assume the same jar as clazz. */
            String me = ResourceLister.class.getName().replace(".", "/") + ".class";
            dirURL = ResourceLister.class.getClassLoader().getResource(me);
            if (dirURL.getProtocol().equals("jar")) {
                /* A JAR path */
                String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
                try (var jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"))) {
                    Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
                    while (entries.hasMoreElements()) {
                        String name = entries.nextElement().getName();
                        if (name.startsWith(path.substring(1))) { //filter according to the path
                            String entry = name.substring(path.length());
                            int checkSubdir = entry.indexOf("/");
                            if (checkSubdir >= 0) {
                                // if it is a subdirectory, we just return the directory name
                                entry = entry.substring(0, checkSubdir);
                            }
                            if (!entry.isEmpty() && !filenames.contains(entry)) {
                                filenames.add(entry);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return filenames;
    }

    private static List<String> listFilesUsingFile(URL dirURL) throws IOException {
        java.io.File f = new java.io.File(dirURL.getFile());
        List<String> filenames = new ArrayList<>();
        if (f.exists() && f.isDirectory()) {
            java.io.File[] files = f.listFiles();
            if (files != null) {
                for (java.io.File file : files) {
                    filenames.add(file.getName());
                }
            }
        }
        return filenames;
    }
}

