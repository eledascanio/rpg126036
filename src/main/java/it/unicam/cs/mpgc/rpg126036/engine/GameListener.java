package it.unicam.cs.mpgc.rpg126036.engine;

import it.unicam.cs.mpgc.rpg126036.interaction.InteractionResult;
import it.unicam.cs.mpgc.rpg126036.model.Chapter;
import it.unicam.cs.mpgc.rpg126036.model.Item;
import it.unicam.cs.mpgc.rpg126036.model.Scene;

/**
 * Osservatore degli eventi di gioco emessi dal {@link GameEngine} (pattern
 * Observer). Consente a vista e controller di reagire ai cambiamenti di stato
 * senza accoppiarsi alla logica del motore.
 *
 * <p>Tutti i metodi hanno un'implementazione vuota di default: un osservatore
 * puo' ridefinire solo gli eventi di proprio interesse.</p>
 */
public interface GameListener {

    /**
     * Invocato quando la scena corrente cambia.
     *
     * @param scena la nuova scena corrente
     */
    default void onSceneChanged(Scene scena) {
    }

    /**
     * Invocato al termine della sequenza di fine capitolo (dopo ripristino
     * energia, upgrade e avanzamento).
     *
     * @param capitolo il capitolo appena concluso
     */
    default void onChapterCompleted(Chapter capitolo) {
    }

    /**
     * Invocato quando si avanza al capitolo successivo.
     *
     * @param capitolo il nuovo capitolo corrente
     */
    default void onChapterAdvanced(Chapter capitolo) {
    }

    /**
     * Invocato quando un oggetto viene raccolto.
     *
     * @param item l'oggetto trovato
     */
    default void onItemFound(Item item) {
    }

    /**
     * Invocato dopo l'esecuzione di un'interazione.
     *
     * @param risultato l'esito dell'interazione
     */
    default void onInteractionExecuted(InteractionResult risultato) {
    }

    /**
     * Invocato quando il giocatore accumula XP sufficienti
     * ({@link it.unicam.cs.mpgc.rpg126036.model.Player#COSTO_XP_POTENZIAMENTO})
     * per un potenziamento di statistica durante il gioco: la vista dovrebbe
     * mostrare il pannello di scelta ("Upgrade statistica disponibile") con le
     * tre statistiche selezionabili, oscurando lo sfondo senza interrompere la
     * narrazione.
     */
    default void onUpgradeStatisticaDisponibile() {
    }

    /**
     * Invocato quando la partita termina, sia per vittoria (ultimo capitolo
     * completato) sia per sconfitta (energia esaurita). La causa e' distinguibile
     * tramite lo stato del motore.
     */
    default void onGameOver() {
    }

    /**
     * Invocato quando il gioco viene messo in pausa: la vista dovrebbe oscurare
     * la schermata e mostrare l'indicatore di pausa.
     */
    default void onPaused() {
    }

    /**
     * Invocato quando il gioco riprende dalla pausa: la vista dovrebbe
     * ripristinare la schermata di gioco.
     */
    default void onResumed() {
    }
}
