package br.edu.infnet.indiestage.eventos;
import com.sun.net.httpserver.HttpServer;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import tools.jackson.databind.json.JsonMapper;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class EventoDetalhesIntegrationTest {
    private static final AtomicInteger resposta=new AtomicInteger(200);
    private static final HttpServer espacos=iniciarServidor();
    @LocalServerPort int port;
    @DynamicPropertySource
    static void descoberta(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.discovery.client.simple.instances.espacos[0].uri",
                ()->"http://127.0.0.1:"+espacos.getAddress().getPort());
    }
    @AfterAll static void fechar(){espacos.stop(0);}
    @Test
    void deveComporDetalhesEDistinguirEspacoInexistenteDeIndisponivel() throws Exception {
        try(HttpClient client=HttpClient.newHttpClient()) {
            var normal=consultar(client,"/api/eventos/1/detalhes");
            assertThat(normal.statusCode()).isEqualTo(200);
            var json=JsonMapper.builder().build().readTree(normal.body());
            assertThat(json.get("nome").asText()).isEqualTo("Jazz no Rooftop");
            assertThat(json.get("espaco").get("nome").asText()).isEqualTo("Galpão de integração");
            assertThat(json.get("espaco").get("status").asText()).isEqualTo("DISPONIVEL");
            resposta.set(404);
            assertThat(consultar(client,"/api/eventos/1/detalhes").body()).contains("NAO_ENCONTRADO");
            resposta.set(503);
            var fallback=consultar(client,"/api/eventos/1/detalhes");
            assertThat(fallback.statusCode()).isEqualTo(200);
            assertThat(fallback.body()).contains("TEMPORARIAMENTE_INDISPONIVEL","Jazz no Rooftop");
            assertThat(consultar(client,"/api/eventos/999999/detalhes").statusCode()).isEqualTo(404);
        } finally {resposta.set(200);}
    }
    private HttpResponse<String> consultar(HttpClient client,String caminho)throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+caminho)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }
    private static HttpServer iniciarServidor() {
        try {
            HttpServer server=HttpServer.create(new InetSocketAddress("127.0.0.1",0),0);
            server.createContext("/api/espacos/",exchange->{
                byte[] body="{\"id\":1,\"nome\":\"Galpão de integração\",\"cidade\":\"Recife\",\"capacidade\":80,\"disponivel\":true}"
                        .getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type","application/json");
                exchange.sendResponseHeaders(resposta.get(),body.length);
                try(var output=exchange.getResponseBody()){output.write(body);}
            });
            server.start();return server;
        } catch(java.io.IOException e){throw new IllegalStateException(e);}
    }
}