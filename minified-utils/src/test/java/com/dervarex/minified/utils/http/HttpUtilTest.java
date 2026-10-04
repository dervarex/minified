package com.dervarex.minified.utils.http;

import com.dervarex.minified.utils.exceptions.HttpException;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class HttpUtilTest {
    private HttpServer server;
    private String baseUrl;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/ok", exchange -> {
            byte[] body = "{\"ok\":true}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.createContext("/error", exchange -> {
            byte[] body = "nope".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(500, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.createContext("/echo", exchange -> {
            byte[] body = (exchange.getRequestMethod() + " " + exchange.getRequestHeaders().getFirst("Content-Type") + " "
                    + new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.createContext("/busy", exchange -> {
            byte[] body = "come back later".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("X-Request-Id", "abc-123");
            exchange.sendResponseHeaders(503, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort();
    }

    @AfterEach
    void tearDown() {
        if (server != null) server.stop(0);
    }

    @Test
    void getReturnsBody() throws Exception {
        String body = HttpUtil.get(baseUrl + "/ok");
        assertEquals("{\"ok\":true}", body);
    }

    @Test
    void getThrowsOnNonSuccess() {
        HttpException ex = assertThrows(HttpException.class, () -> HttpUtil.get(baseUrl + "/error"));
        assertNotNull(ex);
        assertEquals(500, ex.getStatusCode());
    }

    @Test
    void requestJsonSendsTheBodyAsJson() throws Exception {
        HttpResponse response = HttpUtil.requestJson("POST", baseUrl + "/echo", "{\"hello\":true}");

        assertEquals(200, response.statusCode());
        assertEquals("POST application/json {\"hello\":true}", response.getBodyAsString());
    }

    @Test
    void errorsKeepWhatTheServerSaid() {
        HttpException ex = assertThrows(HttpException.class, () -> HttpUtil.get(baseUrl + "/busy"));

        assertTrue(ex.isTransientFailure());
        assertEquals(HttpException.Method.GET, ex.getMethod());
        assertEquals(baseUrl + "/busy", ex.getUrl());
        assertEquals("come back later", ex.responseSnippet(100));
        // the test server sends it as "X-request-id", real ones send whatever they like
        assertEquals("abc-123", ex.getRequestId());
    }
}
