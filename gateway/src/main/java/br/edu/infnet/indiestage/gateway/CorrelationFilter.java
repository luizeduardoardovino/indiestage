package br.edu.infnet.indiestage.gateway;
import java.util.UUID;
import org.slf4j.*;
import org.springframework.cloud.gateway.filter.*;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
@Component
public class CorrelationFilter implements GlobalFilter, Ordered {
    private static final Logger log=LoggerFactory.getLogger(CorrelationFilter.class);
    @Override public int getOrder(){return -100;}
    @Override public Mono<Void> filter(ServerWebExchange exchange,GatewayFilterChain chain){
        String recebido=exchange.getRequest().getHeaders().getFirst("X-Correlation-Id");
        String id=recebido!=null&&recebido.matches("[a-zA-Z0-9._-]{1,80}")?recebido:UUID.randomUUID().toString();
        exchange.getResponse().getHeaders().set("X-Correlation-Id",id);
        var alterado=exchange.mutate().request(exchange.getRequest().mutate()
                .header("X-Correlation-Id",id).build()).build();
        return chain.filter(alterado).doFinally(sinal->log.info("correlationId={} metodo={} rota={} status={}",
                id,exchange.getRequest().getMethod(),exchange.getRequest().getPath(),
                exchange.getResponse().getStatusCode()));
    }
}