/**
 * Clase principal para manejar la aplicación de Star Wars
 */
class StarWarsApp {
    constructor() {
        this.initializeElements();
        this.initializeEventListeners();
    }

    initializeElements() {
        this.movieInput = document.getElementById('movie-input');
        this.movieDetails = document.getElementById('movie-details');
        this.searchButton = document.querySelector('.star-wars-button');
    }

    initializeEventListeners() {
        this.searchButton.addEventListener('click', () => this.searchMovie());
        this.movieInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                this.searchMovie();
            }
        });
    }

    async searchMovie() {
        const movieId = this.movieInput.value.trim();

        if (!this.validateInput(movieId)) {
            return;
        }

        try {
            this.showLoadingState();
            const response = await fetch(`/api/film/${movieId}`);
            const data = await response.json();

            console.log('Response:', response);
            console.log('Data:', data);

            if (!response.ok) {
                throw new Error(data.error || 'Error al obtener la película');
            }

            this.displayMovie(data);

        } catch (error) {
            console.error('Error:', error);
            this.displayError(error.message);
        } finally {
            this.hideLoadingState();
        }
    }

    validateInput(movieId) {
        if (!movieId) {
            this.displayError('Por favor ingrese un número de película');
            return false;
        }

        const id = parseInt(movieId);
        if (isNaN(id) || id < 1 || id > 7) {
            this.displayError('Por favor ingrese un número entre 1 y 7');
            return false;
        }

        return true;
    }

    displayMovie(movie) {
        console.log('Displaying movie:', movie);

        if (!movie) {
            this.displayError('No se pudo obtener la información de la película');
            return;
        }

        const formattedDate = movie.releaseDate || movie.release_date || 'Fecha no disponible';
        const formattedCrawl = movie.openingCrawl || movie.opening_crawl || '';

        this.movieDetails.innerHTML = `
            <div class="movie-info">
                <h2 class="movie-title">${movie.title || 'Sin título'}</h2>
                <p><strong>Episodio:</strong> ${movie.episodeId || movie.episode_id || 'N/A'}</p>
                <p><strong>Director:</strong> ${movie.director || 'No disponible'}</p>
                <p><strong>Productor:</strong> ${movie.producer || 'No disponible'}</p>
                <p><strong>Fecha de estreno:</strong> ${formattedDate}</p>
                <p><strong>Introducción:</strong> ${formattedCrawl}</p>
            </div>
        `;
    }

    displayError(message) {
        console.error('Error message:', message);
        this.movieDetails.innerHTML = `
            <div class="error-message">
                <p><strong>Error:</strong> ${message}</p>
                <p>Por favor intente de nuevo.</p>
            </div>
        `;
    }

    showLoadingState() {
        this.searchButton.disabled = true;
        this.movieInput.disabled = true;
        this.movieDetails.innerHTML = `
            <div class="loading-message">
                <div class="loading-spinner"></div>
                <p>Buscando película...</p>
            </div>
        `;
    }

    hideLoadingState() {
        this.searchButton.disabled = false;
        this.movieInput.disabled = false;
    }
}

// Inicializar la aplicación cuando el DOM esté cargado
document.addEventListener('DOMContentLoaded', () => {
    window.starWarsApp = new StarWarsApp();
    console.log('Star Wars App initialized');
});