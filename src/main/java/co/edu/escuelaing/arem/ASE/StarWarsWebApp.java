package co.edu.escuelaing.arem.ASE;

import co.edu.escuelaing.arem.ASE.controller.MovieController;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Aplicación web para el servicio de películas de Star Wars
 */
public class StarWarsWebApp {
    private static final Map<String, HttpHandler> GET_ROUTES = new HashMap<>();
    private static final Logger logger = Logger.getLogger(StarWarsWebApp.class.getName());
    private static final int DEFAULT_BACKLOG = 0;
    private static String staticFilesPath = "target/classes/public";

    public static byte[] getStaticFile(String path) throws IOException, URISyntaxException {
        try {
            Path filePath = Path.of(staticFilesPath + path);

            logger.info("Intentando servir archivo: " + filePath);

            if (!Files.exists(filePath)) {
                logger.warning("Archivo no encontrado: " + filePath);
                return null;
            }

            byte[] fileBytes = Files.readAllBytes(filePath);
            logger.info("Sirviendo archivo: " + path);
            return fileBytes;

        } catch (IOException e) {
            logger.log(Level.WARNING, "Error al servir archivo: " + path, e);
            throw e;
        }
    }

    @FunctionalInterface
    interface HttpHandler {
        String handle(Map<String, String> params);
    }

    /**
     * Método principal que inicia la aplicación
     * @param args argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        setupRoutes();
        startServer(8080);
    }

    /**
     * Configura las rutas de la aplicación
     */
    private static void setupRoutes() {
        get("/api/film/:id", params -> MovieController.getMovie(params.get("id")));
        staticfiles(staticFilesPath);
        logger.info("Rutas configuradas correctamente");
    }

    /**
     * Registra una ruta GET
     * @param path ruta a registrar
     * @param handler manejador de la ruta
     */
    public static void get(String path, HttpHandler handler) {
        GET_ROUTES.put(path, handler);
        logger.info("Ruta GET registrada: " + path);
    }

    /**
     * Configura la ubicación de los archivos estáticos
     * @param path ruta de los archivos estáticos
     */
    public static void staticfiles(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta de archivos estáticos no puede estar vacía");
        }
        staticFilesPath = path;
        logger.info("Archivos estáticos configurados en: " + path);
    }

    /**
     * Inicia el servidor HTTP
     * @param port puerto en el que escuchará el servidor
     */
    private static void startServer(int port) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), DEFAULT_BACKLOG);
            server.createContext("/", StarWarsWebApp::handleRequest);
            server.setExecutor(null);
            server.start();
            logger.info("Servidor iniciado en el puerto " + port);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error al iniciar el servidor", e);
            throw new RuntimeException("No se pudo iniciar el servidor", e);
        }
    }

    /**
     * Maneja las peticiones HTTP
     */
    private static void handleRequest(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            logger.info("Recibida petición: " + method + " " + path);

            if ("GET".equals(method)) {
                handleGetRequest(exchange, path);
            } else {
                sendMethodNotAllowedResponse(exchange);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al procesar la petición", e);
            sendErrorResponse(exchange, "Internal Server Error");
        } finally {
            exchange.close();
        }
    }

    /**
     * Maneja las peticiones GET
     */
    private static void handleGetRequest(HttpExchange exchange, String path) throws IOException {
        // Primero intentamos rutas dinámicas
        String response = handleDynamicRoute(path);
        if (response != null) {
            sendResponse(exchange, response);
            return;
        }

        // Luego intentamos servir archivos estáticos
        if (serveStaticFile(exchange, path)) {
            return;
        }

        sendNotFoundResponse(exchange);
    }

    /**
     * Procesa rutas dinámicas
     */
    private static String handleDynamicRoute(String path) {
        return GET_ROUTES.entrySet().stream()
                .filter(route -> matchesRoute(route.getKey(), path))
                .findFirst()
                .map(route -> route.getValue().handle(extractParams(route.getKey(), path)))
                .orElse(null);
    }

    /**
     * Verifica si una ruta coincide con un patrón
     */
    private static boolean matchesRoute(String routePattern, String actualPath) {
        String[] routeParts = routePattern.split("/");
        String[] pathParts = actualPath.split("/");

        if (routeParts.length != pathParts.length) return false;

        for (int i = 0; i < routeParts.length; i++) {
            if (!routeParts[i].startsWith(":") && !routeParts[i].equals(pathParts[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Extrae parámetros de una ruta
     */
    private static Map<String, String> extractParams(String routePattern, String actualPath) {
        Map<String, String> params = new HashMap<>();
        String[] routeParts = routePattern.split("/");
        String[] pathParts = actualPath.split("/");

        for (int i = 0; i < routeParts.length; i++) {
            if (routeParts[i].startsWith(":")) {
                params.put(routeParts[i].substring(1), pathParts[i]);
            }
        }
        return params;
    }

    /**
     * Sirve archivos estáticos
     */
    private static boolean serveStaticFile(HttpExchange exchange, String path) {
        try {
            path = path.equals("/") ? "/index.html" : path;
            Path filePath = Path.of(staticFilesPath + path);

            logger.info("Intentando servir archivo: " + filePath);

            if (!Files.exists(filePath)) {
                logger.warning("Archivo no encontrado: " + filePath);
                return false;
            }

            byte[] fileBytes = Files.readAllBytes(filePath);
            String contentType = getContentType(path);

            logger.info("Sirviendo archivo: " + path + " con Content-Type: " + contentType);

            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(200, fileBytes.length);
            exchange.getResponseBody().write(fileBytes);
            exchange.getResponseBody().close();
            return true;

        } catch (IOException e) {
            logger.log(Level.WARNING, "Error al servir archivo: " + path, e);
            return false;
        }
    }

    /**
     * Envía una respuesta HTTP
     */
    private static void sendResponse(HttpExchange exchange, String response) throws IOException {
        byte[] responseBytes = response.getBytes();
        exchange.getResponseHeaders().set("Content-Type", getContentType(exchange.getRequestURI().getPath()));
        exchange.sendResponseHeaders(200, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.getResponseBody().close();
    }

    /**
     * Envía una respuesta de error
     */
    private static void sendErrorResponse(HttpExchange exchange, String message) throws IOException {
        String response = "{\"error\": \"" + message + "\"}";
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = response.getBytes();
        exchange.sendResponseHeaders(500, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.getResponseBody().close();
    }

    /**
     * Envía una respuesta 404 Not Found
     */
    private static void sendNotFoundResponse(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(404, -1);
    }

    /**
     * Envía una respuesta 405 Method Not Allowed
     */
    private static void sendMethodNotAllowedResponse(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(405, -1);
    }

    /**
     * Determina el tipo de contenido basado en la extensión del archivo
     */
    private static String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html";
        if (path.endsWith(".css")) return "text/css";
        if (path.endsWith(".js")) return "application/javascript";
        if (path.endsWith(".json")) return "application/json";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".webp")) return "image/webp";
        if (path.endsWith(".gif")) return "image/gif";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".ico")) return "image/x-icon";
        return "text/plain";
    }
}