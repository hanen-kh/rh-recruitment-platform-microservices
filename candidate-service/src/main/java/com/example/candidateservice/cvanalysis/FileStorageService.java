package com.example.candidateservice.cvanalysis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@Service
public class FileStorageService {

    private final RestClient tikaClient;

    public FileStorageService(
            RestClient.Builder restClientBuilder,
            @Value("${app.tika-server-url:http://localhost:9998}") String tikaServerUrl,
            @Value("${app.tika.connect-timeout:5s}") Duration connectTimeout,
            @Value("${app.tika.read-timeout:2m}") Duration readTimeout
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);
        this.tikaClient = restClientBuilder.clone()
                .baseUrl(tikaServerUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public String extractText(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.isFile()) {
            throw new IOException("Fichier CV introuvable au chemin : " + filePath);
        }

        try {
            MediaType contentType = MediaTypeFactory.getMediaType(file.getName())
                    .orElse(MediaType.APPLICATION_OCTET_STREAM);
            return tikaClient.put()
                    .uri("/tika")
                    .contentType(contentType)
                    .accept(MediaType.TEXT_PLAIN)
                    .body(new FileSystemResource(file))
                    .retrieve()
                    .body(String.class);
        } catch (ResourceAccessException exception) {
            throw new ResponseStatusException(
                    SERVICE_UNAVAILABLE, "Le serveur Tika est indisponible.", exception);
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().is4xxClientError()) {
                throw new ResponseStatusException(
                        BAD_REQUEST,
                        "Tika ne peut pas analyser ce fichier (HTTP " + exception.getStatusCode().value() + ").",
                        exception
                );
            }
            throw new ResponseStatusException(BAD_GATEWAY, "Le serveur Tika a retourne une erreur.", exception);
        }
    }
}
