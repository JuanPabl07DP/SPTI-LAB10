package co.edu.escuelaing.arem.ASE.controller;

import co.edu.escuelaing.arem.ASE.service.MovieService;
import co.edu.escuelaing.arem.ASE.model.Movie;
import com.google.gson.Gson;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador para manejar las peticiones relacionadas con películas
 */
public class MovieController {
    private static final MovieService movieService = new MovieService();
    private static final Gson gson = new Gson();
    private static final Logger logger = Logger.getLogger(MovieController.class.getName());

    /**
     * Obtiene una película por su ID
     * @param id ID de la película a buscar
     * @return JSON con la información de la película o mensaje de error
     */
    public static String getMovie(String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return createErrorResponse("El ID de la película no puede estar vacío");
            }

            int movieId;
            try {
                movieId = Integer.parseInt(id);
                // Cambiar de 1-6 a 1-7
                if (movieId < 1 || movieId > 7) {
                    return createErrorResponse("El ID de la película debe estar entre 1 y 7");
                }
            } catch (NumberFormatException e) {
                return createErrorResponse("El ID de la película debe ser un número válido");
            }

            Movie movie = movieService.getMovieById(id);
            if (movie == null) {
                return createErrorResponse("Película no encontrada");
            }

            return gson.toJson(movie);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al obtener la película", e);
            return createErrorResponse("Error al obtener la información de la película");
        }
    }

    /**
     * Crea una respuesta de error en formato JSON
     * @param message Mensaje de error
     * @return JSON con el mensaje de error
     */
    private static String createErrorResponse(String message) {
        return String.format("{\"error\": \"%s\"}", message);
    }
}
