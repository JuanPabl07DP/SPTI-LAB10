package co.edu.escuelaing.arem.ase.service;
import co.edu.escuelaing.arem.ase.model.Movie;
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
        validateId(id);
        String responseBody = fetchDataFromApi();
        return findMovieInResponse(responseBody, id);
    }

    private String fetchDataFromApi() throws MovieServiceException {
        try {
            String url = SWAPI_URL + "?format=json";
            logInfo("Requesting URL: %s", url);

            HttpRequest request = buildRequest(url);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            validateResponse(response);

            String responseBody = response.body();
            logInfo("API Response: %s", responseBody);

            return responseBody;
        } catch (IOException e) {
            logSevere("IO Error: %s", e.getMessage());
            throw new MovieServiceException("Error de conexión", e);
        } catch (InterruptedException e) {
            logSevere("Request interrupted: %s", e.getMessage());
            Thread.currentThread().interrupt();
            throw new MovieServiceException("La solicitud fue interrumpida", e);
        }
    }

    private HttpRequest buildRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();
    }

    private void validateResponse(HttpResponse<String> response) throws MovieServiceException {
        if (response.statusCode() != 200) {
            logSevere("Error response from API: %d", response.statusCode());
            throw new MovieServiceException(String.format("Error del servidor: %d", response.statusCode()));
        }
    }

    private Movie findMovieInResponse(String responseBody, String id) throws MovieServiceException {
        try {
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            JsonArray results = jsonResponse.getAsJsonArray("results");
            int searchId = Integer.parseInt(id);

            for (JsonElement element : results) {
                JsonObject movieJson = element.getAsJsonObject();
                if (movieJson.has("episode_id")) {
                    int episodeId = movieJson.get("episode_id").getAsInt();
                    logInfo("Checking episode_id: %d", episodeId);

                    if (episodeId == searchId) {
                        Movie movie = gson.fromJson(movieJson, Movie.class);
                        logInfo("Found movie: %s", movie);
                        return movie;
                    }
                }
            }

            logWarning("Movie not found with episode_id: %s", id);
            throw new MovieNotFoundException(String.format("Película con ID %s no encontrada", id));

        } catch (JsonParseException e) {
            logSevere("JSON Parse error: %s", e.getMessage());
            throw new MovieServiceException(String.format("Error al procesar la respuesta para película con ID %s", id), e);
        } catch (Exception e) {
            logSevere("Unexpected error: %s", e.getMessage());
            throw new MovieServiceException(String.format("Error inesperado al buscar película con ID %s", id), e);
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

    private void logInfo(String format, Object... args) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info(String.format(format, args));
        }
    }

    private void logWarning(String format, Object... args) {
        if (logger.isLoggable(Level.WARNING)) {
            logger.warning(String.format(format, args));
        }
    }

    private void logSevere(String format, Object... args) {
        if (logger.isLoggable(Level.SEVERE)) {
            logger.severe(String.format(format, args));
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