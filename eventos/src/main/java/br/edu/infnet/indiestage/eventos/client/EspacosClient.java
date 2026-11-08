package br.edu.infnet.indiestage.eventos.client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import java.util.UUID;
@Component
public class EspacosClient {
    private static final Logger log=LoggerFactory.getLogger(EspacosClient.class);
    private final RestClient http;
    private final CircuitBreakerFactory<?,?> circuits;
    public EspacosClient(@org.springframework.beans.factory.annotation.Qualifier("espacosRestClientBuilder") RestClient.Builder espacosRestClientBuilder,CircuitBreakerFactory<?,?> circuits) {
        this.http=espacosRestClientBuilder.build(); this.circuits=circuits;
    }
    public EspacoInfo consultar(Long id) {
        String atual=MDC.get("correlationId");
        String correlacao=atual==null?UUID.randomUUID().toString():atual;
        return circuits.create("espacos").run(()->{
            try {
                DadosEspaco dados=http.get().uri("http://espacos/api/espacos/{id}",id)
                        .header("X-Correlation-Id",correlacao).retrieve().body(DadosEspaco.class);
                if(dados==null) throw new IllegalStateException("Resposta vazia de Espaços");
                return new EspacoInfo(dados.id(),"DISPONIVEL",dados.nome(),dados.cidade(),
                        dados.capacidade(),dados.disponivel());
            } catch(HttpClientErrorException.NotFound e) {
                return EspacoInfo.inexistente(id);
            }
        },falha->{
            Throwable causa=falha;
            while(causa.getCause()!=null) causa=causa.getCause();
            log.warn("correlationId={} operacao=consultarEspaco espacoId={} resultado=fallback causa={}",
                    correlacao,id,causa.getClass().getSimpleName());
            return EspacoInfo.indisponivel(id);
        });
    }
    private record DadosEspaco(Long id,String nome,String cidade,int capacidade,boolean disponivel) {}
}