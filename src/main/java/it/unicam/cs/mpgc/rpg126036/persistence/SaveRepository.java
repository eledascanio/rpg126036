package it.unicam.cs.mpgc.rpg126036.persistence;

import it.unicam.cs.mpgc.rpg126036.engine.GameState;

import java.util.List;
import java.util.Optional;

/**
 * Contratto per la persistenza delle partite su un massimo di
 * {@value #MAX_SLOTS} slot. Ogni slot e' identificato dal nome del personaggio.
 */
public interface SaveRepository {

    /** Numero massimo di slot di salvataggio. */
    int MAX_SLOTS = 3;

    /**
     * Salva (o sovrascrive) la partita nello slot corrispondente al nome del
     * personaggio. Se lo slot e' nuovo e gli slot sono gia' pieni, l'operazione
     * fallisce: occorre prima eliminare uno slot.
     *
     * @param stato lo stato della partita da salvare (non nullo)
     * @throws PersistenceException in caso di errore di scrittura o slot pieni
     */
    void save(GameState stato);

    /**
     * @return i metadati degli slot salvati (al massimo {@value #MAX_SLOTS})
     */
    List<SaveSlot> listSlots();

    /**
     * Carica la partita salvata con il nome indicato.
     *
     * @param nomePersonaggio nome del personaggio/slot
     * @return lo stato ripristinato, oppure vuoto se lo slot non esiste
     */
    Optional<GameState> load(String nomePersonaggio);

    /**
     * Elimina lo slot indicato.
     *
     * @param nomePersonaggio nome del personaggio/slot
     * @return {@code true} se lo slot esisteva ed e' stato eliminato
     */
    boolean delete(String nomePersonaggio);

    /**
     * @param nomePersonaggio nome del personaggio/slot
     * @return {@code true} se lo slot esiste
     */
    boolean exists(String nomePersonaggio);

    /**
     * @return {@code true} se tutti gli slot sono occupati
     */
    default boolean isFull() {
        return listSlots().size() >= MAX_SLOTS;
    }
}
