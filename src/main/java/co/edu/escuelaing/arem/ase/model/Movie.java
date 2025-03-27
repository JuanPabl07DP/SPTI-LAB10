package co.edu.escuelaing.arem.ase.model;

import com.google.gson.annotations.SerializedName;

public class Movie {
    private String title;

    @SerializedName("episode_id")
    private Integer episodeId;

    @SerializedName("opening_crawl")
    private String openingCrawl;

    private String director;
    private String producer;

    @SerializedName("release_date")
    private String releaseDate;

    public Movie() {
    }

    public Movie(String title, Integer episodeId, String openingCrawl, String director, String producer, String releaseDate) {
        this.title = title;
        this.episodeId = episodeId;
        this.openingCrawl = openingCrawl;
        this.director = director;
        this.producer = producer;
        this.releaseDate = releaseDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(Integer episodeId) {
        this.episodeId = episodeId;
    }

    public String getOpeningCrawl() {
        return openingCrawl;
    }

    public void setOpeningCrawl(String openingCrawl) {
        this.openingCrawl = openingCrawl;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "title='" + title + '\'' +
                ", episodeId=" + episodeId +
                ", director='" + director + '\'' +
                ", producer='" + producer + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                ", openingCrawl='" + openingCrawl + '\'' +
                '}';
    }
}