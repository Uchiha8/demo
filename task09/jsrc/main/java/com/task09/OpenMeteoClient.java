package com.task09;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OpenMeteoClient {

    private final String CURRENT_WEATHER_URI = "https://api.open-meteo.com/v1/forecast?latitude=41.2647&longitude=69.2163&hourly=temperature_2m";

    public HttpResponse<String> getCurrentWeather() {

        HttpRequest request = null;
        HttpResponse<String> response;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI(CURRENT_WEATHER_URI))
                    .GET()
                    .build();

            response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return response;

    }

}
