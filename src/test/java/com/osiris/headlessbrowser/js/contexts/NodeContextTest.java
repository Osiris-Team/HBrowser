package com.osiris.headlessbrowser.js.contexts;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class NodeContextTest {

    @Test
    void install() throws Exception {
        NodeContext ctx = new NodeContext(null, System.out, 30); // Installs and starts Node.js if not exists
        ctx.install(true);
    }
}