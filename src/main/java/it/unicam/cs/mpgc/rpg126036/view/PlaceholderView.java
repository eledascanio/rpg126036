package it.unicam.cs.mpgc.rpg126036.view;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.Objects;

/**
 * Schermata segnaposto temporanea, usata dallo scheletro della GUI per le viste
 * non ancora realizzate o per messaggi di conferma. Mostra un testo e un pulsante
 * per tornare alla {@link HomeView}.
 */
public class PlaceholderView {

    private final VBox root;

    public PlaceholderView(AppContext context, String messaggio) {
        Objects.requireNonNull(context, "Il contesto non puo' essere nullo.");
        Objects.requireNonNull(messaggio, "Il messaggio non puo' essere nullo.");

        Label etichetta = new Label(messaggio);
        etichetta.setStyle("-fx-font-size: 24px; -fx-text-fill: #ecf0f1;");

        Button indietro = new Button("Torna alla Home");
        indietro.setPrefWidth(180);
        indietro.setOnAction(e -> context.navigator().mostra(new HomeView(context).getRoot()));

        root = new VBox(20, etichetta, indietro);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #111111;");
    }

    /**
     * @return il nodo radice della schermata
     */
    public Parent getRoot() {
        return root;
    }
}
