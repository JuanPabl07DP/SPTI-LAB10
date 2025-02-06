/**
 * Clase principal para manejar la aplicación de Star Wars
 */
class StarWarsApp {
    constructor() {
        // Inicializar elementos del DOM
        this.movieInput = document.getElementById('movie-input');
        this.movieDetails = document.getElementById('movie-details');
        this.searchButton = document.querySelector('.star-wars-button');

        // Array para almacenar las películas consultadas
        this.consultedMovies = [];

        // Inicializar eventos
        this.initializeEventListeners();
    }

    /**
     * Inicializa los event listeners
     */
    initializeEventListeners() {
        // Evento click del botón buscar
        this.searchButton.addEventListener('click', () => {
            this.searchMovie();
        });

        // Evento para el input cuando se presiona Enter
        this.movieInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                this.searchMovie();
            }
        });
    }

    /**
     * Método principal para buscar la película
     */
    async searchMovie() {
        // Obtener el valor del input
        const movieId = this.movieInput.value.trim();

        // Validar el input
        if (!this.validateInput(movieId)) {
            return;
        }

        try {
            // Mostrar estado de carga
            this.showLoadingState();

            // Realizar la petición al servidor
            const response = await fetch(`/api/film/${movieId}`);

            // Verificar si la respuesta es exitosa
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            // Obtener los datos de la película
            const movieData = await response.json();

            // Verificar si hay un error en la respuesta
            if (movieData.error) {
                throw new Error(movieData.error);
            }

            // Mostrar la película y agregarla a consultadas
            this.displayMovie(movieData);
            this.addToConsultedMovies(movieData);

        } catch (error) {
            // Manejar cualquier error que ocurra
            this.handleError(error);
        } finally {
            // Ocultar estado de carga
            this.hideLoadingState();
        }
    }

    /**
     * Valida el input del usuario
     */
    validateInput(movieId) {
        if (!movieId) {
            this.showError('Por favor ingrese un número de película');
            return false;
        }

        const id = parseInt(movieId);
        if (isNaN(id) || id < 1 || id > 6) {
            this.showError('Por favor ingrese un número válido entre 1 y 6');
            return false;
        }

        return true;
    }

    /**
     * Muestra la información de la película en el DOM
     */
    displayMovie(movie) {
        const formattedDate = new Date(movie.releaseDate).toLocaleDateString('es-ES', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });

        this.movieDetails.innerHTML = `
            <div class="movie-info">
                <h2 class="movie-title">${movie.title}</h2>
                <div class="movie-meta">
                    <p><strong>Episodio:</strong> ${movie.episodeId}</p>
                    <p><strong>Director:</strong> ${movie.director}</p>
                    <p><strong>Fecha de estreno:</strong> ${formattedDate}</p>
                </div>
                <div class="movie-description">
                    <p><strong>Introducción:</strong></p>
                    <p class="opening-crawl">${movie.openingCrawl}</p>
                </div>
            </div>
        `;
    }

    /**
     * Agrega una película al historial de consultadas
     */
    addToConsultedMovies(movie) {
        // Verificar si la película ya está en el historial
        if (!this.consultedMovies.some(m => m.episodeId === movie.episodeId)) {
            this.consultedMovies.push(movie);
            // Ordenar películas por episodio
            this.consultedMovies.sort((a, b) => a.episodeId - b.episodeId);
        }
    }

    /**
     * Muestra el estado de carga
     */
    showLoadingState() {
        this.movieDetails.innerHTML = `
            <div class="loading">
                <div class="loading-spinner"></div>
                <p>Buscando película...</p>
            </div>
        `;
        // Deshabilitar input y botón durante la carga
        this.movieInput.disabled = true;
        this.searchButton.disabled = true;
    }

    /**
     * Oculta el estado de carga
     */
    hideLoadingState() {
        // Habilitar input y botón
        this.movieInput.disabled = false;
        this.searchButton.disabled = false;
        // Limpiar el input
        this.movieInput.value = '';
    }

    /**
     * Maneja los errores de la aplicación
     */
    handleError(error) {
        console.error('Error:', error);
        this.movieDetails.innerHTML = `
            <div class="error-message">
                <p>Error: No se pudo obtener la información de la película.</p>
                <p>Por favor intente de nuevo más tarde.</p>
            </div>
        `;
    }

    /**
     * Muestra mensajes de error al usuario
     */
    showError(message) {
        // Puedes personalizar cómo mostrar los errores
        alert(message);
    }
}

// Inicializar la aplicación cuando el DOM esté completamente cargado
document.addEventListener('DOMContentLoaded', () => {
    // Crear instancia de la aplicación
    window.starWarsApp = new StarWarsApp();
});