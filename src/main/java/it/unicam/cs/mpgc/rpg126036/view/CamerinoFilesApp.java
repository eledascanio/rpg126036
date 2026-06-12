package it.unicam.cs.mpgc.rpg126036.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Applicazione JavaFX di <i>Camerino Files</i>. Crea la finestra principale e vi
 * inserisce la {@link ViewNavigator} che gestisce il passaggio tra le schermate.
 *
 * <p>Per ora avvia la schermata {@link HomeView}; le viste di gioco verranno
 * agganciate alla stessa navigazione man mano che vengono realizzate.</p>
 */
public class CamerinoFilesApp extends Application {

    /** Titolo della finestra e del gioco. */
    public static final String TITOLO = "Camerino Files";

    private static final double LARGHEZZA = 960;
    private static final double ALTEZZA = 600;

    @Override
    public void start(Stage stage) {
        ViewNavigator navigator = new ViewNavigator();
        navigator.mostra(new HomeView(navigator).getRoot());

        Scene scena = new Scene(navigator.getRoot(), LARGHEZZA, ALTEZZA);
        stage.setTitle(TITOLO);
        stage.setScene(scena);
        stage.show();
    }
}
