package it.unicam.cs.mpgc.rpg126036.view;

import it.unicam.cs.mpgc.rpg126036.model.Puzzle;
import it.unicam.cs.mpgc.rpg126036.model.StatType;
import it.unicam.cs.mpgc.rpg126036.model.Transition;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.util.List;
import java.util.function.Consumer;

/**
 * Servizi di scena che la schermata di esplorazione mette a disposizione dei
 * componenti di trama estratti ({@link AulaB}, {@link Porte}, {@link DialoghiNpc},
 * {@link PcVittima}): registrazione e rimozione degli elementi sulla mappa, dialog
 * box e pannelli in sovrimpressione, aggiornamento dell'HUD, potenziamenti,
 * avanzamento del motore ed effetti di scena (torcia, animazioni).
 *
 * <p>Astrazione introdotta per invertire la dipendenza (DIP): i componenti di
 * trama dipendono da questo contratto e non dalla classe concreta
 * {@link ExplorationView}, che ne è l'unica implementazione.</p>
 */
public interface RegiaEsplorazione {

    /** Energia consumata a ogni dialogo con un NPC (regola di gioco), e penalità affini. */
    int COSTO_ENERGIA_DIALOGO = 10;

    // ----------------------------------------------------------------------
    // Elementi e animazioni della mappa
    // ----------------------------------------------------------------------

    /** Registra un elemento interattivo sulla mappa, mostrandone nodo ed etichetta. */
    void registra(ElementoScena elemento);

    /** Rimuove un elemento dalla mappa (e dall'eventuale suggerimento di prossimità). */
    void rimuoviElemento(ElementoScena elemento);

    /** Registra un'animazione di scena, da fermare alla ricostruzione della stessa. */
    void registraAnimazione(Timeline animazione);

    // ----------------------------------------------------------------------
    // Effetto torcia (Aula B)
    // ----------------------------------------------------------------------

    /** Aggiunge il velo della torcia sopra lo scenario (Aula B al buio). */
    void aggiungiTorcia();

    /** Ricalcola il foro di luce della torcia centrandolo sul giocatore. */
    void aggiornaTorcia();

    // ----------------------------------------------------------------------
    // Dialoghi e pannelli in sovrimpressione
    // ----------------------------------------------------------------------

    /** Mostra un dialog box (nome + testo) con chiusura standard. */
    void mostraDialogo(String nome, String testo);

    /**
     * Mostra un dialog box completo: al termine del testo, se non ci sono opzioni
     * esegue {@code alTermine}, altrimenti presenta le scelte.
     */
    void mostraDialogo(String nome, String testo, Runnable alTermine, List<OpzioneDialogo> opzioni);

    /** Mostra l'enigma a tastierino dell'elemento indicato (porta del laboratorio). */
    void mostraEnigma(Puzzle puzzle, ElementoScena elemento);

    /** Avvolge un contenuto nel velo scuro dell'overlay. */
    StackPane velo(Node contenuto);

    /** Mostra un overlay (eventualmente chiudibile con ESC), sostituendo il precedente. */
    void mostraOverlay(Node velo, boolean chiudibile);

    /** Chiude l'overlay corrente, fermando l'eventuale dialogo in corso. */
    void chiudiOverlay();

    // ----------------------------------------------------------------------
    // Progressione: HUD, potenziamenti, traguardi, avanzamento
    // ----------------------------------------------------------------------

    /** Aggiorna l'HUD (energia, XP, statistiche) sullo stato corrente. */
    void aggiornaHud();

    /**
     * Se il giocatore ha raggiunto la soglia di XP, mostra la scelta del
     * potenziamento (ripetuta a catena); al termine — o subito — esegue {@code dopo}.
     */
    void mostraPotenziamentoSeDovuto(Runnable dopo);

    /** Mostra la schermata di scelta della statistica da potenziare. */
    void mostraSceltaUpgrade(String titolo, String sottotitolo, Consumer<StatType> azione);

    /** Annuncia col dialog box un eventuale traguardo appena sbloccato, poi esegue {@code dopo}. */
    void annunciaTraguardoSePresente(Runnable dopo);

    /** Mostra una sola volta il pensiero d'indagine verso il PC, se ne ricorrono le condizioni. */
    void forsePensieroIndagine();

    /** Segue una transizione verso un'altra scena (con il relativo cartello). */
    void usaUscita(Transition transizione);

    /** Avanza il motore alla scena indicata senza ricostruire la vista (scene di servizio). */
    void avanzaSenzaRicostruire(String idScena);
}
