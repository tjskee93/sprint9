package ru.yandex.practicum.mybank.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    @Value("${bank.gateway-url:http://gateway:8081}")
    private String gatewayUrl;
    @Bean
    RestClient gatewayRestClient(RestClient.Builder builder) {
        return builder.baseUrl(gatewayUrl).build();
    }
}
