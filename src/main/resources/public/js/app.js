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

        // Limpiar el contenedor de detalles
        while (this.movieDetails.firstChild) {
            this.movieDetails.removeChild(this.movieDetails.firstChild);
        }

        // Crear el contenedor principal
        const movieInfo = document.createElement('div');
        movieInfo.className = 'movie-info';

        // Título de la película
        const title = document.createElement('h2');
        title.className = 'movie-title';
        title.textContent = movie.title || 'Sin título';
        movieInfo.appendChild(title);

        // Crear y añadir cada detalle de la película
        const details = [
            { label: 'Episodio', value: movie.episodeId || movie.episode_id || 'N/A' },
            { label: 'Director', value: movie.director || 'No disponible' },
            { label: 'Productor', value: movie.producer || 'No disponible' },
            { label: 'Fecha de estreno', value: movie.releaseDate || movie.release_date || 'Fecha no disponible' },
            { label: 'Introducción', value: movie.openingCrawl || movie.opening_crawl || '' }
        ];

        details.forEach(detail => {
            const paragraph = document.createElement('p');

            const strong = document.createElement('strong');
            strong.textContent = `${detail.label}: `;
            paragraph.appendChild(strong);

            // Añadir el valor como texto plano
            paragraph.appendChild(document.createTextNode(detail.value));

            movieInfo.appendChild(paragraph);
        });

        // Añadir todo al contenedor de detalles
        this.movieDetails.appendChild(movieInfo);
    }

    displayError(message) {
        console.error('Error message:', message);

        // Limpiar el contenedor de detalles
        while (this.movieDetails.firstChild) {
            this.movieDetails.removeChild(this.movieDetails.firstChild);
        }

        // Crear el contenedor de error
        const errorDiv = document.createElement('div');
        errorDiv.className = 'error-message';

        // Crear y configurar el mensaje de error
        const errorParagraph = document.createElement('p');
        const strongElement = document.createElement('strong');
        strongElement.textContent = 'Error: ';
        errorParagraph.appendChild(strongElement);

        // Añadir el mensaje de error como texto plano
        errorParagraph.appendChild(document.createTextNode(message));

        // Crear el mensaje adicional
        const retryParagraph = document.createElement('p');
        retryParagraph.textContent = 'Por favor intente de nuevo.';

        // Ensamblar la estructura
        errorDiv.appendChild(errorParagraph);
        errorDiv.appendChild(retryParagraph);

        // Añadir al DOM
        this.movieDetails.appendChild(errorDiv);
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

        // Limpiar el contenedor
        while (consultedMoviesContainer.firstChild) {
            consultedMoviesContainer.removeChild(consultedMoviesContainer.firstChild);
        }

        if (this.consultedMovies.length === 0) {
            console.log('No hay películas consultadas');
            const noMoviesMessage = document.createElement('p');
            noMoviesMessage.textContent = 'No se han consultado películas aún.';
            consultedMoviesContainer.appendChild(noMoviesMessage);
            return;
        }

        try {
            // Crear elementos DOM de forma segura
            this.consultedMovies.forEach((movie, index) => {
                const movieDiv = document.createElement('div');
                movieDiv.className = 'consulted-movie';

                const title = document.createElement('h3');
                title.textContent = `Película ${index + 1}`;
                movieDiv.appendChild(title);

                const pre = document.createElement('pre');
                pre.className = 'consulted-movies-json';

                // Sanitizar el contenido JSON
                const safeJson = document.createTextNode(JSON.stringify(movie, null, 2));
                pre.appendChild(safeJson);

                movieDiv.appendChild(pre);
                consultedMoviesContainer.appendChild(movieDiv);
            });

            console.log('Contenido actualizado exitosamente');
        } catch (error) {
            console.error('Error al actualizar la lista:', error);
            const errorMessage = document.createElement('p');
            errorMessage.textContent = `Error al actualizar la lista: ${error.message}`;
            consultedMoviesContainer.appendChild(errorMessage);
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    window.starWarsApp = new StarWarsApp();
    console.log('Star Wars App initialized');
});