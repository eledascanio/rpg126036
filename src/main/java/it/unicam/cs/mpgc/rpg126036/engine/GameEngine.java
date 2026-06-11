package it.unicam.cs.mpgc.rpg126036.engine;

import it.unicam.cs.mpgc.rpg126036.interaction.Interaction;
import it.unicam.cs.mpgc.rpg126036.interaction.InteractionResult;
import it.unicam.cs.mpgc.rpg126036.interaction.ItemInteraction;
import it.unicam.cs.mpgc.rpg126036.model.Chapter;
import it.unicam.cs.mpgc.rpg126036.model.Item;
import it.unicam.cs.mpgc.rpg126036.model.Player;
import it.unicam.cs.mpgc.rpg126036.model.Scene;
import it.unicam.cs.mpgc.rpg126036.model.StatType;
import it.unicam.cs.mpgc.rpg126036.model.Transition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Motore del ciclo di gioco. Coordina l'avanzamento tra scene e capitoli e
 * l'esecuzione delle interazioni sullo {@link GameState}, notificando gli eventi
 * ai {@link GameListener} registrati (pattern Observer).
 *
 * <p>Non gestisce l'input/output: espone le operazioni che il controller invoca
 * nel proprio loop (mostra stato &rarr; raccoglie la scelta &rarr; esegue &rarr;
 * aggiorna). Le interazioni applicano da sole il consumo di energia e gli XP; il
 * motore ne restituisce l'esito e ne notifica gli eventi.</p>
 */
public class GameEngine {

    private final GameState stato;
    private final List<Chapter> capitoli;
    private final List<GameListener> listeners = new ArrayList<>();

    private int indiceCapitolo;
    private boolean gameOverNotificato;
    private boolean inPausa;

    /**
     * @param stato    lo stato della partita (non nullo)
     * @param capitoli la sequenza ordinata dei capitoli della campagna (non nulla, non vuota)
     */
    public GameEngine(GameState stato, List<Chapter> capitoli) {
        this.stato = Objects.requireNonNull(stato, "Lo stato non puo' essere nullo.");
        Objects.requireNonNull(capitoli, "I capitoli non possono essere nulli.");
        if (capitoli.isEmpty()) {
            throw new IllegalArgumentException("La campagna deve avere almeno un capitolo.");
        }
        this.capitoli = List.copyOf(capitoli);
        this.indiceCapitolo = 0;
        this.stato.setCapitoloCorrente(this.capitoli.get(0));
    }

    // ----------------------------------------------------------------------
    // Registrazione osservatori
    // ----------------------------------------------------------------------

    /**
     * Registra un osservatore degli eventi di gioco.
     *
     * @param listener l'osservatore (non nullo)
     */
    public void addListener(GameListener listener) {
        listeners.add(Objects.requireNonNull(listener, "Il listener non puo' essere nullo."));
    }

    /**
     * Rimuove un osservatore precedentemente registrato.
     *
     * @param listener l'osservatore da rimuovere
     * @return {@code true} se era registrato
     */
    public boolean removeListener(GameListener listener) {
        return listeners.remove(listener);
    }

    // ----------------------------------------------------------------------
    // Stato corrente
    // ----------------------------------------------------------------------

    public GameState getStato() {
        return stato;
    }

    public Chapter getCapitoloCorrente() {
        return capitoli.get(indiceCapitolo);
    }

    public Scene getScenaCorrente() {
        return getCapitoloCorrente().getScenaCorrente();
    }

    public List<Transition> transizioniDisponibili() {
        return getScenaCorrente().getTransizioni();
    }

    public int getEnergia() {
        return stato.getPlayer().getEnergia();
    }

    public boolean isEnergiaEsaurita() {
        return stato.getPlayer().getEnergia() <= 0;
    }

    /**
     * @return {@code true} se la partita e' conclusa: ultimo capitolo completato
     */
    public boolean isPartitaTerminata() {
        return indiceCapitolo == capitoli.size() - 1 && getCapitoloCorrente().isCompletato();
    }

    // ----------------------------------------------------------------------
    // Pausa
    // ----------------------------------------------------------------------

    /**
     * Mette il gioco in pausa: finche' e' in pausa le interazioni e gli
     * avanzamenti sono bloccati. Notifica {@link GameListener#onPaused()}.
     */
    public void pausa() {
        if (!inPausa) {
            inPausa = true;
            listeners.forEach(GameListener::onPaused);
        }
    }

    /**
     * Riprende il gioco dalla pausa. Notifica {@link GameListener#onResumed()}.
     */
    public void riprendi() {
        if (inPausa) {
            inPausa = false;
            listeners.forEach(GameListener::onResumed);
        }
    }

    /**
     * @return {@code true} se il gioco e' attualmente in pausa
     */
    public boolean isInPausa() {
        return inPausa;
    }

    // ----------------------------------------------------------------------
    // Ciclo di gioco
    // ----------------------------------------------------------------------

    /**
     * Esegue un'interazione applicandone gli effetti al giocatore e notifica
     * l'evento {@link GameListener#onInteractionExecuted(InteractionResult)}.
     *
     * @param interazione l'interazione da eseguire (non nulla)
     * @return l'esito dell'interazione
     */
    public InteractionResult esegui(Interaction interazione) {
        Objects.requireNonNull(interazione, "L'interazione non puo' essere nulla.");
        if (inPausa) {
            return new InteractionResult(false, "Gioco in pausa.", 0, 0);
        }
        InteractionResult risultato = interazione.esegui(stato.getPlayer());
        listeners.forEach(l -> l.onInteractionExecuted(risultato));
        verificaGameOver();
        return risultato;
    }

