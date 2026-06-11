package it.unicam.cs.mpgc.rpg126036.model;

import java.util.Objects;
import java.util.Optional;

/**
 * Dati di dialogo di un {@link Npc}. Un dialogo puo' essere:
 * <ul>
 *     <li><b>semplice</b>: una sola battuta, sempre mostrata;</li>
 *     <li><b>condizionato</b>: subordinato a un {@link StatRequirement}. Se il
 *     personaggio soddisfa il requisito viene mostrata la battuta di successo,
 *     altrimenti quella di blocco.</li>
 * </ul>
 */
public class Dialogue {

    private final String testo;
    private final String testoBloccato;
    private final StatRequirement requisito;

    private Dialogue(String testo, String testoBloccato, StatRequirement requisito) {
        this.testo = testo;
        this.testoBloccato = testoBloccato;
        this.requisito = requisito;
    }

    /**
     * Crea un dialogo semplice, sempre accessibile.
     *
     * @param testo la battuta dell'NPC (non nulla)
     */
    public static Dialogue semplice(String testo) {
        return new Dialogue(Objects.requireNonNull(testo, "Il testo non puo' essere nullo."), null, null);
    }

    /**
     * Crea un dialogo condizionato da un requisito di statistica.
     *
     * @param requisito     il requisito da soddisfare (non nullo)
     * @param testoSuccesso la battuta mostrata se il requisito e' soddisfatto (non nulla)
     * @param testoBloccato la battuta mostrata se il requisito non e' soddisfatto (non nulla)
     */
    public static Dialogue condizionato(StatRequirement requisito, String testoSuccesso, String testoBloccato) {
        Objects.requireNonNull(requisito, "Il requisito non puo' essere nullo.");
        Objects.requireNonNull(testoSuccesso, "Il testo di successo non puo' essere nullo.");
        Objects.requireNonNull(testoBloccato, "Il testo di blocco non puo' essere nullo.");
        return new Dialogue(testoSuccesso, testoBloccato, requisito);
    }

    /**
     * @return {@code true} se il dialogo dipende da un requisito di statistica
     */
    public boolean isCondizionato() {
        return requisito != null;
    }

    /**
     * @param player il personaggio che interagisce (non nullo)
     * @return {@code true} se la battuta di successo e' accessibile al personaggio
     */
    public boolean isAccessibile(Player player) {
        return requisito == null || requisito.isSoddisfatto(player);
    }

    /**
     * Restituisce la battuta appropriata in base allo stato del personaggio.
     *
     * @param player il personaggio che interagisce (non nullo)
     * @return la battuta di successo se accessibile, altrimenti quella di blocco
     */
    public String parlaCon(Player player) {
        return isAccessibile(player) ? testo : testoBloccato;
    }

    /**
     * @return l'eventuale requisito associato al dialogo
     */
    public Optional<StatRequirement> getRequisito() {
        return Optional.ofNullable(requisito);
    }

    /**
     * @return la battuta di successo (o l'unica battuta, per i dialoghi semplici)
     */
    public String getTesto() {
        return testo;
    }
}
