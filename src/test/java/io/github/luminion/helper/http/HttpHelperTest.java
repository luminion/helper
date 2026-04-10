package io.github.luminion.helper.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpHelperTest {

    private HttpServer httpServer;
    private String baseUrl;

    @BeforeEach
    void setUp() throws Exception {
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        httpServer.createContext("/echo", new EchoHandler());
        httpServer.start();
        baseUrl = "http://127.0.0.1:" + httpServer.getAddress().getPort();
    }

    @AfterEach
    void tearDown() {
        if (httpServer != null) {
            httpServer.stop(0);
        }
    }

    @Test
    void shouldAppendQueryParamsWithoutExtraAmpersand() {
        String response = HttpHelper.get(baseUrl + "/echo?")
                .queryParam("a", "1")
                .responseString(StandardCharsets.UTF_8);

        assertTrue(response.contains("query=a=1"));
    }

    @Test
    void shouldSendBodyAndReadResponse() {
        String response = HttpHelper.post(baseUrl + "/echo")
                .bodyParam("payload")
                .responseString(StandardCharsets.UTF_8);

        assertTrue(response.contains("method=POST"));
        assertTrue(response.contains("body=payload"));
    }

    @Test
    void shouldRejectFormAndBodyAtTheSameTime() {
        HttpHelper helper = HttpHelper.post(baseUrl + "/echo").formParam("k", "v");
        assertThrows(IllegalStateException.class, new org.junit.jupiter.api.function.Executable() {
            @Override
            public void execute() {
                helper.bodyParam("payload");
            }
        });
    }

    private static class EchoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] requestBody = readAll(exchange.getRequestBody());
            String response = "method=" + exchange.getRequestMethod()
                    + ";query=" + exchange.getRequestURI().getRawQuery()
                    + ";body=" + new String(requestBody, StandardCharsets.UTF_8);
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            try (OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(responseBytes);
            }
        }

        private byte[] readAll(InputStream inputStream) throws IOException {
            byte[] buffer = new byte[1024];
            int length;
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            return outputStream.toByteArray();
        }
    }
}
