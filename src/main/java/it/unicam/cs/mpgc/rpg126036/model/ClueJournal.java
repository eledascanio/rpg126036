package it.unicam.cs.mpgc.rpg126036.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Diario degli indizi del personaggio: raccoglie i {@link Clue} scoperti,
 * indicizzati per id e mantenuti nell'ordine di scoperta.
 *
 * <p>E' l'analogo dell' {@link Inventory} per le informazioni: tiene separati i
 * "flag di conoscenza" dagli oggetti fisici raccoglibili.</p>
 */
public class ClueJournal {

    private final Map<String, Clue> indizi = new LinkedHashMap<>();

    /**
     * Registra un indizio nel diario, se non gia' presente.
     *
     * @param indizio l'indizio da aggiungere (non nullo)
     * @return {@code true} se aggiunto, {@code false} se un indizio con lo stesso id era gia' noto
     */
    public boolean aggiungi(Clue indizio) {
        Objects.requireNonNull(indizio, "L'indizio non puo' essere nullo.");
        if (indizi.containsKey(indizio.id())) {
            return false;
        }
        indizi.put(indizio.id(), indizio);
        return true;
    }

    /**
     * @param idIndizio id da cercare
     * @return {@code true} se il diario contiene un indizio con quell'id
     */
    public boolean contiene(String idIndizio) {
        return indizi.containsKey(idIndizio);
    }

    /**
     * @param idIndizio id da cercare
     * @return l'indizio corrispondente, se presente
     */
    public Optional<Clue> get(String idIndizio) {
        return Optional.ofNullable(indizi.get(idIndizio));
    }

    /**
     * @return {@code true} se il diario e' vuoto
     */
    public boolean isEmpty() {
        return indizi.isEmpty();
    }

    /**
     * @return il numero di indizi noti
     */
    public int size() {
        return indizi.size();
    }

    /**
     * @return la lista, in sola lettura, degli indizi nell'ordine di scoperta
     */
    public List<Clue> getIndizi() {
        return List.copyOf(indizi.values());
    }
}
