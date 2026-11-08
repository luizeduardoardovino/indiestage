package br.edu.infnet.indiestage.eventos;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.json.JsonMapper;
import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class EventosIntegrationTest {
    @LocalServerPort int porta;
    private final String cadastro="""
        {"nome":"Evento teste","descricao":"Descrição de teste","categoria":"MUSICA",
         "dataHora":"2026-12-10T20:00:00","espacoId":1,"situacao":"AGENDADO"}
        """;
    @Test
    void deveCadastrarConsultarAtualizarEExcluirEvento() throws Exception {
        try(HttpClient client=HttpClient.newHttpClient()) {
            var criado=enviar(client,"POST","/api/eventos",cadastro);
            assertThat(criado.statusCode()).isEqualTo(201);
            String local=criado.headers().firstValue("Location").orElseThrow();
            var consulta=enviar(client,"GET",local,null);
            assertThat(consulta.statusCode()).isEqualTo(200);
            assertThat(consulta.body()).contains("Evento teste","2026-12-10T20:00:00");
            var atualizado=enviar(client,"PUT",local,cadastro.replace("AGENDADO","CANCELADO"));
            assertThat(atualizado.statusCode()).isEqualTo(200);
            assertThat(atualizado.body()).contains("CANCELADO");
            assertThat(enviar(client,"DELETE",local,null).statusCode()).isEqualTo(204);
            assertThat(enviar(client,"GET",local,null).statusCode()).isEqualTo(404);
            assertThat(enviar(client,"PUT",local,cadastro).statusCode()).isEqualTo(404);
            assertThat(enviar(client,"DELETE",local,null).statusCode()).isEqualTo(404);
        }
    }
    @Test
    void devePaginarDadosIniciais() throws Exception {
        try(HttpClient client=HttpClient.newHttpClient()) {
            var resposta=enviar(client,"GET","/api/eventos/paginados?pagina=0&tamanho=2",null);
            assertThat(resposta.statusCode()).isEqualTo(200);
            var pagina=JsonMapper.builder().build().readTree(resposta.body());
            assertThat(pagina.get("totalElementos").asLong()).isEqualTo(4);
            assertThat(pagina.get("conteudo").size()).isEqualTo(2);
            assertThat(pagina.get("conteudo").get(0).get("nome").asText()).isEqualTo("Jazz no Rooftop");
            assertThat(enviar(client,"GET","/api/eventos/paginados?pagina=-1",null).statusCode()).isEqualTo(400);
        }
    }
    @Test
    void deveRejeitarDadosInvalidos() throws Exception {
        try(HttpClient client=HttpClient.newHttpClient()) {
            var resposta=enviar(client,"POST","/api/eventos","{}");
            assertThat(resposta.statusCode()).isEqualTo(400);
            assertThat(resposta.body()).contains("campos","nome","dataHora","espacoId");
            assertThat(enviar(client,"POST","/api/eventos",cadastro.replace("AGENDADO","INVALIDO"))
                    .statusCode()).isEqualTo(400);
            assertThat(enviar(client,"POST","/api/eventos",cadastro.replace("\"espacoId\":1","\"espacoId\":0"))
                    .statusCode()).isEqualTo(400);
        }
    }
    private HttpResponse<String> enviar(HttpClient client,String metodo,String path,String body) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:"+porta+path))
            .timeout(Duration.ofSeconds(10)).header("Content-Type","application/json")
            .method(metodo,body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(body))
            .build(),HttpResponse.BodyHandlers.ofString());
    }
}