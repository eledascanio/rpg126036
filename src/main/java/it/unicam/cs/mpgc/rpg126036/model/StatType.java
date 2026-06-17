package it.unicam.cs.mpgc.rpg126036.model;

/**
 * Tipologie di statistica che caratterizzano un personaggio nel gioco investigativo.
 */
public enum StatType {

    /** Capacità di raccogliere indizi ed esaminare le scene. */
    INVESTIGAZIONE("Investigazione", "🔍"),

    /** Capacità di persuadere e interagire con gli altri personaggi. */
    CARISMA("Carisma", "💬"),

    /** Capacità di cogliere dettagli nascosti e anticipare le situazioni. */
    INTUIZIONE("Intuizione", "💡");

    private final String nomeVisualizzato;
    private final String icona;

    StatType(String nomeVisualizzato, String icona) {
        this.nomeVisualizzato = nomeVisualizzato;
        this.icona = icona;
    }

    /**
     * @return il nome della statistica adatto alla visualizzazione.
     */
    public String getNomeVisualizzato() {
        return nomeVisualizzato;
    }

    /**
     * @return l'emoji che rappresenta la statistica nell'interfaccia.
     */
    public String getIcona() {
        return icona;
    }
}
