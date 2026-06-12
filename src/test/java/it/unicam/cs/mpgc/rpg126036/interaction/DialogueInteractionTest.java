package it.unicam.cs.mpgc.rpg126036.interaction;

import it.unicam.cs.mpgc.rpg126036.model.CharacterClass;
import it.unicam.cs.mpgc.rpg126036.model.Dialogue;
import it.unicam.cs.mpgc.rpg126036.model.Npc;
import it.unicam.cs.mpgc.rpg126036.model.Player;
import it.unicam.cs.mpgc.rpg126036.model.StatRequirement;
import it.unicam.cs.mpgc.rpg126036.model.StatType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica il gating delle scelte e la selezione in un {@link DialogueInteraction}.
 */
class DialogueInteractionTest {

    private final Npc npc = new Npc("npc", "Studente", true, Dialogue.semplice("Ehi"));

    private final DialogueChoice chiediDettagli = new DialogueChoice(
            "dettagli", "Chiedi dettagli",
            new SimpleInteraction("esito", "esito", true, "Ottieni un indizio", 0, 20));

    private final DialogueChoice viaGated = new DialogueChoice(
            "gated", "Opzione speciale", new StatRequirement(StatType.CARISMA, 2),
            new SimpleInteraction("esito2", "esito2", true, "Speciale", 0, 0));

    private Player conCarisma(int valore) {
        Player player = new Player("Eroe", CharacterClass.STUDENTE_MODELLO);
        player.aumentaStatistica(StatType.CARISMA, valore);
        return player;
    }

    private DialogueInteraction dialogo() {
        return new DialogueInteraction(npc, "Cosa vuoi sapere?", List.of(chiediDettagli, viaGated));
    }

    @Test
    void leScelteGatedSonoNascosteSottoSoglia() {
        assertEquals(1, dialogo().scelteDisponibili(conCarisma(0)).size());
        assertEquals(2, dialogo().scelteDisponibili(conCarisma(2)).size());
    }

    @Test
    void scegliereApplicaLEsitoDellaScelta() {
        Player player = conCarisma(0);

        InteractionResult esito = dialogo().scegli(player, chiediDettagli);

        assertTrue(esito.successo());
        assertEquals(20, player.getXp());
    }

    @Test
    void unaSceltaEstraneaAlDialogoVieneRifiutata() {
        DialogueChoice estranea = new DialogueChoice("x", "X",
                new SimpleInteraction("y", "y", true, "y"));

        assertThrows(IllegalArgumentException.class,
                () -> dialogo().scegli(conCarisma(0), estranea));
    }

    @Test
    void selezionareUnaScelaGatedNonDisponibileNonNeApplicaLEsito() {
        Player player = conCarisma(0);

        InteractionResult esito = viaGated.esegui(player);

        assertEquals(0, player.getXp());
        assertTrue(esito.messaggio().toLowerCase().contains("non"));
    }
}
