package it.unicam.cs.mpgc.rpg126036.model;

/**
 * Tipologie di statistica che caratterizzano un personaggio nel gioco investigativo.
 */
public enum StatType {

    /** Capacità di raccogliere indizi ed esaminare le scene. */
    INVESTIGAZIONE("Investigazione"),

    /** Capacità di persuadere e interagire con gli altri personaggi. */
    CARISMA("Carisma"),

    /** Capacità di cogliere dettagli nascosti e anticipare le situazioni. */
    INTUIZIONE("Intuizione");

    private final String nomeVisualizzato;

    StatType(String nomeVisualizzato) {
        this.nomeVisualizzato = nomeVisualizzato;
    }

    /**
     * @return il nome della statistica adatto alla visualizzazione.
     */
    public String getNomeVisualizzato() {
        return nomeVisualizzato;
    }
}
