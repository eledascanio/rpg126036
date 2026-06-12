package it.unicam.cs.mpgc.rpg126036.view;

import it.unicam.cs.mpgc.rpg126036.app.ContentResolver;
import it.unicam.cs.mpgc.rpg126036.app.GameSessionFactory;
import it.unicam.cs.mpgc.rpg126036.persistence.Campaign;
import it.unicam.cs.mpgc.rpg126036.persistence.CampaignLoader;
import it.unicam.cs.mpgc.rpg126036.persistence.SaveRepository;
import it.unicam.cs.mpgc.rpg126036.persistence.XmlSaveRepository;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Applicazione JavaFX di <i>Camerino Files</i>. All'avvio carica la campagna,
 * prepara i servizi condivisi ({@link AppContext}) e mostra la {@link HomeView}.
 *
 * <p>La finestra ospita la {@link ViewNavigator}, che gestisce il passaggio tra le
 * schermate: le viste di gioco vengono agganciate alla stessa navigazione man mano
 * che vengono realizzate.</p>
 */
public class CamerinoFilesApp extends Application {

    /** Titolo della finestra e del gioco. */
    public static final String TITOLO = "Camerino Files";

    private static final double LARGHEZZA = 960;
    private static final double ALTEZZA = 600;

    @Override
    public void start(Stage stage) {
        ViewNavigator navigator = new ViewNavigator();
        SaveRepository repository = new XmlSaveRepository();
        Campaign campaign = new CampaignLoader().caricaStandard();
        GameSessionFactory sessionFactory = new GameSessionFactory(repository, campaign);
        AppContext context = new AppContext(navigator, sessionFactory, new ContentResolver());

        navigator.mostra(new HomeView(context).getRoot());

        Scene scena = new Scene(navigator.getRoot(), LARGHEZZA, ALTEZZA);
        applicaTema(scena);
        stage.setTitle(TITOLO);
        stage.setScene(scena);
        stage.show();
    }

    /** Applica il foglio di stile del tema, se presente sul classpath. */
    private void applicaTema(Scene scena) {
        var css = getClass().getResource("/css/app.css");
        if (css != null) {
            scena.getStylesheets().add(css.toExternalForm());
        }
    }
}
