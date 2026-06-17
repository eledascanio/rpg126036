package it.unicam.cs.mpgc.rpg126036.view;

import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.util.List;
import java.util.Objects;

/**
 * Gestore dei pannelli modali in sovrimpressione (overlay) della schermata di
 * esplorazione: dialoghi, messaggi, enigmi, pausa e schermate di fine capitolo.
 * Tiene un solo pannello alla volta e l'eventuale dialogo a macchina da scrivere in
 * corso, ed espone alla schermata se un overlay è aperto e chiudibile.
 *
 * <p>Estratto dalla {@link ExplorationView} per isolare il meccanismo modale: i vari
 * "mostraXxx" che costruiscono i contenuti specifici restano nella schermata e si
 * appoggiano qui per velare, mostrare e chiudere. All'apertura esegue l'azione
 * {@code allApertura} (azzera i tasti premuti, così il movimento non prosegue sotto
 * l'overlay).</p>
 */
final class GestoreOverlay {

    private final StackPane root;
    private final Runnable allApertura;

    // Un solo pannello modale alla volta in sovrimpressione.
    private Node overlayCorrente;
    private boolean overlayChiudibile;
    // Dialogo NPC eventualmente in corso: animazione e azione di avanzamento/chiusura.
    private Timeline dialogoMacchina;
    private Runnable avanzamentoDialogo;

    /**
     * @param root        il contenitore radice su cui sovrapporre gli overlay
     * @param allApertura azione eseguita all'apertura di un overlay (azzera i tasti)
     */
    GestoreOverlay(StackPane root, Runnable allApertura) {
        this.root = Objects.requireNonNull(root, "La radice non puo' essere nulla.");
        this.allApertura = Objects.requireNonNull(allApertura, "L'azione di apertura non puo' essere nulla.");
    }

    /** @return {@code true} se c'è un overlay in sovrimpressione. */
    boolean isAperto() {
        return overlayCorrente != null;
    }

    /** @return {@code true} se l'overlay corrente è chiudibile con ESC. */
    boolean isChiudibile() {
        return overlayChiudibile;
    }

    /** Avvolge un contenuto nel velo scuro dell'overlay. */
    StackPane velo(Node contenuto) {
        StackPane overlay = new StackPane(contenuto);
        overlay.getStyleClass().add("overlay-veil");
        return overlay;
    }

    /** Mostra un overlay (eventualmente chiudibile con ESC), sostituendo il precedente. */
    void mostra(Node velo, boolean chiudibile) {
        chiudi();
        overlayCorrente = velo;
        overlayChiudibile = chiudibile;
        allApertura.run();
        root.getChildren().add(velo);
    }

    /** Chiude l'overlay corrente, fermando l'eventuale dialogo in corso. */
    void chiudi() {
        // Ferma l'eventuale dialogo in corso prima di rimuovere il pannello.
        if (dialogoMacchina != null) {
            dialogoMacchina.stop();
            dialogoMacchina = null;
        }
        avanzamentoDialogo = null;
        if (overlayCorrente != null) {
            root.getChildren().remove(overlayCorrente);
            overlayCorrente = null;
        }
    }

    /** Mostra un dialog box (nome + testo) con chiusura standard. */
    void mostraDialogo(String nome, String testo) {
        mostraDialogo(nome, testo, this::chiudi, List.of());
    }

    /**
     * Variante completa del dialog box. Quando il testo è interamente rivelato: se
     * {@code opzioni} è vuota un ulteriore input (E o clic) esegue {@code alTermine}
     * (di norma la chiusura); altrimenti compaiono i pulsanti di scelta e l'avanzamento
     * da tastiera/clic diventa inerte (si prosegue scegliendo un'opzione).
     *
     * @param nome      nome dell'NPC mostrato in alto
     * @param testo     battuta rivelata a macchina da scrivere
     * @param alTermine azione eseguita al termine quando non ci sono scelte
     * @param opzioni   le scelte proposte al termine (eventualmente vuote)
     */
    void mostraDialogo(String nome, String testo, Runnable alTermine, List<OpzioneDialogo> opzioni) {
        DialogoBox.Sessione dialogo = DialogoBox.crea(root, nome, testo, alTermine, opzioni);
        // I campi vanno impostati dopo mostra(), che azzera lo stato precedente.
        mostra(dialogo.nodo(), true);
        dialogoMacchina = dialogo.macchina();
        avanzamentoDialogo = dialogo.avanzamento();
        dialogo.avvia();
    }

    /**
     * Fa avanzare il dialogo in corso, se presente (tasto E o clic): completa il testo
     * o lo chiude.
     *
     * @return {@code true} se un dialogo era in corso ed è stato fatto avanzare
     */
    boolean avanzaDialogoSePresente() {
        if (avanzamentoDialogo != null) {
            avanzamentoDialogo.run();
            return true;
        }
        return false;
    }
}
