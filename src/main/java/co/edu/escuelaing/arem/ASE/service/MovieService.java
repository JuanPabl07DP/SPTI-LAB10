package co.edu.escuelaing.arem.ASE.service;

import co.edu.escuelaing.arem.ASE.model.Movie;
import com.google.gson.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MovieService {
    private static final String SWAPI_URL = "https://swapi.py4e.com/api/films/";
    private static final Logger logger = Logger.getLogger(MovieService.class.getName());
    private final HttpClient client;
    private final Gson gson;

    public MovieService() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    public Movie getMovieById(String id) throws MovieServiceException {
        try {
            validateId(id);

            // Construir la URL específica para la película
            String url = SWAPI_URL + "?format=json";
            logger.info("Requesting URL: " + url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                logger.severe("Error response from API: " + response.statusCode());
                throw new MovieServiceException("Error del servidor: " + response.statusCode());
            }

            String responseBody = response.body();
            logger.info("API Response: " + responseBody);

            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            JsonArray results = jsonResponse.getAsJsonArray("results");

            int searchId = Integer.parseInt(id);

            for (JsonElement element : results) {
                JsonObject movieJson = element.getAsJsonObject();
                if (movieJson.has("episode_id")) {
                    int episodeId = movieJson.get("episode_id").getAsInt();
                    logger.info("Checking episode_id: " + episodeId);

                    if (episodeId == searchId) {
                        Movie movie = gson.fromJson(movieJson, Movie.class);
                        logger.info("Found movie: " + movie);
                        return movie;
                    }
                }
            }

            logger.warning("Movie not found with episode_id: " + id);
            throw new MovieNotFoundException("Película no encontrada");

        } catch (IOException e) {
            logger.severe("IO Error: " + e.getMessage());
            throw new MovieServiceException("Error de conexión");
        } catch (InterruptedException e) {
            logger.severe("Request interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
            throw new MovieServiceException("La solicitud fue interrumpida");
        } catch (JsonParseException e) {
            logger.severe("JSON Parse error: " + e.getMessage());
            throw new MovieServiceException("Error al procesar la respuesta");
        } catch (Exception e) {
            logger.severe("Unexpected error: " + e.getMessage());
            throw new MovieServiceException("Error inesperado: " + e.getMessage());
        }
    }

    private void validateId(String id) throws MovieServiceException {
        try {
            int episodeId = Integer.parseInt(id);
            if (episodeId < 1 || episodeId > 7) {
                throw new MovieServiceException("El ID debe estar entre 1 y 7");
            }
        } catch (NumberFormatException e) {
            throw new MovieServiceException("ID inválido");
        }
    }
}

class MovieServiceException extends Exception {
    public MovieServiceException(String message) {
        super(message);
    }
}

class MovieNotFoundException extends MovieServiceException {
    public MovieNotFoundException(String message) {
        super(message);
    }
}