    /**
     * Raccoglie un oggetto dalla scena, aggiornando l'inventario e notificando
     * {@link GameListener#onItemFound(Item)} se la raccolta ha successo.
     *
     * @param oggetto l'oggetto interagibile presente nella scena (non nullo)
     * @return l'esito della raccolta
     */
    public InteractionResult raccogli(ItemInteraction oggetto) {
        Objects.requireNonNull(oggetto, "L'oggetto non puo' essere nullo.");
        if (inPausa) {
            return new InteractionResult(false, "Gioco in pausa.", 0, 0);
        }
        boolean eraPresente = oggetto.isPresente();
        InteractionResult risultato = oggetto.raccogli(stato.getPlayer());
        if (eraPresente && risultato.successo()) {
            Item item = oggetto.getItem();
            listeners.forEach(l -> l.onItemFound(item));
        }
        verificaGameOver();
        return risultato;
    }

    /**
     * Avanza verso la scena destinazione seguendo un arco della scena corrente.
     * Notifica il cambio di scena e, se il capitolo si conclude, l'eventuale
     * avanzamento di capitolo e la fine partita.
     *
     * @param idScenaDestinazione id della scena verso cui spostarsi
     * @return {@code true} se l'avanzamento e' avvenuto
     */
    public boolean avanza(String idScenaDestinazione) {
        if (inPausa) {
            return false;
        }
        Chapter capitolo = getCapitoloCorrente();
        if (!capitolo.vaiA(idScenaDestinazione)) {
            return false;
        }
        notificaSceneChanged();
        // Il passaggio al capitolo successivo non e' automatico: avviene tramite
        // concludiCapitolo(...), che intercala ripristino energia e upgrade.
        verificaFinePartita();
        return true;
    }

    /**
     * Esegue la sequenza di fine capitolo quando il capitolo corrente e'
     * completato ed esiste un capitolo successivo: ripristina l'energia al
     * massimo, applica l'upgrade gratuito della statistica scelta, avanza al
     * capitolo successivo e notifica {@link GameListener#onChapterCompleted(Chapter)}.
     *
     * @param statDaPotenziare la statistica scelta dal giocatore per il +1 (non nulla)
     * @return {@code true} se la sequenza e' stata eseguita, {@code false} se il
     *         capitolo non e' completato o e' l'ultimo della campagna
     */
    public boolean concludiCapitolo(StatType statDaPotenziare) {
        Objects.requireNonNull(statDaPotenziare, "La statistica non puo' essere nulla.");
        if (inPausa) {
            return false;
        }
        Chapter concluso = getCapitoloCorrente();
        if (!concluso.isCompletato() || indiceCapitolo >= capitoli.size() - 1) {
            return false;
        }
        // 2. ripristino energia al massimo (riposo tra i capitoli)
        stato.getPlayer().ripristinaEnergia(Player.ENERGIA_MASSIMA);
        // 3. upgrade gratuito della statistica scelta
        stato.getPlayer().aumentaStatistica(statDaPotenziare, 1);
        // 4. avanzamento al capitolo successivo
        avanzaCapitolo();
        // 5. evento di fine capitolo
        listeners.forEach(l -> l.onChapterCompleted(concluso));
        return true;
    }

    /**
     * Passa al capitolo successivo, se il corrente e' completato e ne esiste uno,
     * notificando l'avanzamento di capitolo e il relativo cambio di scena.
     *
     * @return {@code true} se il passaggio e' avvenuto
     */
    public boolean avanzaCapitolo() {
        if (indiceCapitolo < capitoli.size() - 1 && getCapitoloCorrente().isCompletato()) {
            indiceCapitolo++;
            Chapter nuovo = capitoli.get(indiceCapitolo);
            stato.setCapitoloCorrente(nuovo);
            listeners.forEach(l -> l.onChapterAdvanced(nuovo));
            notificaSceneChanged();
            verificaFinePartita();
            return true;
        }
        return false;
    }

    private void notificaSceneChanged() {
        Scene scena = getScenaCorrente();
        listeners.forEach(l -> l.onSceneChanged(scena));
    }

    private void verificaFinePartita() {
        if (!gameOverNotificato && isPartitaTerminata()) {
            gameOverNotificato = true;
            listeners.forEach(GameListener::onGameOver);
        }
    }

    /**
     * Controlla la condizione di sconfitta per energia esaurita e, se ricorre,
     * notifica {@link GameListener#onGameOver()} (una sola volta). Va invocato
     * dopo le azioni che consumano energia esterne al motore (ad es. la forza
     * bruta di un enigma); le interazioni eseguite dal motore lo fanno gia'.
     *
     * @return {@code true} se ha appena segnalato il game over per energia
     */
    public boolean verificaGameOver() {
        if (!gameOverNotificato && isEnergiaEsaurita()) {
            gameOverNotificato = true;
            listeners.forEach(GameListener::onGameOver);
            return true;
        }
        return false;
    }

    /**
     * @return {@code true} se la partita e' persa per energia esaurita
     */
    public boolean isSconfitta() {
        return isEnergiaEsaurita();
    }
}
