package it.unicam.cs.mpgc.rpg126036.view;

import it.unicam.cs.mpgc.rpg126036.app.GameSession;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Objects;

/**
 * Schermata di introduzione narrativa: il monologo iniziale (testo rosso su sfondo
 * nero) e il pulsante "Prosegui". Premendolo si passa al cartello del capitolo e
 * quindi all'esplorazione.
 */
public class IntroView {

    private static final String MONOLOGO = """
            Camerino, ore 01:00. La nebbia avvolge il cortile del Polo Universitario, \
            tagliata fuori dai fumi della festa studentesca. Ma per te la festa finisce qui. \
            Antonio, il tuo compagno di studi, giace a terra, senza vita. L'allarme è scattato \
            e la Polizia è in viaggio, ma sai come funzionano queste cose: i rilievi bloccheranno \
            tutto, le piste si raffredderanno e l'assassino – che è sicuramente ancora qui intorno – \
            avrà il tempo di coprire le sue tracce e dileguarsi nel nulla.

            Non puoi permetterlo. Non per Antonio. Hai poco tempo prima di sentire le sirene \
            in lontananza. Trova chi è stato.""";

    /** Ritardo fra un carattere e il successivo nell'effetto macchina da scrivere. */
    private static final Duration RITMO_CARATTERE = Duration.millis(18);

    private final StackPane root;

    public IntroView(AppContext context, GameSession session) {
        Objects.requireNonNull(context, "Il contesto non puo' essere nullo.");
        Objects.requireNonNull(session, "La sessione non puo' essere nulla.");

        Label testo = new Label();
        testo.getStyleClass().add("intro-text");
        testo.setWrapText(true);
        testo.setMaxWidth(680);
        testo.setTextAlignment(javafx.scene.text.TextAlignment.JUSTIFY);

        Button prosegui = new Button("Prosegui");
        prosegui.getStyleClass().add("menu-button");
        prosegui.setOnAction(e -> avviaSequenzaCapitolo(context, session));

        VBox contenuto = new VBox(36, testo, prosegui);
        contenuto.setAlignment(Pos.CENTER);
        contenuto.setPadding(new Insets(48));

        root = new StackPane(contenuto);
        // title-card per lo sfondo nero, pixel-font per il font VT323 su testo e pulsante.
        root.getStyleClass().addAll("title-card", "pixel-font");

        Timeline macchinaDaScrivere = creaEffettoMacchinaDaScrivere(testo);
        // Un clic sullo sfondo rivela subito tutto il monologo, saltando l'animazione.
        root.setOnMouseClicked(e -> {
            macchinaDaScrivere.stop();
            testo.setText(MONOLOGO);
        });
        macchinaDaScrivere.play();
    }

    /**
     * Avvia la sequenza dei cartelli: prima "Capitolo 1", poi il nome
     * dell'ambientazione, infine la mappa esplorabile.
     */
    private void avviaSequenzaCapitolo(AppContext context, GameSession session) {
        Runnable vaiAEsplorazione = () -> context.navigator().mostra(
                new ExplorationView(context, session).getRoot());
        Runnable vaiAlCortile = () -> context.navigator().mostra(
                new ChapterTitleView(context, "Cortile del Polo di Informatica", vaiAEsplorazione).getRoot());
        context.navigator().mostra(
                new ChapterTitleView(context, "Capitolo 1", vaiAlCortile).getRoot());
    }

    /**
     * Costruisce l'animazione che svela il monologo un carattere alla volta,
     * creando l'effetto di scrittura in tempo reale davanti al giocatore.
     */
    private Timeline creaEffettoMacchinaDaScrivere(Label testo) {
        int[] indice = {0};
        Timeline timeline = new Timeline(new KeyFrame(RITMO_CARATTERE, e -> {
            indice[0]++;
            testo.setText(MONOLOGO.substring(0, indice[0]));
        }));
        // Un ciclo per ogni carattere del monologo.
        timeline.setCycleCount(MONOLOGO.length());
        return timeline;
    }

    /**
     * @return il nodo radice della schermata
     */
    public Parent getRoot() {
        return root;
    }
}
