package org.tuiken;

import static org.junit.jupiter.api.Assertions.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

class MainTest {

    private static Server server;


    @org.junit.jupiter.api.BeforeEach
    void startServer() throws Exception {
        server = new Server(8998);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.addServlet(new ServletHolder(new HelloServlet()), "/hello");
        server.setHandler(context);
        server.start();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() throws Exception {
        server.stop();
    }

    @org.junit.jupiter.api.Test
    void main() throws IOException {
        Main.main(new String[]{"data/input_test.json"});
        String fileContent = new String(Files.readAllBytes(Paths.get("data/files/hello")));
        assertEquals("Hello!", fileContent);
    }
}