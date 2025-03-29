package co.edu.escuelaing.arem.ase;

import co.edu.escuelaing.arem.ase.controller.MovieController;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
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
    // Constantes para literales de strings comunes
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String ERROR_PREFIX = "{\"error\": \"";
    private static final String ERROR_SUFFIX = "\"}";
    private static final String INDEX_HTML = "/index.html";
    // Constantes para los tipos MIME
    private static final String MIME_TEXT_HTML = "text/html";
    private static final String MIME_TEXT_CSS = "text/css";
    private static final String MIME_APPLICATION_JS = "application/javascript";
    private static final String MIME_APPLICATION_JSON = "application/json";
    private static final String MIME_IMAGE_PNG = "image/png";
    private static final String MIME_IMAGE_JPEG = "image/jpeg";
    private static final String MIME_IMAGE_WEBP = "image/webp";
    private static final String MIME_IMAGE_GIF = "image/gif";
    private static final String MIME_IMAGE_SVG = "image/svg+xml";
    private static final String MIME_IMAGE_ICON = "image/x-icon";
    private static final String MIME_TEXT_PLAIN = "text/plain";

    public static byte[] getStaticFile(String path) throws IOException {
        if (path == null || path.isEmpty()) {
            logger.warning("La ruta del archivo es nula o vacía.");
            return null;
        }

        try {
            Path filePath = buildFilePath(staticFilesPath, path);

            logger.info(String.format("Intentando servir archivo: %s", filePath));

            if (!Files.exists(filePath)) {
                logger.warning(String.format("Archivo no encontrado: %s", filePath));
                return null;
            }

            byte[] fileBytes = Files.readAllBytes(filePath);
            logger.info(String.format("Sirviendo archivo: %s", path));
            return fileBytes;

        } catch (IOException e) {
            logger.log(Level.WARNING, String.format("Error al servir archivo: %s", path), e);
            throw e;  // Propaga la excepción para que el llamador maneje el error
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
            path = path.equals("/") ? INDEX_HTML : path;
            Path filePath = buildFilePath(staticFilesPath, path);

            logger.info(String.format("Intentando servir archivo: %s", filePath));

            if (!Files.exists(filePath)) {
                logger.warning(String.format("Archivo no encontrado: %s", filePath));
                return false;
            }

            byte[] fileBytes = Files.readAllBytes(filePath);
            String contentType = getContentType(path);

            logger.info(String.format("Sirviendo archivo: %s con Content-Type: %s", path, contentType));

            exchange.getResponseHeaders().set(CONTENT_TYPE_HEADER, contentType);
            exchange.sendResponseHeaders(200, fileBytes.length);
            exchange.getResponseBody().write(fileBytes);
            exchange.getResponseBody().close();
            return true;

        } catch (IOException e) {
            logger.log(Level.WARNING, String.format("Error al servir archivo: %s", path), e);
            return false;
        }
    }

    /**
     * Envía una respuesta HTTP
     */
    private static void sendResponse(HttpExchange exchange, String response) throws IOException {
        byte[] responseBytes = response.getBytes();
        exchange.getResponseHeaders().set(CONTENT_TYPE_HEADER, getContentType(exchange.getRequestURI().getPath()));
        exchange.sendResponseHeaders(200, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.getResponseBody().close();
    }

    /**
     * Envía una respuesta de error
     */
    private static void sendErrorResponse(HttpExchange exchange, String message) throws IOException {
        String response = ERROR_PREFIX + message + ERROR_SUFFIX;
        exchange.getResponseHeaders().set(CONTENT_TYPE_HEADER, APPLICATION_JSON);
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

    private static Path buildFilePath(String basePath, String filePath) {
        // Convertir a Path para manejo correcto según el sistema
        Path base = Path.of(basePath);
        Path relativePath = Path.of(filePath.startsWith("/") ? filePath.substring(1) : filePath);

        // Resolver la ruta relativa contra la base
        return base.resolve(relativePath);
    }

    /**
     * Determina el tipo de contenido basado en la extensión del archivo
     */
    private static String getContentType(String path) {
        if (path.endsWith(".html")) return MIME_TEXT_HTML;
        if (path.endsWith(".css")) return MIME_TEXT_CSS;
        if (path.endsWith(".js")) return MIME_APPLICATION_JS;
        if (path.endsWith(".json")) return MIME_APPLICATION_JSON;
        if (path.endsWith(".png")) return MIME_IMAGE_PNG;
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return MIME_IMAGE_JPEG;
        if (path.endsWith(".webp")) return MIME_IMAGE_WEBP;
        if (path.endsWith(".gif")) return MIME_IMAGE_GIF;
        if (path.endsWith(".svg")) return MIME_IMAGE_SVG;
        if (path.endsWith(".ico")) return MIME_IMAGE_ICON;
        return MIME_TEXT_PLAIN;
    }
}