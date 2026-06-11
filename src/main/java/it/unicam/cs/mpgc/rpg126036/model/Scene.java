package it.unicam.cs.mpgc.rpg126036.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Nodo del grafo di navigazione di un {@link Chapter}. Rappresenta una singola
 * scena con la sua narrazione e gli archi uscenti ({@link Transition}) verso
 * altre scene.
 *
 * <p>Il flag {@code completata} costituisce il dato di progresso della scena.</p>
 */
public class Scene {

    private final String id;
    private final String titolo;
    private final String descrizione;
    private final List<Transition> transizioni = new ArrayList<>();

    private boolean completata;

    /**
     * @param id          identificativo univoco della scena (non nullo)
     * @param titolo      titolo della scena (non nullo)
     * @param descrizione testo narrativo della scena (non nullo)
     */
    public Scene(String id, String titolo, String descrizione) {
        this.id = Objects.requireNonNull(id, "L'id non puo' essere nullo.");
        this.titolo = Objects.requireNonNull(titolo, "Il titolo non puo' essere nullo.");
        this.descrizione = Objects.requireNonNull(descrizione, "La descrizione non puo' essere nulla.");
    }

    /**
     * Aggiunge un arco uscente verso un'altra scena.
     *
     * @param etichetta      testo della scelta (non nullo)
     * @param idDestinazione id della scena destinazione (non nullo)
     */
    public void aggiungiTransizione(String etichetta, String idDestinazione) {
        transizioni.add(new Transition(etichetta, idDestinazione));
    }

    /**
     * @return gli archi uscenti, in sola lettura
     */
    public List<Transition> getTransizioni() {
        return List.copyOf(transizioni);
    }

    /**
     * @return {@code true} se la scena non ha archi uscenti (scena finale)
     */
    public boolean isTerminale() {
        return transizioni.isEmpty();
    }

    /**
     * Marca la scena come completata (dato di progresso).
     */
    public void segnaCompletata() {
        this.completata = true;
    }

    /**
     * @return {@code true} se la scena risulta completata
     */
    public boolean isCompletata() {
        return completata;
    }

    public String getId() {
        return id;
    }

    public String getTitolo() {
        return titolo;
    }

    public String getDescrizione() {
        return descrizione;
    }
}
