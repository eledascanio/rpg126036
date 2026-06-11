package it.unicam.cs.mpgc.rpg126036.interaction;

import it.unicam.cs.mpgc.rpg126036.model.Item;
import it.unicam.cs.mpgc.rpg126036.model.Player;

import java.util.Objects;

/**
 * Interazione con un oggetto presente nel mondo di gioco. Consente di
 * <b>esaminare</b> l'oggetto (mostrarne la descrizione, senza effetti) e di
 * <b>raccoglierlo</b>, aggiungendolo all'inventario del giocatore.
 *
 * <p>Una volta raccolto l'oggetto non e' piu' presente nella scena
 * ({@link #isPresente()} diventa {@code false}): modella la chiave del capitolo 1
 * che, raccolta da terra, sparisce.</p>
 */
public class ItemInteraction {

    private final Item item;
    private boolean raccolto;

    /**
     * @param item l'oggetto presente nella scena (non nullo)
     */
    public ItemInteraction(Item item) {
        this.item = Objects.requireNonNull(item, "L'oggetto non puo' essere nullo.");
    }

    /**
     * @return l'oggetto associato a questa interazione
     */
    public Item getItem() {
        return item;
    }

    /**
     * @return {@code true} se l'oggetto e' ancora presente nella scena (non raccolto)
     */
    public boolean isPresente() {
        return !raccolto;
    }

    /**
     * Esamina l'oggetto mostrandone la descrizione, senza raccoglierlo.
     *
     * @return l'esito con il testo descrittivo, oppure un esito negativo se
     *         l'oggetto e' gia' stato raccolto
     */
    public InteractionResult esamina() {
        if (raccolto) {
            return InteractionResult.di(false, "Non c'è più nulla da esaminare qui.");
        }
        return InteractionResult.di(true, item.descrizione());
    }

    /**
     * Raccoglie l'oggetto aggiungendolo all'inventario del giocatore. Dopo la
     * raccolta l'oggetto sparisce dalla scena.
     *
     * @param giocatore il giocatore che raccoglie (non nullo)
     * @return l'esito della raccolta
     */
    public InteractionResult raccogli(Player giocatore) {
        Objects.requireNonNull(giocatore, "Il giocatore non puo' essere nullo.");
        if (raccolto) {
            return InteractionResult.di(false, "Hai già raccolto " + item.nome() + ".");
        }
        boolean aggiunto = giocatore.raccogli(item);
        // L'oggetto sparisce comunque dalla scena, anche se era gia' nell'inventario.
        raccolto = true;
        if (!aggiunto) {
            return InteractionResult.di(true, item.nome() + " era già nel tuo inventario.");
        }
        return InteractionResult.di(true, "Hai raccolto: " + item.nome() + ".");
    }
}
