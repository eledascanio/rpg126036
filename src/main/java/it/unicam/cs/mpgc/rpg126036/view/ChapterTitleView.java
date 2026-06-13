package it.unicam.cs.mpgc.rpg126036.view;

import javafx.animation.PauseTransition;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.Objects;

/**
 * Cartello a schermo: schermata nera con una scritta (nome del capitolo o
 * dell'ambientazione), mostrata per pochi secondi prima di proseguire
 * automaticamente al passo successivo della sequenza.
 *
 * <p>Il passo successivo è fornito come {@link Runnable}, così più cartelli
 * possono essere concatenati (ad es. "Capitolo 1" seguito dal nome della scena).</p>
 */
public class ChapterTitleView {

    private static final Duration DURATA = Duration.seconds(2.5);

    private final StackPane root;

    /**
     * @param context       il contesto applicativo (non nullo)
     * @param titolo        la scritta da mostrare (non nulla)
     * @param proseguimento azione da eseguire allo scadere del cartello (non nulla)
     */
    public ChapterTitleView(AppContext context, String titolo, Runnable proseguimento) {
        Objects.requireNonNull(context, "Il contesto non puo' essere nullo.");
        Objects.requireNonNull(titolo, "Il titolo non puo' essere nullo.");
        Objects.requireNonNull(proseguimento, "Il proseguimento non puo' essere nullo.");

        Label etichetta = new Label(titolo);
        etichetta.getStyleClass().add("title-card-text");

        root = new StackPane(etichetta);
        // title-card per lo sfondo nero, pixel-font per il font VT323 come le altre schermate.
        root.getStyleClass().addAll("title-card", "pixel-font");

        // Dopo una breve pausa si passa al cartello/scena successivi.
        PauseTransition pausa = new PauseTransition(DURATA);
        pausa.setOnFinished(e -> proseguimento.run());
        pausa.play();
    }

    /**
     * @return il nodo radice della schermata
     */
    public Parent getRoot() {
        return root;
    }
}
