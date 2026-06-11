package it.unicam.cs.mpgc.rpg126036.engine;

import it.unicam.cs.mpgc.rpg126036.interaction.Interaction;
import it.unicam.cs.mpgc.rpg126036.interaction.InteractionResult;
import it.unicam.cs.mpgc.rpg126036.model.Chapter;
import it.unicam.cs.mpgc.rpg126036.model.Scene;
import it.unicam.cs.mpgc.rpg126036.model.Transition;

import java.util.List;
import java.util.Objects;

/**
 * Motore del ciclo di gioco. Coordina l'avanzamento tra scene e capitoli e
 * l'esecuzione delle interazioni sullo {@link GameState}.
 *
 * <p>Non gestisce l'input/output: espone le operazioni che il controller invoca
 * nel proprio loop (mostra stato &rarr; raccoglie la scelta &rarr; esegue &rarr;
 * aggiorna). Le interazioni applicano da sole il consumo di energia e gli XP; il
 * motore ne restituisce l'esito.</p>
 */
public class GameEngine {

    private final GameState stato;
    private final List<Chapter> capitoli;

    private int indiceCapitolo;

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

    /**
     * @return lo stato della partita
     */
    public GameState getStato() {
        return stato;
    }

    /**
     * @return il capitolo attualmente in corso
     */
    public Chapter getCapitoloCorrente() {
        return capitoli.get(indiceCapitolo);
    }

    /**
     * @return la scena corrente del capitolo in corso
     */
    public Scene getScenaCorrente() {
        return getCapitoloCorrente().getScenaCorrente();
    }

    /**
     * @return le transizioni percorribili dalla scena corrente
     */
    public List<Transition> transizioniDisponibili() {
        return getScenaCorrente().getTransizioni();
    }

    /**
     * Esegue un'interazione applicandone gli effetti al giocatore.
     *
     * @param interazione l'interazione da eseguire (non nulla)
     * @return l'esito dell'interazione
     */
    public InteractionResult esegui(Interaction interazione) {
        Objects.requireNonNull(interazione, "L'interazione non puo' essere nulla.");
        return interazione.esegui(stato.getPlayer());
    }

    /**
     * Avanza verso la scena destinazione seguendo un arco della scena corrente.
     * Se l'avanzamento conclude il capitolo, passa automaticamente al capitolo
     * successivo (se presente).
     *
     * @param idScenaDestinazione id della scena verso cui spostarsi
     * @return {@code true} se l'avanzamento e' avvenuto
     */
    public boolean avanza(String idScenaDestinazione) {
        Chapter capitolo = getCapitoloCorrente();
        boolean mosso = capitolo.vaiA(idScenaDestinazione);
        if (mosso && capitolo.isCompletato()) {
            avanzaCapitolo();
        }
        return mosso;
    }

    /**
     * Passa al capitolo successivo, se il corrente e' completato e ne esiste uno.
     *
     * @return {@code true} se il passaggio e' avvenuto
     */
    public boolean avanzaCapitolo() {
        if (indiceCapitolo < capitoli.size() - 1 && getCapitoloCorrente().isCompletato()) {
            indiceCapitolo++;
            stato.setCapitoloCorrente(capitoli.get(indiceCapitolo));
            return true;
        }
        return false;
    }

    /**
     * @return {@code true} se la partita e' conclusa: ultimo capitolo completato
     */
    public boolean isPartitaTerminata() {
        return indiceCapitolo == capitoli.size() - 1 && getCapitoloCorrente().isCompletato();
    }

    /**
     * @return l'energia corrente del giocatore
     */
    public int getEnergia() {
        return stato.getPlayer().getEnergia();
    }

    /**
     * Indica se il giocatore e' rimasto senza energia. Nota: nel design attuale
     * questa condizione non interrompe la partita (lo sblocco di forza bruta
     * evita gli stalli), ma e' esposta per il controller/la vista.
     *
     * @return {@code true} se l'energia e' a zero
     */
    public boolean isEnergiaEsaurita() {
        return stato.getPlayer().getEnergia() <= 0;
    }
}
