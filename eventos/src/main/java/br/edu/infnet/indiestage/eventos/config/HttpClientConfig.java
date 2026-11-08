package br.edu.infnet.indiestage.eventos.config;
import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
@Configuration
public class HttpClientConfig {
    @Bean @org.springframework.context.annotation.Primary
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
    @Bean @LoadBalanced
    public RestClient.Builder espacosRestClientBuilder() {
        HttpClient http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build();
        JdkClientHttpRequestFactory factory=new JdkClientHttpRequestFactory(http);
        factory.setReadTimeout(Duration.ofSeconds(2));
        return RestClient.builder().requestFactory(factory);
    }
}