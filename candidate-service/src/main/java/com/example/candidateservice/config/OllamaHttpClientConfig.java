package com.example.candidateservice.config;

import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class OllamaHttpClientConfig {

    @Bean
    OllamaApi ollamaApi(
            @Value("${spring.ai.ollama.base-url:http://localhost:11434}") String baseUrl,
            @Value("${app.ollama.connect-timeout:5s}") Duration connectTimeout,
            @Value("${app.ollama.read-timeout:5m}") Duration readTimeout,
            ObjectProvider<RestClient.Builder> restClientBuilderProvider,
            ObjectProvider<WebClient.Builder> webClientBuilderProvider,
            ObjectProvider<ResponseErrorHandler> responseErrorHandlerProvider
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);

        RestClient.Builder restClientBuilder = restClientBuilderProvider
                .getIfAvailable(RestClient::builder)
                .clone()
                .requestFactory(requestFactory);
        WebClient.Builder webClientBuilder = webClientBuilderProvider
                .getIfAvailable(WebClient::builder);

        return OllamaApi.builder()
                .baseUrl(baseUrl)
                .restClientBuilder(restClientBuilder)
                .webClientBuilder(webClientBuilder)
                .responseErrorHandler(responseErrorHandlerProvider.getIfAvailable(
                        () -> RetryUtils.DEFAULT_RESPONSE_ERROR_HANDLER))
                .build();
    }
}
