class StarWarsApp {
    constructor() {
        this.initializeElements();
        this.initializeEventListeners();
        this.consultedMovies = [];
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

        // Cambiar la selección del botón
        const updateListButton = document.querySelector('.update-list-button');
        console.log('Botón de actualizar lista:', updateListButton);

        if (updateListButton) {
            updateListButton.addEventListener('click', () => {
                console.log('Botón de actualizar lista clickeado');
                this.updateConsultedMoviesList();
            });
        } else {
            console.error('No se encontró el botón de actualizar lista');
        }
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

            if (!response.ok) {
                throw new Error(data.error || 'Error al obtener la película');
            }

            this.consultedMovies.push(data);

            this.displayMovie(data);

        } catch (error) {
            console.error('Error en searchMovie:', error);
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

        // Función para sanitizar texto y prevenir XSS
        const sanitizeHTML = (text) => {
            if (!text) return '';
            const element = document.createElement('div');
            element.textContent = text;
            return element.textContent;
        };

        const title = sanitizeHTML(movie.title) || 'Sin título';
        const episodeId = sanitizeHTML(movie.episodeId || movie.episode_id) || 'N/A';
        const director = sanitizeHTML(movie.director) || 'No disponible';
        const producer = sanitizeHTML(movie.producer) || 'No disponible';
        const releaseDate = sanitizeHTML(movie.releaseDate || movie.release_date) || 'Fecha no disponible';
        const openingCrawl = sanitizeHTML(movie.openingCrawl || movie.opening_crawl) || '';

        this.movieDetails.innerHTML = `
            <div class="movie-info">
                <h2 class="movie-title">${title}</h2>
                <p><strong>Episodio:</strong> ${episodeId}</p>
                <p><strong>Director:</strong> ${director}</p>
                <p><strong>Productor:</strong> ${producer}</p>
                <p><strong>Fecha de estreno:</strong> ${releaseDate}</p>
                <p><strong>Introducción:</strong> ${openingCrawl}</p>
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

    updateConsultedMoviesList() {
        console.log('Método updateConsultedMoviesList llamado');
        console.log('Número de películas consultadas:', this.consultedMovies.length);
        console.log('Contenido de películas consultadas:', JSON.stringify(this.consultedMovies, null, 2));

        const consultedMoviesContainer = document.querySelector('.consulted-movies-container');
        console.log('Contenedor encontrado:', consultedMoviesContainer);

        if (this.consultedMovies.length === 0) {
            console.log('No hay películas consultadas');
            consultedMoviesContainer.innerHTML = '<p>No se han consultado películas aún.</p>';
            return;
        }

        try {
            // Cambiamos a un método más robusto de renderizado
            const moviesHTML = this.consultedMovies.map((movie, index) => `
            <div class="consulted-movie">
                <h3>Película ${index + 1}</h3>
                <pre class="consulted-movies-json">${JSON.stringify(movie, null, 2)}</pre>
            </div>
        `).join('');

            consultedMoviesContainer.innerHTML = moviesHTML;
            console.log('Contenido actualizado exitosamente');
        } catch (error) {
            console.error('Error al actualizar la lista:', error);
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    window.starWarsApp = new StarWarsApp();
    console.log('Star Wars App initialized');
});