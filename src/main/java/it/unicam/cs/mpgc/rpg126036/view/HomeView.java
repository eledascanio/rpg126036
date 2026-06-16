package it.unicam.cs.mpgc.rpg126036.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.InputStream;
import java.util.Objects;

/**
 * Schermata principale (Home) in stile noir/terminale: immagine di sfondo con un
 * velo scuro per la leggibilità, titolo, menu e riquadri decorativi.
 *
 * <p>"Nuova partita" porta alla {@link CharacterCreationView}; "Carica partita"
 * apre la {@link LoadGameView} con gli slot salvati; "Esci" chiude l'applicazione.
 * Lo stile è definito nel foglio {@code /css/app.css} tramite gli styleClass.</p>
 */
public class HomeView {

    /** Risorsa dell'immagine di sfondo della Home. */
    private static final String SFONDO = "/images/home-background.jpg";

    private final AppContext context;
    private final StackPane root;

    public HomeView(AppContext context) {
        this.context = Objects.requireNonNull(context, "Il contesto non puo' essere nullo.");

        root = new StackPane();
        root.getStyleClass().addAll("screen-root", "pixel-font");
        root.getChildren().addAll(livelloSfondo(), livelloVelo(), livelloContenuto());
    }

    /** Immagine di sfondo ridimensionata a coprire l'intera area. */
    private Region livelloSfondo() {
        Region sfondo = new Region();
        InputStream risorsa = getClass().getResourceAsStream(SFONDO);
        if (risorsa != null) {
            BackgroundSize cover = new BackgroundSize(
                    BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true);
            sfondo.setBackground(new Background(new BackgroundImage(new Image(risorsa),
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER, cover)));
        }
        return sfondo;
    }

    /** Velo scuro sopra l'immagine, per il contrasto del testo. */
    private Region livelloVelo() {
        Region velo = new Region();
        velo.getStyleClass().add("background-veil");
        return velo;
    }

    /** Strato dei contenuti: titolo, menu, riquadro terminale e footer. */
    private BorderPane livelloContenuto() {
        Label titolo = new Label("CAMERINO FILES");
        titolo.getStyleClass().add("title");
        // Mantiene il font originale del titolo, escludendolo dal pixel font ereditato dal root.
        titolo.setStyle("-fx-font-family: \"Consolas\", \"Space Mono\", monospace;");

        VBox intestazione = new VBox(8, titolo);
        intestazione.setAlignment(Pos.CENTER);

        VBox menu = new VBox(12,
                voceMenu("Nuova Partita", "➜", e -> context.navigator()
                        .mostra(new CharacterCreationView(context).getRoot())),
                voceMenu("Carica Partita", "💾", e -> context.navigator()
                        .mostra(new LoadGameView(context).getRoot())),
                voceMenu("Esci", "⏻", e -> Platform.exit()));
        menu.setAlignment(Pos.CENTER);
        menu.setMaxWidth(320);

        VBox centro = new VBox(40, intestazione, menu);
        centro.setAlignment(Pos.CENTER);

        BorderPane contenuto = new BorderPane();
        contenuto.setCenter(centro);
        contenuto.setPadding(new Insets(32));
        return contenuto;
    }

    /**
     * Costruisce una voce di menu con il testo a sinistra e una piccola icona
     * all'estrema destra. Testo e azione restano invariati; l'icona è puramente
     * decorativa (un simbolo Unicode) ed eredita il colore del pulsante.
     */
    private Button voceMenu(String testo, String icona,
                            javafx.event.EventHandler<javafx.event.ActionEvent> azione) {
        Label etichetta = new Label(testo);
        etichetta.getStyleClass().add("menu-label");
        Label simbolo = new Label(icona);
        simbolo.getStyleClass().add("menu-icon");

        Region spazio = new Region();
        HBox.setHgrow(spazio, Priority.ALWAYS);
        HBox contenuto = new HBox(etichetta, spazio, simbolo);
        // Allineamento alla baseline del testo: così l'icona segue la linea della
        // scritta (già centrata nel pulsante) invece di apparire spostata in alto.
        contenuto.setAlignment(Pos.BASELINE_LEFT);

        Button pulsante = new Button();
        pulsante.getStyleClass().add("menu-button");
        pulsante.setMaxWidth(Double.MAX_VALUE);
        pulsante.setGraphic(contenuto);
        pulsante.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        // Il contenuto riempie la larghezza del pulsante (meno i bordi/padding),
        // così lo spazio elastico spinge l'icona contro il margine destro.
        contenuto.prefWidthProperty().bind(pulsante.widthProperty().subtract(44));
        pulsante.setOnAction(azione);
        return pulsante;
    }

    /**
     * @return il nodo radice della schermata
     */
    public Parent getRoot() {
        return root;
    }
}
