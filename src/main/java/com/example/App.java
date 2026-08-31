package com.example;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class App {

    public static String getMessage() {
        return "Hello from Jenkins CI/CD!";
    }

    public static void main(String[] args) throws IOException {

        HttpServer server =
                HttpServer.create(new InetSocketAddress(8081), 0);

        server.createContext("/", exchange -> {

            String response = getMessage();

            exchange.sendResponseHeaders(
                    200,
                    response.getBytes().length
            );

            OutputStream outputStream =
                    exchange.getResponseBody();

            outputStream.write(response.getBytes());
            outputStream.close();
        });

        server.start();

        System.out.println(
                "Server started on http://localhost:8081"
        );
    }
}
