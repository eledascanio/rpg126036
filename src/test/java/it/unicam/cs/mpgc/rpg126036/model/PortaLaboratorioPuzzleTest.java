package it.unicam.cs.mpgc.rpg126036.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica l'enigma della porta del laboratorio: soluzione, indizi per statistica
 * e forza bruta.
 */
class PortaLaboratorioPuzzleTest {

    private Player giocatore() {
        return new Player("Eroe", CharacterClass.STUDENTE_MODELLO);
    }

    @Test
    void laCombinazioneCorrettaRisolveEAssegnaXp() {
        PortaLaboratorioPuzzle porta = new PortaLaboratorioPuzzle();
        Player player = giocatore();

        PuzzleOutcome esito = porta.tenta(player, PortaLaboratorioPuzzle.SOLUZIONE);

        assertTrue(esito.risolto());
        assertTrue(porta.isRisolto());
        assertEquals(PortaLaboratorioPuzzle.XP, esito.xpGuadagnati());
        assertEquals(PortaLaboratorioPuzzle.XP, player.getXp());
    }

    @Test
    void unaCombinazioneErrataNonRisolve() {
        PortaLaboratorioPuzzle porta = new PortaLaboratorioPuzzle();
        Player player = giocatore();

        PuzzleOutcome esito = porta.tenta(player, "0000");

        assertFalse(esito.risolto());
        assertFalse(porta.isRisolto());
        assertEquals(0, player.getXp());
    }

    @Test
    void laForzaBrutaApreConsumandoEnergiaMaAssegnaXp() {
        PortaLaboratorioPuzzle porta = new PortaLaboratorioPuzzle();
        Player player = giocatore();

        PuzzleOutcome esito = porta.forzaBruta(player);

        assertTrue(esito.risolto());
        assertEquals(PortaLaboratorioPuzzle.COSTO_FORZA_BRUTA, esito.energiaConsumata());
        assertEquals(Player.ENERGIA_MASSIMA - PortaLaboratorioPuzzle.COSTO_FORZA_BRUTA, player.getEnergia());
        assertEquals(PortaLaboratorioPuzzle.XP, player.getXp());
    }

    @Test
    void ogniStatisticaAllaSogliaSbloccaIlProprioIndizio() {
        PortaLaboratorioPuzzle porta = new PortaLaboratorioPuzzle();
        // Il rappresentante ha Carisma 1 (>= soglia) ma Investigazione e Intuizione a 0:
        // vede subito un solo indizio, quello del Carisma.
        Player player = new Player("Eroe", CharacterClass.RAPPRESENTANTE_STUDENTI);
        assertEquals(1, porta.suggerimentiPer(player).size());

        // Portando l'Intuizione alla soglia se ne sblocca un secondo.
        player.aumentaStatistica(StatType.INTUIZIONE, PortaLaboratorioPuzzle.SOGLIA_STAT);
        assertEquals(2, porta.suggerimentiPer(player).size());
    }
}
