package it.unicam.cs.mpgc.rpg126036.view;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.Objects;

/**
 * Schermata principale (Home): titolo del gioco e le opzioni di avvio.
 *
 * <p>Le voci "Nuova partita" e "Carica partita" rimandano per ora a una
 * schermata segnaposto: verranno collegate alle rispettive viste (creazione
 * personaggio e selezione slot) quando saranno realizzate. "Esci" chiude
 * l'applicazione.</p>
 */
public class HomeView {

    private final VBox root;

    public HomeView(ViewNavigator navigator) {
        Objects.requireNonNull(navigator, "Il navigator non puo' essere nullo.");

        Label titolo = new Label("CAMERINO FILES");
        titolo.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: #c0392b;");

        Button nuova = pulsante("Nuova partita");
        Button carica = pulsante("Carica partita");
        Button esci = pulsante("Esci");

        nuova.setOnAction(e -> navigator.mostra(new PlaceholderView(navigator, "Nuova partita").getRoot()));
        carica.setOnAction(e -> navigator.mostra(new PlaceholderView(navigator, "Carica partita").getRoot()));
        esci.setOnAction(e -> Platform.exit());

        root = new VBox(16, titolo, nuova, carica, esci);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #111111;");
    }

    private Button pulsante(String testo) {
        Button pulsante = new Button(testo);
        pulsante.setPrefWidth(240);
        return pulsante;
    }

    /**
     * @return il nodo radice della schermata
     */
    public Parent getRoot() {
        return root;
    }
}
