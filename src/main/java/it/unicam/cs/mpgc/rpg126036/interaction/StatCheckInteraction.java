package it.unicam.cs.mpgc.rpg126036.interaction;

import it.unicam.cs.mpgc.rpg126036.model.Player;
import it.unicam.cs.mpgc.rpg126036.model.StatRequirement;
import it.unicam.cs.mpgc.rpg126036.model.StatType;

import java.util.Objects;

/**
 * Interazione basata su un check di statistica: verifica un {@link StatRequirement}
 * sul giocatore e ramifica l'esito tra successo e fallimento, ciascuno con il
 * proprio {@link EsitoCheck} (testo ed effetti su energia e XP).
 *
 * <p>Costituisce il meccanismo generico dei check sulle statistiche: ad esempio
 * il dialogo del tecnico di laboratorio (Carisma &ge; 2) o l'accesso agevolato a
 * un percorso di un enigma.</p>
 */
public class StatCheckInteraction implements Interaction {

    private final String id;
    private final String descrizione;
    private final StatRequirement requisito;
    private final EsitoCheck esitoSuccesso;
    private final EsitoCheck esitoFallimento;

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
        this(id, descrizione, requisito,
                new EsitoCheck(testoSuccesso, 0, 0), new EsitoCheck(testoFallimento, 0, 0));
    }

    /**
     * Crea un check di statistica con gli esiti completi dei due rami.
     *
     * @param id              identificativo dell'interazione
     * @param descrizione     testo di presentazione
     * @param requisito       il check da verificare
     * @param esitoSuccesso   esito applicato se il check riesce (non nullo)
     * @param esitoFallimento esito applicato se il check fallisce (non nullo)
     */
    public StatCheckInteraction(String id, String descrizione, StatRequirement requisito,
                                EsitoCheck esitoSuccesso, EsitoCheck esitoFallimento) {
        this.id = Objects.requireNonNull(id, "L'id non puo' essere nullo.");
        this.descrizione = Objects.requireNonNull(descrizione, "La descrizione non puo' essere nulla.");
        this.requisito = Objects.requireNonNull(requisito, "Il requisito non puo' essere nullo.");
        this.esitoSuccesso = Objects.requireNonNull(esitoSuccesso, "L'esito di successo non puo' essere nullo.");
        this.esitoFallimento = Objects.requireNonNull(esitoFallimento, "L'esito di fallimento non puo' essere nullo.");
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
     * Esegue il check: se il giocatore soddisfa il requisito applica l'esito del
     * ramo di successo, altrimenti quello del ramo di fallimento.
     */
    @Override
    public InteractionResult esegui(Player giocatore) {
        Objects.requireNonNull(giocatore, "Il giocatore non puo' essere nullo.");
        boolean soddisfatto = requisito.isSoddisfatto(giocatore);
        EsitoCheck esito = soddisfatto ? esitoSuccesso : esitoFallimento;
        giocatore.riduciEnergia(esito.energia());
        giocatore.aggiungiXp(esito.xp());
        return new InteractionResult(soddisfatto, esito.testo(), esito.energia(), esito.xp());
    }
}
