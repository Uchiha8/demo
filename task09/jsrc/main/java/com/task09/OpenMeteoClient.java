package com.task09;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OpenMeteoClient {
    public HttpResponse<String> getCurrentWeather() {
        HttpRequest request;
        HttpResponse<String> response;
        try {
            String CURRENT_WEATHER_URI = "https://api.open-meteo.com/v1/forecast?latitude=52.52&longitude=13.41&current=temperature_2m,wind_speed_10m&hourly=temperature_2m,relative_humidity_2m,wind_speed_10m";
            request = HttpRequest.newBuilder()
                    .uri(new URI(CURRENT_WEATHER_URI))
                    .GET()
                    .build();
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return response;
    }
}
