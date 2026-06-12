package it.unicam.cs.mpgc.rpg126036.view;

import javafx.scene.Parent;
import javafx.scene.layout.StackPane;

import java.util.Objects;

/**
 * Gestore della navigazione tra le schermate: mantiene un contenitore radice
 * unico e ne sostituisce il contenuto quando si cambia vista.
 *
 * <p>Disaccoppia le viste dal modo in cui vengono mostrate: ciascuna vista riceve
 * il navigator e si limita a chiedere il passaggio a un'altra, senza conoscere lo
 * {@code Stage} o la {@code Scene}. Aggiungere una nuova schermata non richiede
 * modifiche qui.</p>
 */
public class ViewNavigator {

    private final StackPane root = new StackPane();

    /**
     * @return il contenitore radice da inserire nella {@code Scene}
     */
    public Parent getRoot() {
        return root;
    }

    /**
     * Mostra la vista indicata, sostituendo quella corrente.
     *
     * @param vista la vista da visualizzare (non nulla)
     */
    public void mostra(Parent vista) {
        Objects.requireNonNull(vista, "La vista non puo' essere nulla.");
        root.getChildren().setAll(vista);
    }
}
