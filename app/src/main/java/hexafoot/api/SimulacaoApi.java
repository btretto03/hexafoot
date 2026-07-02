package hexafoot.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class SimulacaoApi {
    public static void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/simulacao", new SimulacaoHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("API disponível em http://localhost:" + port + "/api/simulacao");
    }

    static class SimulacaoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String response = """
            {
              "home": "Brasil",
              "away": "Argentina",
              "score": {"home": 2, "away": 1},
              "minute": "35'",
              "event": "Gol de Vinícius Jr.",
              "tactic": "Equilibrada",
              "possession": {"home": 58, "away": 42},
              "shots": {"home": 11, "away": 7},
              "highlights": [
                "Pressão alta",
                "Passe longo",
                "Finalização no travessão"
              ]
            }
            """;

            byte[] data = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, data.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(data);
            }
        }
    }
}
