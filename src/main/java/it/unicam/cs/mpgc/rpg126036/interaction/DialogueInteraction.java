package it.unicam.cs.mpgc.rpg126036.interaction;

import it.unicam.cs.mpgc.rpg126036.model.Npc;
import it.unicam.cs.mpgc.rpg126036.model.Player;

import java.util.List;
import java.util.Objects;

/**
 * Dialogo con un {@link Npc} strutturato come singolo scambio: l'NPC presenta un
 * testo di apertura, il giocatore sceglie tra le {@link DialogueChoice} disponibili
 * e la conversazione si conclude con l'esito della scelta.
 *
 * <p>Vive nello strato {@code interaction} e riusa l'{@code Npc} del modello senza
 * modificarlo. Il gating delle scelte dipende dalle statistiche del giocatore.</p>
 */
public class DialogueInteraction {

    private final Npc npc;
    private final String testoApertura;
    private final List<DialogueChoice> scelte;

    /**
     * @param npc           l'NPC con cui si dialoga (non nullo)
     * @param testoApertura la battuta iniziale dell'NPC (non nulla)
     * @param scelte        le scelte proposte al giocatore (non nulle, non vuote)
     */
    public DialogueInteraction(Npc npc, String testoApertura, List<DialogueChoice> scelte) {
        this.npc = Objects.requireNonNull(npc, "L'NPC non puo' essere nullo.");
        this.testoApertura = Objects.requireNonNull(testoApertura, "Il testo di apertura non puo' essere nullo.");
        Objects.requireNonNull(scelte, "Le scelte non possono essere nulle.");
        if (scelte.isEmpty()) {
            throw new IllegalArgumentException("Un dialogo deve avere almeno una scelta.");
        }
        this.scelte = List.copyOf(scelte);
    }

    public Npc getNpc() {
        return npc;
    }

    public String getTestoApertura() {
        return testoApertura;
    }

    /**
     * @return tutte le scelte del dialogo, in sola lettura
     */
    public List<DialogueChoice> getScelte() {
        return scelte;
    }

    /**
     * Restituisce le sole scelte disponibili per il giocatore, applicando il
     * gating sui requisiti di statistica.
     *
     * @param giocatore il giocatore (non nullo)
     * @return le scelte selezionabili
     */
    public List<DialogueChoice> scelteDisponibili(Player giocatore) {
        Objects.requireNonNull(giocatore, "Il giocatore non puo' essere nullo.");
        return scelte.stream().filter(scelta -> scelta.isDisponibile(giocatore)).toList();
    }

    /**
     * Seleziona una scelta e ne restituisce l'esito.
     *
     * @param giocatore il giocatore (non nullo)
     * @param scelta    la scelta selezionata (deve appartenere a questo dialogo)
     * @return l'esito della scelta
     * @throws IllegalArgumentException se la scelta non appartiene al dialogo
     */
    public InteractionResult scegli(Player giocatore, DialogueChoice scelta) {
        Objects.requireNonNull(giocatore, "Il giocatore non puo' essere nullo.");
        Objects.requireNonNull(scelta, "La scelta non puo' essere nulla.");
        if (!scelte.contains(scelta)) {
            throw new IllegalArgumentException("La scelta non appartiene a questo dialogo.");
        }
        return scelta.esegui(giocatore);
    }
}
