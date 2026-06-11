package it.unicam.cs.mpgc.rpg126036.interaction;

import it.unicam.cs.mpgc.rpg126036.model.Player;

import java.util.Objects;

/**
 * Interazione dall'esito predeterminato: produce sempre lo stesso
 * {@link InteractionResult}, applicandone gli effetti (energia, XP) al giocatore.
 * Utile come esito "fisso" di una {@link DialogueChoice} che non richiede un check.
 */
public class SimpleInteraction implements Interaction {

    private final String id;
    private final String descrizione;
    private final boolean successo;
    private final String messaggio;
    private final int energiaConsumata;
    private final int xpGuadagnati;

    /**
     * Crea un'interazione con esito fisso senza effetti su energia o XP.
     */
    public SimpleInteraction(String id, String descrizione, boolean successo, String messaggio) {
        this(id, descrizione, successo, messaggio, 0, 0);
    }

    /**
     * Crea un'interazione con esito fisso ed effetti su energia e XP.
     *
     * @param id               identificativo dell'interazione
     * @param descrizione      testo di presentazione
     * @param successo         esito da riportare
     * @param messaggio        testo narrativo dell'esito
     * @param energiaConsumata energia sottratta al giocatore (>= 0)
     * @param xpGuadagnati     XP assegnati al giocatore (>= 0)
     */
    public SimpleInteraction(String id, String descrizione, boolean successo, String messaggio,
                             int energiaConsumata, int xpGuadagnati) {
        this.id = Objects.requireNonNull(id, "L'id non puo' essere nullo.");
        this.descrizione = Objects.requireNonNull(descrizione, "La descrizione non puo' essere nulla.");
        this.messaggio = Objects.requireNonNull(messaggio, "Il messaggio non puo' essere nullo.");
        this.successo = successo;
        if (energiaConsumata < 0) {
            throw new IllegalArgumentException("L'energia consumata non puo' essere negativa.");
        }
        if (xpGuadagnati < 0) {
            throw new IllegalArgumentException("Gli XP guadagnati non possono essere negativi.");
        }
        this.energiaConsumata = energiaConsumata;
        this.xpGuadagnati = xpGuadagnati;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDescrizione() {
        return descrizione;
    }

    @Override
    public InteractionResult esegui(Player giocatore) {
        Objects.requireNonNull(giocatore, "Il giocatore non puo' essere nullo.");
        giocatore.riduciEnergia(energiaConsumata);
        giocatore.aggiungiXp(xpGuadagnati);
        return new InteractionResult(successo, messaggio, energiaConsumata, xpGuadagnati);
    }
}
