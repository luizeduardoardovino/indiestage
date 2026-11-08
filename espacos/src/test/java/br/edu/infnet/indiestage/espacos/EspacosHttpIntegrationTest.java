package br.edu.infnet.indiestage.espacos;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.h2.console.enabled=true")
class EspacosHttpIntegrationTest {

    @Test
    void deveDisponibilizarPaginaDoConsoleH2() throws Exception {
        try (HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL).build()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/h2-console/"))
                    .timeout(Duration.ofSeconds(10)).GET().build();
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.body()).contains("H2 Console");
        }
    }
    @LocalServerPort
    private int port;

    @Test
    void deveListarEspacosComoJsonPorHttp() throws Exception {
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/api/espacos"))
                    .timeout(Duration.ofSeconds(10))
                    .GET().build();
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            assertThat(response.statusCode()).isEqualTo(200);
            assertThat(response.headers().firstValue("Content-Type").orElse(""))
                    .startsWith("application/json");
            JsonNode espacos = JsonMapper.builder().build().readTree(response.body());
            assertThat(espacos.isArray()).isTrue();
            assertThat(espacos.size()).isEqualTo(4);
            assertThat(espacos.get(0).get("id").asLong()).isEqualTo(1L);
            assertThat(espacos.get(0).get("nome").asText()).isEqualTo("Galpão 47");
            assertThat(espacos.get(1).get("cidade").asText()).isEqualTo("Niterói");
            assertThat(espacos.get(0).get("capacidade").asInt()).isEqualTo(120);
            assertThat(espacos.get(0).get("disponivel").asBoolean()).isTrue();
        }
    }
}