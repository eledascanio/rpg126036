package it.unicam.cs.mpgc.rpg126036.interaction;

import it.unicam.cs.mpgc.rpg126036.model.Player;
import it.unicam.cs.mpgc.rpg126036.model.StatRequirement;
import it.unicam.cs.mpgc.rpg126036.model.StatType;

import java.util.Objects;

/**
 * Interazione basata su un check di statistica: verifica un {@link StatRequirement}
 * sul giocatore e ramifica l'esito tra successo e fallimento, ciascuno con il
 * proprio testo ed eventuali effetti su energia e XP.
 *
 * <p>Costituisce il meccanismo generico dei check sulle statistiche: ad esempio
 * il dialogo del tecnico di laboratorio (Carisma &ge; 2) o l'accesso agevolato a
 * un percorso di un enigma.</p>
 */
public class StatCheckInteraction implements Interaction {

    private final String id;
    private final String descrizione;
    private final StatRequirement requisito;

    private final String testoSuccesso;
    private final int energiaSuccesso;
    private final int xpSuccesso;

    private final String testoFallimento;
    private final int energiaFallimento;
    private final int xpFallimento;

    /**
     * Crea un check di statistica senza effetti su energia o XP.
     *
     * @param id              identificativo dell'interazione
     * @param descrizione     testo di presentazione
     * @param requisito       il check da verificare
     * @param testoSuccesso   testo mostrato se il check riesce
     * @param testoFallimento testo mostrato se il check fallisce
     */
    public StatCheckInteraction(String id, String descrizione, StatRequirement requisito,
                                String testoSuccesso, String testoFallimento) {
        this(id, descrizione, requisito, testoSuccesso, 0, 0, testoFallimento, 0, 0);
    }

    /**
     * Crea un check di statistica con effetti su energia e XP per ciascun ramo.
     *
     * @param id                identificativo dell'interazione
     * @param descrizione       testo di presentazione
     * @param requisito         il check da verificare
     * @param testoSuccesso     testo mostrato se il check riesce
     * @param energiaSuccesso   energia consumata in caso di successo (>= 0)
     * @param xpSuccesso        XP guadagnati in caso di successo (>= 0)
     * @param testoFallimento   testo mostrato se il check fallisce
     * @param energiaFallimento energia consumata in caso di fallimento (>= 0)
     * @param xpFallimento      XP guadagnati in caso di fallimento (>= 0)
     */
    public StatCheckInteraction(String id, String descrizione, StatRequirement requisito,
                                String testoSuccesso, int energiaSuccesso, int xpSuccesso,
                                String testoFallimento, int energiaFallimento, int xpFallimento) {
        this.id = Objects.requireNonNull(id, "L'id non puo' essere nullo.");
        this.descrizione = Objects.requireNonNull(descrizione, "La descrizione non puo' essere nulla.");
        this.requisito = Objects.requireNonNull(requisito, "Il requisito non puo' essere nullo.");
        this.testoSuccesso = Objects.requireNonNull(testoSuccesso, "Il testo di successo non puo' essere nullo.");
        this.testoFallimento = Objects.requireNonNull(testoFallimento, "Il testo di fallimento non puo' essere nullo.");
        this.energiaSuccesso = nonNegativo(energiaSuccesso, "energiaSuccesso");
        this.xpSuccesso = nonNegativo(xpSuccesso, "xpSuccesso");
        this.energiaFallimento = nonNegativo(energiaFallimento, "energiaFallimento");
        this.xpFallimento = nonNegativo(xpFallimento, "xpFallimento");
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDescrizione() {
        return descrizione;
    }

    /**
     * @return la statistica verificata dal check
     */
    public StatType getStatistica() {
        return requisito.tipo();
    }

    /**
     * @return la soglia richiesta dal check
     */
    public int getSoglia() {
        return requisito.valoreMinimo();
    }

    /**
     * Esegue il check: se il giocatore soddisfa il requisito applica gli effetti
     * del ramo di successo, altrimenti quelli del ramo di fallimento.
     */
    @Override
    public InteractionResult esegui(Player giocatore) {
        Objects.requireNonNull(giocatore, "Il giocatore non puo' essere nullo.");
        if (requisito.isSoddisfatto(giocatore)) {
            giocatore.riduciEnergia(energiaSuccesso);
            giocatore.aggiungiXp(xpSuccesso);
            return new InteractionResult(true, testoSuccesso, energiaSuccesso, xpSuccesso);
        }
        giocatore.riduciEnergia(energiaFallimento);
        giocatore.aggiungiXp(xpFallimento);
        return new InteractionResult(false, testoFallimento, energiaFallimento, xpFallimento);
    }

    private static int nonNegativo(int valore, String nome) {
        if (valore < 0) {
            throw new IllegalArgumentException("Il valore '" + nome + "' non puo' essere negativo.");
        }
        return valore;
    }
}
