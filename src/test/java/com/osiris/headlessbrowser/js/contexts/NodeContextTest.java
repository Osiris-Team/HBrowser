package com.osiris.headlessbrowser.js.contexts;

import com.osiris.headlessbrowser.Versions;
import com.osiris.headlessbrowser.utils.OS;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class NodeContextTest {

    @Test
    void testPath() throws IOException, InterruptedException {
        File parent = new File(System.getProperty("user.dir")+"/headless-browser/test-node");
        NodeContext ctx = new NodeContext(parent, System.out, 30); // Installs and starts Node.js if not exists
        ctx.npmInstall("test");
    }

    @Test
    void install() throws Exception {
        //OS.TYPE = OS.Type.LINUX; // Try for a custom OS if needed
        File parent = new File(System.getProperty("user.dir")+"/headless-browser/test-node-temp");
        FileUtils.deleteDirectory(parent);
        parent.mkdirs();
        try{
            NodeContext ctx = new NodeContext(parent, System.out, 30); // Installs and starts Node.js if not exists
            ctx.install(Versions.NODEJS, true);
        } catch (Exception e) {
            throw e;
        } finally {
            FileUtils.deleteDirectory(parent);
        }
    }
}