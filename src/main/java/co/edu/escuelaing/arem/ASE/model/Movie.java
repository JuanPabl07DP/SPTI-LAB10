package co.edu.escuelaing.arem.ASE.model;

import com.google.gson.annotations.SerializedName;

/**
 * Clase que representa una película de Star Wars
 */
public class Movie {
    private String title;

    @SerializedName("episode_id")
    private int episodeId;

    @SerializedName("opening_crawl")
    private String openingCrawl;

    private String director;

    @SerializedName("release_date")
    private String releaseDate;

    /**
     * Constructor vacío necesario para la deserialización
     */
    public Movie() {
    }

    /**
     * Constructor con todos los campos
     * @param title Título de la película
     * @param episodeId Número de episodio
     * @param openingCrawl Texto de apertura
     * @param director Director de la película
     * @param releaseDate Fecha de estreno
     */
    public Movie(String title, int episodeId, String openingCrawl, String director, String releaseDate) {
        this.setTitle(title);
        this.setEpisodeId(episodeId);
        this.setOpeningCrawl(openingCrawl);
        this.setDirector(director);
        this.setReleaseDate(releaseDate);
    }

    /**
     * Obtiene el título de la película
     * @return título de la película
     */
    public String getTitle() {
        return title;
    }

    /**
     * Establece el título de la película
     * @param title título a establecer
     * @throws IllegalArgumentException si el título es nulo o vacío
     */
    public void setTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("El título no puede estar vacío");
        }
        this.title = title.trim();
    }

    /**
     * Obtiene el número de episodio
     * @return número de episodio
     */
    public int getEpisodeId() {
        return episodeId;
    }

    /**
     * Establece el número de episodio
     * @param episodeId número de episodio a establecer
     * @throws IllegalArgumentException si el episodio no está entre 1 y 6
     */
    public void setEpisodeId(int episodeId) {
        if (episodeId < 1 || episodeId > 6) {
            throw new IllegalArgumentException("El número de episodio debe estar entre 1 y 6");
        }
        this.episodeId = episodeId;
    }

    /**
     * Obtiene el texto de apertura
     * @return texto de apertura
     */
    public String getOpeningCrawl() {
        return openingCrawl;
    }

    /**
     * Establece el texto de apertura
     * @param openingCrawl texto de apertura a establecer
     * @throws IllegalArgumentException si el texto es nulo o vacío
     */
    public void setOpeningCrawl(String openingCrawl) {
        if (openingCrawl == null || openingCrawl.trim().isEmpty()) {
            throw new IllegalArgumentException("El texto de apertura no puede estar vacío");
        }
        this.openingCrawl = openingCrawl.trim();
    }

    /**
     * Obtiene el director de la película
     * @return director de la película
     */
    public String getDirector() {
        return director;
    }

    /**
     * Establece el director de la película
     * @param director director a establecer
     * @throws IllegalArgumentException si el director es nulo o vacío
     */
    public void setDirector(String director) {
        if (director == null || director.trim().isEmpty()) {
            throw new IllegalArgumentException("El director no puede estar vacío");
        }
        this.director = director.trim();
    }

    /**
     * Obtiene la fecha de estreno
     * @return fecha de estreno
     */
    public String getReleaseDate() {
        return releaseDate;
    }

    /**
     * Establece la fecha de estreno
     * @param releaseDate fecha de estreno a establecer
     * @throws IllegalArgumentException si la fecha es nula o vacía
     */
    public void setReleaseDate(String releaseDate) {
        if (releaseDate == null || releaseDate.trim().isEmpty()) {
            throw new IllegalArgumentException("La fecha de estreno no puede estar vacía");
        }
        this.releaseDate = releaseDate.trim();
    }

    @Override
    public String toString() {
        return "Movie{" +
                "title='" + title + '\'' +
                ", episodeId=" + episodeId +
                ", director='" + director + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                '}';
    }
}
