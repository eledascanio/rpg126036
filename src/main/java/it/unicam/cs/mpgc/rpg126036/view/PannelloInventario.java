package it.unicam.cs.mpgc.rpg126036.view;

import it.unicam.cs.mpgc.rpg126036.achievement.Achievement;
import it.unicam.cs.mpgc.rpg126036.achievement.AchievementManager;
import it.unicam.cs.mpgc.rpg126036.engine.GameState;
import it.unicam.cs.mpgc.rpg126036.model.Clue;
import it.unicam.cs.mpgc.rpg126036.model.Item;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

/**
 * Costruisce il pannello dell'inventario ("Diario") con tre sezioni selezionabili:
 * "Indizi" (il diario degli indizi), "Prove" (gli oggetti raccolti) e "Obiettivi"
 * (i traguardi sbloccati).
 *
 * <p>Legge i dati dallo {@link GameState} e dall'{@link AchievementManager}; non
 * gestisce l'overlay né il pulsante che lo apre, che restano nella
 * {@link ExplorationView}. Comunica con il chiamante solo tramite l'azione di
 * chiusura.</p>
 */
final class PannelloInventario {

    private PannelloInventario() {
    }

    /**
     * Crea il pannello dell'inventario, gia' posizionato sulla sezione "Indizi".
     *
     * @param stato              lo stato della partita, da cui leggere diario e oggetti
     * @param achievementManager il gestore dei traguardi, per la sezione "Obiettivi"
     * @param allaChiusura       azione eseguita dal pulsante "Chiudi"
     * @return il pannello pronto da avvolgere in un overlay
     */
    static VBox crea(GameState stato, AchievementManager achievementManager, Runnable allaChiusura) {
        Label titolo = new Label("Diario");
        titolo.getStyleClass().add("overlay-title");

        // Area scorrevole riempita in base alla sezione selezionata.
        VBox contenuto = new VBox(14);
        contenuto.setAlignment(Pos.TOP_LEFT);
        ScrollPane scorri = new ScrollPane(contenuto);
        scorri.setFitToWidth(true);
        scorri.setPrefViewportHeight(280);
        scorri.setMaxWidth(560);
        scorri.getStyleClass().add("inventory-scroll");

        Button indizi = new Button("Indizi");
        Button prove = new Button("Prove");
        Button obiettivi = new Button("Obiettivi");
        List<Button> tab = List.of(indizi, prove, obiettivi);
        for (Button b : tab) {
            b.getStyleClass().add("game-button");
            b.setFocusTraversable(false);
        }
        indizi.setOnAction(e -> selezionaSezione(tab, indizi, contenuto, c -> riempiIndizi(stato, c)));
        prove.setOnAction(e -> selezionaSezione(tab, prove, contenuto, c -> riempiProve(stato, c)));
        obiettivi.setOnAction(e -> selezionaSezione(tab, obiettivi, contenuto, c -> riempiObiettivi(achievementManager, c)));

        HBox sezioni = new HBox(10, indizi, prove, obiettivi);
        sezioni.setAlignment(Pos.CENTER);

        Button chiudi = new Button("Chiudi");
        chiudi.getStyleClass().add("game-button");
        chiudi.setOnAction(e -> allaChiusura.run());

        VBox pannello = new VBox(18, titolo, sezioni, scorri, chiudi);
        pannello.setAlignment(Pos.CENTER);
        pannello.setMaxWidth(620);
        pannello.getStyleClass().add("pixel-font");

        // All'apertura mostra la sezione "Indizi".
        selezionaSezione(tab, indizi, contenuto, c -> riempiIndizi(stato, c));
        return pannello;
    }

    /** Evidenzia il pulsante della sezione scelta e ne ricostruisce il contenuto. */
    private static void selezionaSezione(List<Button> tab, Button attivo, VBox contenuto,
                                         Consumer<VBox> riempitore) {
        tab.forEach(b -> b.getStyleClass().remove("tab-attivo"));
        attivo.getStyleClass().add("tab-attivo");
        contenuto.getChildren().clear();
        riempitore.accept(contenuto);
    }

    /** Sezione "Indizi": le informazioni raccolte nel diario del giocatore. */
    private static void riempiIndizi(GameState stato, VBox contenuto) {
        List<Clue> indizi = stato.getDiario().getIndizi();
        if (indizi.isEmpty()) {
            contenuto.getChildren().add(voceVuota("Nessun indizio raccolto, per ora."));
            return;
        }
        indizi.forEach(c -> contenuto.getChildren().add(voce("• " + c.titolo(), c.testo())));
    }

    /** Sezione "Prove": gli oggetti fisici raccolti nell'inventario. */
    private static void riempiProve(GameState stato, VBox contenuto) {
        List<Item> prove = stato.getInventario().getOggetti();
        if (prove.isEmpty()) {
            contenuto.getChildren().add(voceVuota("Nessuna prova raccolta, per ora."));
            return;
        }
        prove.forEach(i -> contenuto.getChildren().add(voce("• " + i.nome(), i.descrizione())));
    }

    /**
     * Sezione "Obiettivi": mostra solo i traguardi già sbloccati. Quelli ancora da
     * ottenere restano invisibili, così non ne svelano in anticipo l'esistenza.
     */
    private static void riempiObiettivi(AchievementManager achievementManager, VBox contenuto) {
        List<Achievement> sbloccati = achievementManager.getSbloccati();
        if (sbloccati.isEmpty()) {
            contenuto.getChildren().add(voceVuota("Nessun obiettivo sbloccato, per ora."));
            return;
        }
        for (Achievement a : sbloccati) {
            Node voce = voce("🏆 " + a.titolo(), a.descrizione());
            Label esito = new Label("Sbloccato");
            esito.getStyleClass().add("achievement-unlocked");
            ((VBox) voce).getChildren().add(esito);
            contenuto.getChildren().add(voce);
        }
    }

    /** Voce dell'inventario: titolo in evidenza e testo descrittivo a capo. */
    private static Node voce(String titolo, String testo) {
        Label t = new Label(titolo);
        t.getStyleClass().add("inventory-entry-title");
        Label d = new Label(testo);
        d.getStyleClass().add("inventory-entry-text");
        d.setWrapText(true);
        d.setMaxWidth(520);
        return new VBox(2, t, d);
    }

    /** Messaggio segnaposto per una sezione ancora vuota. */
    private static Node voceVuota(String messaggio) {
        Label l = new Label(messaggio);
        l.getStyleClass().add("inventory-entry-text");
        l.setWrapText(true);
        l.setMaxWidth(520);
        return l;
    }
}
