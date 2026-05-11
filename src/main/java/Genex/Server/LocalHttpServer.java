package Genex.Server;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class LocalHttpServer {
    private static HttpServer server;
    private static final int PORT = 7654;

    public static void start() {
        if (server != null) return;

        try {
            server = HttpServer.create(new InetSocketAddress("localhost",PORT), 0);
            // Serve captcha HTML
            server.createContext("/captcha.html", new StaticFileHandler("/html/captcha.html"));
            server.createContext("/tournament-map.html", new StaticFileHandler("/html/tournament-map.html"));

            // Verification endpoint
            server.createContext("/api/verify-captcha", new CaptchaHandler());

            server.setExecutor(Executors.newFixedThreadPool(4));
            server.start();

            System.out.println("✅ Local HTTP Server started on port " + PORT);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("[Cleanup Server] Server stopped.");
        }
    }
}
