package it.unicam.cs.mpgc.rpg126036.persistence;

import it.unicam.cs.mpgc.rpg126036.engine.GameState;
import it.unicam.cs.mpgc.rpg126036.model.Chapter;
import it.unicam.cs.mpgc.rpg126036.model.CharacterClass;
import it.unicam.cs.mpgc.rpg126036.model.ClueCatalog;
import it.unicam.cs.mpgc.rpg126036.model.ItemCatalog;
import it.unicam.cs.mpgc.rpg126036.model.Player;
import it.unicam.cs.mpgc.rpg126036.model.Scene;
import it.unicam.cs.mpgc.rpg126036.model.StatType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica il ciclo completo di salvataggio/caricamento XML e la gestione dei tre
 * slot.
 */
class XmlSaveRepositoryTest {

    private GameState partitaAvanzata() {
        Player player = new Player("Detective", CharacterClass.STUDENTE_MODELLO);
        player.consumaEnergia(40);                                 // energia 60
        player.aggiungiXp(50);
        player.aumentaStatistica(StatType.INVESTIGAZIONE, 2);      // 1 -> 3
        player.raccogli(ItemCatalog.chiaveCapitoloUno());
        player.scopriIndizio(ClueCatalog.alexKaur());

        Chapter capitolo = new Chapter("capitolo1", "Capitolo 1");
        Scene inizio = new Scene("inizio", "Inizio", "...");
        inizio.aggiungiTransizione("Prosegui", "fine");
        capitolo.aggiungiScena(inizio);
        capitolo.aggiungiScena(new Scene("fine", "Fine", "..."));
        capitolo.vaiA("fine");                                     // "inizio" completata

        GameState stato = new GameState(player, capitolo);
        stato.setFlag("porta_aperta");
        return stato;
    }

    private GameState partitaSemplice(String nome) {
        return new GameState(new Player(nome, CharacterClass.ORGANIZZATORE_EVENTI));
    }

    @Test
    void ilCicloSalvaCaricaPreservaInteroStato(@TempDir Path dir) {
        XmlSaveRepository repository = new XmlSaveRepository(dir);
        repository.save(partitaAvanzata());

        GameState caricato = repository.load("Detective").orElseThrow();
        Player p = caricato.getPlayer();

        assertEquals(60, p.getEnergia());
        assertEquals(50, p.getXp());
        assertEquals(3, p.getStatistica(StatType.INVESTIGAZIONE));
        assertTrue(p.getInventario().contiene(ItemCatalog.ID_CHIAVE_CAPITOLO1));
        assertTrue(p.getDiario().contiene(ClueCatalog.ID_ALEX_KAUR));
        assertTrue(caricato.hasFlag("porta_aperta"));
        Chapter capitolo = caricato.getCapitoloCorrente().orElseThrow();
        assertEquals("capitolo1", capitolo.getId());
        assertEquals("fine", capitolo.getScenaCorrente().getId());
        assertTrue(capitolo.getScena("inizio").orElseThrow().isCompletata());
    }

    @Test
    void listSlotsRiportaIMetadatiSenzaCaricareLoStato(@TempDir Path dir) {
        XmlSaveRepository repository = new XmlSaveRepository(dir);
        repository.save(partitaAvanzata());

        var slot = repository.listSlots();
        assertEquals(1, slot.size());
        assertEquals("Detective", slot.get(0).nomePersonaggio());
        assertEquals("STUDENTE_MODELLO", slot.get(0).classe());
        assertEquals("capitolo1", slot.get(0).idCapitolo());
    }

    @Test
    void caricareUnoSlotInesistenteDaVuoto(@TempDir Path dir) {
        assertTrue(new XmlSaveRepository(dir).load("Nessuno").isEmpty());
    }

    @Test
    void eliminareUnoSlotLoRendeNonPiuEsistente(@TempDir Path dir) {
        XmlSaveRepository repository = new XmlSaveRepository(dir);
        repository.save(partitaAvanzata());

        assertTrue(repository.delete("Detective"));
        assertFalse(repository.exists("Detective"));
        assertTrue(repository.load("Detective").isEmpty());
    }

    @Test
    void salvareUnaNuovaPartitaConGliSlotPieniFallisce(@TempDir Path dir) {
        XmlSaveRepository repository = new XmlSaveRepository(dir);
        repository.save(partitaSemplice("A"));
        repository.save(partitaSemplice("B"));
        repository.save(partitaSemplice("C"));

        assertTrue(repository.isFull());
        assertThrows(PersistenceException.class, () -> repository.save(partitaSemplice("D")));
    }
}
