package it.unicam.cs.mpgc.rpg126036.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Inventario del personaggio: raccoglie gli {@link Item} ottenuti, indicizzati
 * per id e mantenuti nell'ordine di raccolta.
 *
 * <p>Nella versione demo l'inventario resta vuoto e non viene mostrato
 * all'utente: la struttura e' predisposta per sviluppi futuri.</p>
 */
public class Inventory {

    private final Map<String, Item> oggetti = new LinkedHashMap<>();

    /**
     * Aggiunge un oggetto all'inventario, se non gia' presente.
     *
     * @param item l'oggetto da aggiungere (non nullo)
     * @return {@code true} se aggiunto, {@code false} se un oggetto con lo stesso id era gia' presente
     */
    public boolean aggiungi(Item item) {
        Objects.requireNonNull(item, "L'oggetto non puo' essere nullo.");
        if (oggetti.containsKey(item.id())) {
            return false;
        }
        oggetti.put(item.id(), item);
        return true;
    }

    /**
     * Rimuove l'oggetto con l'id indicato.
     *
     * @param idItem id dell'oggetto da rimuovere
     * @return {@code true} se l'oggetto era presente ed e' stato rimosso
     */
    public boolean rimuovi(String idItem) {
        return oggetti.remove(idItem) != null;
    }

    /**
     * @param idItem id da cercare
     * @return {@code true} se l'inventario contiene un oggetto con quell'id
     */
    public boolean contiene(String idItem) {
        return oggetti.containsKey(idItem);
    }

    /**
     * @param idItem id da cercare
     * @return l'oggetto corrispondente, se presente
     */
    public Optional<Item> get(String idItem) {
        return Optional.ofNullable(oggetti.get(idItem));
    }

    /**
     * @return {@code true} se l'inventario e' vuoto
     */
    public boolean isEmpty() {
        return oggetti.isEmpty();
    }

    /**
     * @return il numero di oggetti contenuti
     */
    public int size() {
        return oggetti.size();
    }

    /**
     * @return la lista, in sola lettura, degli oggetti nell'ordine di raccolta
     */
    public List<Item> getOggetti() {
        return List.copyOf(oggetti.values());
    }
}
