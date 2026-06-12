package it.unicam.cs.mpgc.rpg126036.view;

import it.unicam.cs.mpgc.rpg126036.app.GameSession;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

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

            Non puoi permetterlo. Non per Antonio. Hai solo pochi minuti prima di sentire le sirene \
            in lontananza. Trova chi è stato.""";

    private final StackPane root;

    public IntroView(AppContext context, GameSession session) {
        Objects.requireNonNull(context, "Il contesto non puo' essere nullo.");
        Objects.requireNonNull(session, "La sessione non puo' essere nulla.");

        Label testo = new Label(MONOLOGO);
        testo.getStyleClass().add("intro-text");
        testo.setWrapText(true);
        testo.setMaxWidth(680);
        testo.setTextAlignment(javafx.scene.text.TextAlignment.JUSTIFY);

        Button prosegui = new Button("Prosegui");
        prosegui.getStyleClass().add("menu-button");
        prosegui.setOnAction(e -> context.navigator().mostra(
                new ChapterTitleView(context, session, "Cortile del Polo di Informatica").getRoot()));

        VBox contenuto = new VBox(36, testo, prosegui);
        contenuto.setAlignment(Pos.CENTER);
        contenuto.setPadding(new Insets(48));

        root = new StackPane(contenuto);
        root.getStyleClass().add("title-card");
    }

    /**
     * @return il nodo radice della schermata
     */
    public Parent getRoot() {
        return root;
    }
}
