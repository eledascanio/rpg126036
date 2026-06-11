package it.unicam.cs.mpgc.rpg126036.engine;

import it.unicam.cs.mpgc.rpg126036.model.Chapter;
import it.unicam.cs.mpgc.rpg126036.model.Inventory;
import it.unicam.cs.mpgc.rpg126036.model.Player;
import it.unicam.cs.mpgc.rpg126036.model.Scene;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Stato completo di una partita: il giocatore, il capitolo (e quindi la scena)
 * corrente, i flag di progresso e l'inventario.
 *
 * <p>L'inventario non e' un campo a se': appartiene al {@link Player} ed e' qui
 * esposto per comodita'. I flag sono semplici marcatori booleani globali (ad es.
 * "porta_aperta", "ha_parlato_con_assistente") usati per tracciare l'avanzamento.</p>
 */
public class GameState {

    private final Player player;
    private final Set<String> flag = new HashSet<>();

    private Chapter capitoloCorrente;

    /**
     * Crea lo stato di una partita senza capitolo iniziale.
     *
     * @param player il giocatore (non nullo)
     */
    public GameState(Player player) {
        this.player = Objects.requireNonNull(player, "Il giocatore non puo' essere nullo.");
    }

    /**
     * Crea lo stato di una partita con un capitolo iniziale.
     *
     * @param player           il giocatore (non nullo)
     * @param capitoloIniziale il capitolo di partenza (non nullo)
     */
    public GameState(Player player, Chapter capitoloIniziale) {
        this(player);
        this.capitoloCorrente = Objects.requireNonNull(capitoloIniziale, "Il capitolo non puo' essere nullo.");
    }

    /**
     * @return il giocatore
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @return l'inventario del giocatore
     */
    public Inventory getInventario() {
        return player.getInventario();
    }

    /**
     * @return il capitolo corrente, se impostato
     */
    public Optional<Chapter> getCapitoloCorrente() {
        return Optional.ofNullable(capitoloCorrente);
    }

    /**
     * Imposta il capitolo corrente (ad es. all'avanzamento tra capitoli).
     *
     * @param capitolo il nuovo capitolo corrente (non nullo)
     */
    public void setCapitoloCorrente(Chapter capitolo) {
        this.capitoloCorrente = Objects.requireNonNull(capitolo, "Il capitolo non puo' essere nullo.");
    }

    /**
     * @return la scena corrente del capitolo corrente, se disponibile
     */
    public Optional<Scene> getScenaCorrente() {
        if (capitoloCorrente == null || capitoloCorrente.getScene().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(capitoloCorrente.getScenaCorrente());
    }

    // ----------------------------------------------------------------------
    // Flag di progresso
    // ----------------------------------------------------------------------

    /**
     * Attiva un flag di progresso.
     *
     * @param flag il nome del flag (non nullo)
     */
    public void setFlag(String flag) {
        this.flag.add(Objects.requireNonNull(flag, "Il flag non puo' essere nullo."));
    }

    /**
     * Disattiva un flag di progresso.
     *
     * @param flag il nome del flag
     * @return {@code true} se il flag era attivo ed e' stato rimosso
     */
    public boolean rimuoviFlag(String flag) {
        return this.flag.remove(flag);
    }

    /**
     * @param flag il nome del flag
     * @return {@code true} se il flag e' attivo
     */
    public boolean hasFlag(String flag) {
        return this.flag.contains(flag);
    }

    /**
     * @return i flag attivi, in sola lettura
     */
    public Set<String> getFlag() {
        return Collections.unmodifiableSet(flag);
    }
}
