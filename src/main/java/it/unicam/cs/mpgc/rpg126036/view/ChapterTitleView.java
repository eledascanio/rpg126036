package it.unicam.cs.mpgc.rpg126036.view;

import it.unicam.cs.mpgc.rpg126036.app.GameSession;
import javafx.animation.PauseTransition;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.Objects;

/**
 * Cartello del capitolo: schermata nera con il nome dell'ambientazione, mostrata
 * per pochi secondi prima di passare automaticamente all'esplorazione.
 */
public class ChapterTitleView {

    private static final Duration DURATA = Duration.seconds(2.5);

    private final StackPane root;

    public ChapterTitleView(AppContext context, GameSession session, String titolo) {
        Objects.requireNonNull(context, "Il contesto non puo' essere nullo.");
        Objects.requireNonNull(session, "La sessione non puo' essere nulla.");
        Objects.requireNonNull(titolo, "Il titolo non puo' essere nullo.");

        Label etichetta = new Label(titolo);
        etichetta.getStyleClass().add("title-card-text");

        root = new StackPane(etichetta);
        root.getStyleClass().add("title-card");

        // Dopo una breve pausa si entra nella mappa esplorabile.
        PauseTransition pausa = new PauseTransition(DURATA);
        pausa.setOnFinished(e -> context.navigator().mostra(
                new ExplorationView(context, session).getRoot()));
        pausa.play();
    }

    /**
     * @return il nodo radice della schermata
     */
    public Parent getRoot() {
        return root;
    }
}
