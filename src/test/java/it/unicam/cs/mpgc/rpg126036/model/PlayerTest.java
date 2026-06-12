package it.unicam.cs.mpgc.rpg126036.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica la gestione di energia, XP, potenziamenti e collezioni del {@link Player}.
 */
class PlayerTest {

    private Player nuovoPlayer() {
        return new Player("Eroe", CharacterClass.STUDENTE_MODELLO);
    }

    @Test
    void partireDaEnergiaMassimaEStatisticheDellaClasse() {
        Player player = nuovoPlayer();

        assertEquals(Player.ENERGIA_MASSIMA, player.getEnergia());
        assertEquals(0, player.getXp());
        assertEquals(1, player.getStatistica(StatType.INVESTIGAZIONE));
    }

    @Test
    void consumaEnergiaSoloSeSufficiente() {
        Player player = nuovoPlayer();

        assertTrue(player.consumaEnergia(30));
        assertEquals(70, player.getEnergia());
        // Richiesta superiore al disponibile: rifiutata senza modificare l'energia.
        assertFalse(player.consumaEnergia(200));
        assertEquals(70, player.getEnergia());
    }

    @Test
    void riduciEnergiaSiFermaAZeroERipristinaNonSuperaIlMassimo() {
        Player player = nuovoPlayer();

        player.riduciEnergia(150);
        assertEquals(0, player.getEnergia());
        player.ripristinaEnergia(250);
        assertEquals(Player.ENERGIA_MASSIMA, player.getEnergia());
    }

    @Test
    void potenziaStatisticaSpende100XpEFallisceSottoSoglia() {
        Player player = nuovoPlayer();

        assertFalse(player.potenziaStatistica(StatType.CARISMA));
        player.aggiungiXp(100);
        assertTrue(player.potenziaStatistica(StatType.CARISMA));
        assertEquals(1, player.getStatistica(StatType.CARISMA));
        assertEquals(0, player.getXp());
    }

    @Test
    void aumentaStatisticaNonCostaXp() {
        Player player = nuovoPlayer();

        player.aumentaStatistica(StatType.INTUIZIONE, 2);
        assertEquals(2, player.getStatistica(StatType.INTUIZIONE));
        assertEquals(0, player.getXp());
    }

    @Test
    void leQuantitaNegativeSonoRifiutate() {
        Player player = nuovoPlayer();

        assertThrows(IllegalArgumentException.class, () -> player.consumaEnergia(-1));
        assertThrows(IllegalArgumentException.class, () -> player.riduciEnergia(-1));
        assertThrows(IllegalArgumentException.class, () -> player.ripristinaEnergia(-1));
        assertThrows(IllegalArgumentException.class, () -> player.aggiungiXp(-1));
        assertThrows(IllegalArgumentException.class, () -> player.aumentaStatistica(StatType.CARISMA, -1));
    }

    @Test
    void raccogliereOggettiEScoprireIndiziAggiornaLeCollezioni() {
        Player player = nuovoPlayer();

        assertTrue(player.raccogli(ItemCatalog.chiaveCapitoloUno()));
        assertFalse(player.raccogli(ItemCatalog.chiaveCapitoloUno()));
        assertTrue(player.getInventario().contiene(ItemCatalog.ID_CHIAVE_CAPITOLO1));

        assertTrue(player.scopriIndizio(ClueCatalog.alexKaur()));
        assertTrue(player.getDiario().contiene(ClueCatalog.ID_ALEX_KAUR));
    }
}
