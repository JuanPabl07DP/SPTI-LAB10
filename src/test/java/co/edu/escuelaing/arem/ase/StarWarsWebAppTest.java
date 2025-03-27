// Archivo: StarWarsWebAppTest.java

package co.edu.escuelaing.arem.ase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

 class StarWarsWebAppTest {
    private StarWarsWebApp webApp;

    @BeforeEach
     void setUp() {
        webApp = new StarWarsWebApp();
        webApp.staticfiles("target/classes/public");
    }

    @Test
     void testGetHtml() {
        byte[] response = null;
        try {
            response = webApp.getStaticFile("/index.html");
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        assertNotNull(response);
    }

    @Test
     void testNotGetHtml() {
        byte[] response = null;
        try {
            response = webApp.getStaticFile("/index2.html");
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        assertNull(response);
    }

//    @Test
//    public void testGetCss() {
//        byte[] response = null;
//        try {
//            response = webApp.getStaticFile("/css/style.css");
//        } catch (IOException | URISyntaxException e) {
//            e.printStackTrace();
//        }
//        assertNotNull(response);
//    }
}
