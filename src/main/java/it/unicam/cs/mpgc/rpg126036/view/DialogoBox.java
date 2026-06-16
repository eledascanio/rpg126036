package it.unicam.cs.mpgc.rpg126036.view;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.List;

/**
 * Costruisce il dialog box degli NPC: una striscia in basso allo schermo con nome
 * e testo rivelato un carattere alla volta (effetto macchina da scrivere), seguito
 * dal prompt di avanzamento o dalle scelte proposte.
 *
 * <p>E' una vista pura: non conosce la logica di gioco, riceve dal chiamante i
 * testi, l'azione di chiusura e le {@link OpzioneDialogo}. Restituisce una
 * {@link Sessione} con il nodo da mostrare, la sua animazione e l'azione di
 * avanzamento, cosi' che la {@link ExplorationView} possa pilotarle da tastiera
 * (tasto E) e fermarle alla chiusura dell'overlay.</p>
 */
final class DialogoBox {

    /** Ritmo dell'effetto macchina da scrivere (come la schermata iniziale). */
    private static final Duration RITMO_DIALOGO = Duration.millis(18);

    private DialogoBox() {
    }

    /**
     * Il dialog box costruito e pronto: il nodo da inserire nell'overlay,
     * l'animazione a macchina da scrivere e l'azione di avanzamento manuale.
     *
     * @param nodo        la striscia di dialogo da mostrare
     * @param macchina    l'animazione che rivela il testo
     * @param avanzamento l'azione invocata da clic o tasto E
     */
    record Sessione(Node nodo, Timeline macchina, Runnable avanzamento) {

        /** Avvia l'effetto macchina da scrivere. */
        void avvia() {
            macchina.play();
        }
    }

    /**
     * Costruisce un dialog box. Quando il testo è interamente rivelato: se
     * {@code opzioni} è vuota un ulteriore input (E o clic) esegue {@code alTermine}
     * (di norma la chiusura); altrimenti compaiono i pulsanti di scelta e
     * l'avanzamento da tastiera/clic diventa inerte (si prosegue scegliendo).
     *
     * @param root      il contenitore radice, per dimensionare il box in proporzione
     * @param nome      nome dell'NPC mostrato in alto
     * @param testo     battuta rivelata a macchina da scrivere
     * @param alTermine azione eseguita al termine quando non ci sono scelte
     * @param opzioni   le scelte proposte al termine (eventualmente vuote)
     * @return la sessione pronta da mostrare e avviare
     */
    static Sessione crea(Region root, String nome, String testo, Runnable alTermine, List<OpzioneDialogo> opzioni) {
        Label etichettaNome = new Label(nome);
        etichettaNome.getStyleClass().add("dialog-name");

        Label etichettaTesto = new Label();
        etichettaTesto.getStyleClass().add("dialog-text");
        etichettaTesto.setWrapText(true);
        etichettaTesto.setMaxWidth(Double.MAX_VALUE);

        Label prompt = new Label("▼  E / clic per proseguire");
        prompt.getStyleClass().add("dialog-prompt");
        HBox rigaPrompt = new HBox(prompt);
        rigaPrompt.setAlignment(Pos.CENTER_RIGHT);

        // Striscia orizzontale in basso: nome in alto, testo al centro (2-3 righe),
        // indicatore in basso a destra.
        BorderPane box = new BorderPane();
        box.getStyleClass().addAll("dialog-box", "pixel-font");
        box.setTop(etichettaNome);
        box.setCenter(etichettaTesto);
        box.setBottom(rigaPrompt);
        BorderPane.setAlignment(etichettaTesto, Pos.TOP_LEFT);
        BorderPane.setMargin(etichettaTesto, new Insets(6, 0, 6, 0));

        // Larghezza piena, altezza limitata a ~28% dello schermo, ancorata in basso:
        // il resto della scena (mappa e personaggi) resta visibile sopra la barra.
        box.setMaxWidth(Double.MAX_VALUE);
        box.prefHeightProperty().bind(root.heightProperty().multiply(0.28));
        box.setMaxHeight(Region.USE_PREF_SIZE);
        StackPane.setAlignment(box, Pos.BOTTOM_CENTER);
        StackPane.setMargin(box, new Insets(0, 16, 16, 16));

        // Effetto macchina da scrivere: un carattere a ogni frame.
        int[] indice = {0};
        Timeline macchina = new Timeline(new KeyFrame(RITMO_DIALOGO, e -> {
            indice[0]++;
            etichettaTesto.setText(testo.substring(0, indice[0]));
        }));
        macchina.setCycleCount(testo.length());

        // A testo completato compaiono le scelte (se presenti), sostituendo il prompt.
        Runnable mostraOpzioni = () -> {
            if (opzioni.isEmpty()) {
                return;
            }
            HBox barraOpzioni = new HBox(12);
            barraOpzioni.setAlignment(Pos.CENTER_RIGHT);
            for (OpzioneDialogo opzione : opzioni) {
                Button scelta = new Button(opzione.etichetta());
                scelta.getStyleClass().add("game-button");
                scelta.setOnAction(e -> opzione.azione().run());
                barraOpzioni.getChildren().add(scelta);
            }
            box.setBottom(barraOpzioni);
        };
        macchina.setOnFinished(e -> mostraOpzioni.run());

        Runnable avanzamento = () -> {
            if (macchina.getStatus() == Animation.Status.RUNNING) {
                // Prima rivelazione completa, poi le scelte (o, senza scelte, la chiusura).
                macchina.stop();
                etichettaTesto.setText(testo);
                mostraOpzioni.run();
            } else if (opzioni.isEmpty()) {
                alTermine.run();
            }
            // Con le scelte già mostrate l'avanzamento è inerte: si sceglie un pulsante.
        };
        box.setOnMouseClicked(e -> avanzamento.run());

        return new Sessione(box, macchina, avanzamento);
    }
}
