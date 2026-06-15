package it.unicam.cs.mpgc.rpg126036.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Capitolo del gioco modellato come grafo di navigazione di {@link Scene}.
 *
 * <p>Mantiene il riferimento alla scena iniziale e alla scena corrente: il
 * progresso del capitolo e' dato dalla scena corrente e dai flag di
 * completamento delle singole scene. La navigazione ({@link #vaiA(String)})
 * segue esclusivamente gli archi definiti dalle scene.</p>
 */
public class Chapter {

    private final String id;
    private final String titolo;
    private final Map<String, Scene> scene = new LinkedHashMap<>();

    private String idScenaIniziale;
    private String idScenaCorrente;

    /**
     * @param id     identificativo univoco del capitolo (non nullo)
     * @param titolo titolo del capitolo (non nullo)
     */
    public Chapter(String id, String titolo) {
        this.id = Objects.requireNonNull(id, "L'id non puo' essere nullo.");
        this.titolo = Objects.requireNonNull(titolo, "Il titolo non puo' essere nullo.");
    }

    /**
     * Aggiunge una scena al grafo. La prima scena aggiunta diventa, per
     * default, la scena iniziale (e quella corrente).
     *
     * @param scena la scena da aggiungere (non nulla)
     */
    public void aggiungiScena(Scene scena) {
        Objects.requireNonNull(scena, "La scena non puo' essere nulla.");
        scene.put(scena.getId(), scena);
        if (idScenaIniziale == null) {
            idScenaIniziale = scena.getId();
            idScenaCorrente = scena.getId();
        }
    }

    /**
     * Imposta esplicitamente la scena iniziale (e la rende corrente).
     *
     * @param idScena id di una scena gia' presente nel grafo
     * @throws IllegalArgumentException se la scena non esiste
     */
    public void setScenaIniziale(String idScena) {
        if (!scene.containsKey(idScena)) {
            throw new IllegalArgumentException("Scena inesistente: " + idScena);
        }
        idScenaIniziale = idScena;
        idScenaCorrente = idScena;
    }

    /**
     * @return la scena corrente
     * @throws IllegalStateException se il capitolo non ha ancora scene
     */
    public Scene getScenaCorrente() {
        if (idScenaCorrente == null) {
            throw new IllegalStateException("Il capitolo non ha scene.");
        }
        return scene.get(idScenaCorrente);
    }

    /**
     * Naviga verso la scena destinazione, se la scena corrente possiede un arco
     * che la raggiunge. La scena lasciata viene marcata come completata.
     *
     * @param idDestinazione id della scena verso cui spostarsi
     * @return {@code true} se la transizione e' avvenuta, {@code false} se non
     *         esiste un arco valido verso quella destinazione
     */
    public boolean vaiA(String idDestinazione) {
        Scene corrente = getScenaCorrente();
        boolean arcoValido = corrente.getTransizioni().stream()
                .anyMatch(t -> t.idDestinazione().equals(idDestinazione));
        if (!arcoValido || !scene.containsKey(idDestinazione)) {
            return false;
        }
        corrente.segnaCompletata();
        idScenaCorrente = idDestinazione;
        return true;
    }

    /**
     * @param idScena id da cercare
     * @return la scena corrispondente, se presente
     */
    public Optional<Scene> getScena(String idScena) {
        return Optional.ofNullable(scene.get(idScena));
    }

    /**
     * @return tutte le scene del capitolo, in sola lettura
     */
    public Collection<Scene> getScene() {
        return Collections.unmodifiableCollection(scene.values());
    }

    /**
     * @return {@code true} se il capitolo e' concluso, ossia la scena corrente
     *         e' terminale (priva di archi uscenti)
     */
    public boolean isCompletato() {
        return idScenaCorrente != null && getScenaCorrente().isTerminale();
    }

    /**
     * Riporta il capitolo allo stato iniziale: la scena corrente torna a essere
     * quella iniziale e tutte le scene vengono segnate come non completate. Serve a
     * iniziare una nuova partita riusando lo stesso grafo di capitoli, senza ereditare
     * il progresso della partita precedente.
     */
    public void reset() {
        idScenaCorrente = idScenaIniziale;
        scene.values().forEach(Scene::reset);
    }

    public String getId() {
        return id;
    }

    public String getTitolo() {
        return titolo;
    }

    public Optional<String> getIdScenaIniziale() {
        return Optional.ofNullable(idScenaIniziale);
    }
}
