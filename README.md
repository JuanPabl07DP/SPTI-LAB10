# **Star Wars Movie API - Taller 2 AREP**
## 📌 Descripción
## 🚀 Tecnologías Utilizadas

- Java 8+

- Spring Boot

- Maven

- Gson

- HTML, CSS y JavaScript

- SWAPI (Star Wars API)

## 🎬 Funcionamiento de la Aplicación

1. El usuario ingresa el ID del episodio de Star Wars en la interfaz web.

2. La a aplicación realiza una solicitud a la API de SWAPI para obtener los datos de la película correspondiente.

3. La información obtenida es procesada y mostrada en la interfaz de usuario de forma clara y estructurada.

## 🔧 Instalación y Ejecución

1️⃣ **Clonar el repositorio**

```
git clone https://github.com/tu-repo/AREP_Taller2.git
cd AREP_Taller2
```

2️⃣ **Compilar el proyecto con Maven**

```
mvn clean install
```

3️⃣ **Ejecutar la aplicación**

```
mvn spring-boot:run
```

4️⃣ **Acceder a la aplicación**

Abre tu navegador y dirígete a:

```
http://localhost:8080/
```

## 🔗 Endpoints Disponibles

-  ```GET /movie/{id}``` → Devuelve información sobre la película correspondiente al episodio indicado.

- ```GET /``` → Página principal de la aplicación.

## ⚠️ Posibles Errores y Soluciones

- **Película no encontrada:** Asegúrate de que el número de la película a consultar esté entre 1 y 7, ya que la API solo cubre esas entregas de la saga.

- **Error de conexión:** Verifica tu conexión a Internet y asegúrate de que la API de SWAPI está en línea.

### 📌 Autores:
- Juan Pablo Daza Pereira

### 📅 Fecha de creación: Febrero 2025
