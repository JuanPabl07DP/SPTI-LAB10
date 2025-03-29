package co.edu.escuelaing.arem.ase.service;

import co.edu.escuelaing.arem.ase.model.Movie;
import com.google.gson.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
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
            logger.info(String.format("Requesting URL: %s", url));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                logger.severe(String.format("Error response from API: %d", response.statusCode()));
                throw new MovieServiceException(String.format("Error del servidor: %d", response.statusCode()));
            }

            String responseBody = response.body();
            logger.info(String.format("API Response: %s", responseBody));

            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            JsonArray results = jsonResponse.getAsJsonArray("results");

            int searchId = Integer.parseInt(id);

            for (JsonElement element : results) {
                JsonObject movieJson = element.getAsJsonObject();
                if (movieJson.has("episode_id")) {
                    int episodeId = movieJson.get("episode_id").getAsInt();
                    logger.info(String.format("Checking episode_id: %d", episodeId));

                    if (episodeId == searchId) {
                        Movie movie = gson.fromJson(movieJson, Movie.class);
                        logger.info(String.format("Found movie: %s", movie));
                        return movie;
                    }
                }
            }

            logger.warning(String.format("Movie not found with episode_id: %s", id));
            throw new MovieNotFoundException(String.format("Película con ID %s no encontrada", id));

        } catch (IOException e) {
            logger.severe(String.format("IO Error while fetching movie with ID %s: %s", id, e.getMessage()));
            throw new MovieServiceException(String.format("Error de conexión al buscar película con ID %s: %s", id, e.getMessage()), e);
        } catch (InterruptedException e) {
            logger.severe(String.format("Request interrupted while fetching movie with ID %s: %s", id, e.getMessage()));
            Thread.currentThread().interrupt();
            throw new MovieServiceException(String.format("La solicitud para la película con ID %s fue interrumpida: %s", id, e.getMessage()), e);
        } catch (JsonParseException e) {
            logger.severe(String.format("JSON Parse error for movie with ID %s: %s", id, e.getMessage()));
            throw new MovieServiceException(String.format("Error al procesar la respuesta para película con ID %s: %s", id, e.getMessage()), e);
        } catch (Exception e) {
            logger.severe(String.format("Unexpected error while fetching movie with ID %s: %s", id, e.getMessage()));
            throw new MovieServiceException(String.format("Error inesperado al buscar película con ID %s: %s", id, e.getMessage()), e);
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

    public MovieServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

class MovieNotFoundException extends MovieServiceException {
    public MovieNotFoundException(String message) {
        super(message);
    }

    public MovieNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}