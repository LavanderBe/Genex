package Genex.Server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StaticFileHandler implements HttpHandler {

    private final String resourcePath;

    public StaticFileHandler(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = resourcePath;

        try (InputStream is = LocalHttpServer.class.getResourceAsStream(path)) {
            if (is == null) {
                exchange.sendResponseHeaders(404, 0);
                return;
            }

            byte[] bytes = is.readAllBytes();
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}