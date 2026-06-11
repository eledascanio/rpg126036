package it.unicam.cs.mpgc.rpg126036.interaction;

import it.unicam.cs.mpgc.rpg126036.model.Player;
import it.unicam.cs.mpgc.rpg126036.model.StatRequirement;

import java.util.Objects;
import java.util.Optional;

/**
 * Singola scelta di un {@link DialogueInteraction}. La scelta puo' essere
 * <b>gated</b> da un requisito di statistica: in tal caso e' disponibile solo se
 * il giocatore lo soddisfa. L'esito della scelta e' delegato a un'altra
 * {@link Interaction}, cosi' da poter essere fisso (es. {@link SimpleInteraction})
 * oppure condizionato dalle statistiche (es. {@code StatCheckInteraction}).
 */
public class DialogueChoice implements Interaction {

    private final String id;
    private final String etichetta;
    private final StatRequirement requisito;
    private final Interaction esito;

    /**
     * Crea una scelta sempre disponibile.
     *
     * @param id        identificativo della scelta
     * @param etichetta testo dell'opzione mostrata al giocatore
     * @param esito     interazione eseguita quando la scelta viene selezionata
     */
    public DialogueChoice(String id, String etichetta, Interaction esito) {
        this(id, etichetta, null, esito);
    }

    /**
     * Crea una scelta eventualmente gated da un requisito di statistica.
     *
     * @param id        identificativo della scelta
     * @param etichetta testo dell'opzione mostrata al giocatore
     * @param requisito requisito di visibilita' (puo' essere nullo = sempre disponibile)
     * @param esito     interazione eseguita quando la scelta viene selezionata
     */
    public DialogueChoice(String id, String etichetta, StatRequirement requisito, Interaction esito) {
        this.id = Objects.requireNonNull(id, "L'id non puo' essere nullo.");
        this.etichetta = Objects.requireNonNull(etichetta, "L'etichetta non puo' essere nulla.");
        this.esito = Objects.requireNonNull(esito, "L'esito non puo' essere nullo.");
        this.requisito = requisito;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDescrizione() {
        return etichetta;
    }

    /**
     * @return l'eventuale requisito di statistica che abilita la scelta
     */
    public Optional<StatRequirement> getRequisito() {
        return Optional.ofNullable(requisito);
    }

    /**
     * Una scelta gated e' disponibile solo se il giocatore soddisfa il requisito.
     */
    @Override
    public boolean isDisponibile(Player giocatore) {
        Objects.requireNonNull(giocatore, "Il giocatore non puo' essere nullo.");
        return requisito == null || requisito.isSoddisfatto(giocatore);
    }

    @Override
    public InteractionResult esegui(Player giocatore) {
        Objects.requireNonNull(giocatore, "Il giocatore non puo' essere nullo.");
        if (!isDisponibile(giocatore)) {
            return InteractionResult.di(false, "Questa scelta non è disponibile.");
        }
        return esito.esegui(giocatore);
    }
}
