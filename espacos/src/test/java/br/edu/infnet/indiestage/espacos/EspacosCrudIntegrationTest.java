package br.edu.infnet.indiestage.espacos;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.json.JsonMapper;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class EspacosCrudIntegrationTest {
    @LocalServerPort int porta;
    @Test
    void deveCadastrarConsultarAtualizarExcluirComStatusCoerentes() throws Exception {
        try(HttpClient client=HttpClient.newHttpClient()) {
            var criado=enviar(client,"POST","/api/espacos", """
                {"nome":"Teatro de teste","cidade":"Recife","capacidade":50,"disponivel":true}
                """);
            assertThat(criado.statusCode()).isEqualTo(201);
            String local=criado.headers().firstValue("Location").orElseThrow();
            assertThat(enviar(client,"GET",local,null).body()).contains("Teatro de teste");
            var atualizado=enviar(client,"PUT",local,"""
                {"nome":"Teatro atualizado","cidade":"Recife","capacidade":90,"disponivel":false}
                """);
            assertThat(atualizado.statusCode()).isEqualTo(200);
            var json=JsonMapper.builder().build().readTree(atualizado.body());
            assertThat(json.get("capacidade").asInt()).isEqualTo(90);
            assertThat(json.get("disponivel").asBoolean()).isFalse();
            assertThat(enviar(client,"DELETE",local,null).statusCode()).isEqualTo(204);
            assertThat(enviar(client,"GET",local,null).statusCode()).isEqualTo(404);
            assertThat(enviar(client,"PUT",local,"""
                {"nome":"Teatro","cidade":"Recife","capacidade":90,"disponivel":true}
                """).statusCode()).isEqualTo(404);
            assertThat(enviar(client,"DELETE",local,null).statusCode()).isEqualTo(404);
        }
    }
    @Test
    void deveRejeitarDadosInvalidosSemPersistir() throws Exception {
        try(HttpClient client=HttpClient.newHttpClient()) {
            var resposta=enviar(client,"POST","/api/espacos","""
                {"nome":" ","cidade":"","capacidade":0}
                """);
            assertThat(resposta.statusCode()).isEqualTo(400);
            assertThat(resposta.body()).contains("campos","nome","cidade","capacidade","disponivel");
            assertThat(enviar(client,"GET","/api/espacos/invalido",null).statusCode()).isEqualTo(400);
        }
    }
    @Test
    void devePaginarNoBancoEValidarParametros() throws Exception {
        try(HttpClient client=HttpClient.newHttpClient()) {
            var resposta=enviar(client,"GET","/api/espacos/paginados?pagina=0&tamanho=2",null);
            assertThat(resposta.statusCode()).isEqualTo(200);
            var pagina=JsonMapper.builder().build().readTree(resposta.body());
            assertThat(pagina.get("conteudo").size()).isEqualTo(2);
            assertThat(pagina.get("totalElementos").asLong()).isEqualTo(4);
            assertThat(pagina.get("totalPaginas").asInt()).isEqualTo(2);
            assertThat(enviar(client,"GET","/api/espacos/paginados?pagina=-1",null).statusCode()).isEqualTo(400);
            assertThat(enviar(client,"GET","/api/espacos/paginados?tamanho=101",null).statusCode()).isEqualTo(400);
        }
    }
    private HttpResponse<String> enviar(HttpClient client,String metodo,String path,String body) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:"+porta+path))
                .timeout(Duration.ofSeconds(10)).header("Content-Type","application/json")
                .method(metodo,body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(body))
                .build(),HttpResponse.BodyHandlers.ofString());
    }
}