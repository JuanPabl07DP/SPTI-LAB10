package co.edu.escuelaing.arem.ASE.service;

import co.edu.escuelaing.arem.ASE.model.Movie;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.TimeoutException;

/**
 * Servicio para obtener información de películas de Star Wars
 */
public class MovieService {
    private static final String SWAPI_URL = "https://swapi.py4e.com/api/films/";
    private static final int TIMEOUT_SECONDS = 10;
    private static final Logger logger = Logger.getLogger(MovieService.class.getName());

    private final HttpClient client;
    private final Gson gson;

    /**
     * Constructor que inicializa el cliente HTTP y Gson
     */
    public MovieService() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();
        this.gson = new Gson();
    }

    /**
     * Obtiene una película por su ID
     * @param id ID de la película a buscar
     * @return Objeto Movie con la información de la película
     * @throws IllegalArgumentException si el ID es inválido
     * @throws MovieNotFoundException si la película no se encuentra
     * @throws MovieServiceException si hay un error en el servicio
     */
    public Movie getMovieById(String id) throws MovieServiceException {
        validateMovieId(id);

        try {
            HttpRequest request = createRequest(id);
            HttpResponse<String> response = sendRequest(request);
            validateResponse(response);
            return parseMovieResponse(response.body());

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error de conexión al obtener la película", e);
            throw new MovieServiceException("Error de conexión al obtener la película");

        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "La operación fue interrumpida", e);
            Thread.currentThread().interrupt();
            throw new MovieServiceException("La operación fue interrumpida");

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error inesperado al obtener la película", e);
            throw new MovieServiceException("Error inesperado al obtener la película");
        }
    }

    /**
     * Valida el ID de la película
     */
    private void validateMovieId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la película no puede estar vacío");
        }
        try {
            int movieId = Integer.parseInt(id);
            if (movieId < 1 || movieId > 6) {
                throw new IllegalArgumentException("El ID de la película debe estar entre 1 y 6");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El ID debe ser un número válido");
        }
    }

    /**
     * Crea la petición HTTP
     */
    private HttpRequest createRequest(String id) {
        return HttpRequest.newBuilder()
                .uri(URI.create(SWAPI_URL + id))
                .header("Accept", "application/json")
                .GET()
                .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();
    }

    /**
     * Envía la petición HTTP
     */
    private HttpResponse<String> sendRequest(HttpRequest request)
            throws IOException, InterruptedException {
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Valida la respuesta HTTP
     */
    private void validateResponse(HttpResponse<String> response) throws MovieServiceException {
        if (response.statusCode() == 404) {
            throw new MovieNotFoundException("Película no encontrada");
        }
        if (response.statusCode() != 200) {
            throw new MovieServiceException("Error del servidor: " + response.statusCode());
        }
    }

    /**
     * Parsea la respuesta JSON a objeto Movie
     */
    private Movie parseMovieResponse(String json) throws MovieServiceException {
        try {
            Movie movie = gson.fromJson(json, Movie.class);
            if (movie == null) {
                throw new MovieServiceException("Error al parsear la respuesta del servidor");
            }
            return movie;
        } catch (JsonSyntaxException e) {
            logger.log(Level.SEVERE, "Error al parsear JSON", e);
            throw new MovieServiceException("Error al parsear la respuesta del servidor");
        }
    }
}

/**
 * Excepción para errores generales del servicio
 */
class MovieServiceException extends Exception {
    public MovieServiceException(String message) {
        super(message);
    }
}

/**
 * Excepción para película no encontrada
 */
class MovieNotFoundException extends MovieServiceException {
    public MovieNotFoundException(String message) {
        super(message);
    }
}
