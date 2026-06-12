package it.unicam.cs.mpgc.rpg126036.view;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.Objects;

/**
 * Schermata segnaposto temporanea, usata dallo scheletro della GUI per le viste
 * non ancora realizzate. Mostra il nome della schermata di destinazione e un
 * pulsante per tornare alla {@link HomeView}.
 */
public class PlaceholderView {

    private final VBox root;

    public PlaceholderView(ViewNavigator navigator, String titoloSchermata) {
        Objects.requireNonNull(navigator, "Il navigator non puo' essere nullo.");
        Objects.requireNonNull(titoloSchermata, "Il titolo non puo' essere nullo.");

        Label etichetta = new Label(titoloSchermata + " — in costruzione");
        etichetta.setStyle("-fx-font-size: 24px; -fx-text-fill: #ecf0f1;");

        Button indietro = new Button("Indietro");
        indietro.setPrefWidth(160);
        indietro.setOnAction(e -> navigator.mostra(new HomeView(navigator).getRoot()));

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
