package it.unicam.cs.mpgc.rpg126036.model;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica l'enigma del PC della vittima nelle sue diverse vie di risoluzione.
 */
class PcVittimaPuzzleTest {

    /** Generatore deterministico: il mini-gioco usera' il numero 32 + 5 = 37. */
    private static Random rngFisso() {
        return new Random() {
            @Override
            public int nextInt(int bound) {
                return 5;
            }
        };
    }

    private Player giocatore() {
        return new Player("Eroe", CharacterClass.STUDENTE_MODELLO);
    }

    @Test
    void laForzaBrutaSbloccaConsumandoEnergia() {
        PcVittimaPuzzle pc = new PcVittimaPuzzle(false);
        Player player = giocatore();

        PuzzleOutcome esito = pc.forzaBruta(player);

        assertTrue(esito.risolto());
        assertEquals(PcVittimaPuzzle.COSTO_FORZA_BRUTA, esito.energiaConsumata());
        assertEquals(PcVittimaPuzzle.XP, player.getXp());
    }

    @Test
    void laViaIntuizioneTrovaLaPasswordSenzaCosti() {
        PcVittimaPuzzle pc = new PcVittimaPuzzle(false);
        Player player = giocatore();
        player.aumentaStatistica(StatType.INTUIZIONE, PcVittimaPuzzle.SOGLIA_STAT);

        PuzzleOutcome esito = pc.cercaIndizi(player);

        assertTrue(esito.risolto());
        assertEquals(0, esito.energiaConsumata());
        assertEquals(PcVittimaPuzzle.XP, player.getXp());
    }

    @Test
    void laViaCarismaRichiedeAverParlatoConLAssistente() {
        Player player = giocatore();
        player.aumentaStatistica(StatType.CARISMA, PcVittimaPuzzle.SOGLIA_STAT);

        PcVittimaPuzzle senzaDialogo = new PcVittimaPuzzle(false);
        assertFalse(senzaDialogo.usaPasswordAssistente(player).risolto());

        PcVittimaPuzzle conDialogo = new PcVittimaPuzzle(true);
        assertTrue(conDialogo.usaPasswordAssistente(player).risolto());
    }

    @Test
    void laViaInvestigazioneSiRisolveConLaConversioneCorretta() {
        PcVittimaPuzzle pc = new PcVittimaPuzzle(false, rngFisso());
        Player player = giocatore();
        player.aumentaStatistica(StatType.INVESTIGAZIONE, PcVittimaPuzzle.SOGLIA_STAT);

        pc.analizzaTerminale(player);
        PuzzleOutcome esito = pc.tenta(player, "37");

        assertTrue(esito.risolto());
        assertEquals(PcVittimaPuzzle.XP, player.getXp());
    }

    @Test
    void laForzaBrutaESempreTraLeOpzioni() {
        PcVittimaPuzzle pc = new PcVittimaPuzzle(false);
        Player player = giocatore();

        assertTrue(pc.opzioniDisponibili(player).contains("Forza Bruta"));
    }
}
