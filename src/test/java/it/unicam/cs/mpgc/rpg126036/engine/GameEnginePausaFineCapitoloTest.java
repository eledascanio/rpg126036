package it.unicam.cs.mpgc.rpg126036.engine;

import it.unicam.cs.mpgc.rpg126036.interaction.SimpleInteraction;
import it.unicam.cs.mpgc.rpg126036.model.Chapter;
import it.unicam.cs.mpgc.rpg126036.model.CharacterClass;
import it.unicam.cs.mpgc.rpg126036.model.Clue;
import it.unicam.cs.mpgc.rpg126036.model.Player;
import it.unicam.cs.mpgc.rpg126036.model.Scene;
import it.unicam.cs.mpgc.rpg126036.model.StatType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica il blocco delle azioni in pausa e la sequenza di fine capitolo del
 * {@link GameEngine}.
 */
class GameEnginePausaFineCapitoloTest {

    /** Capitolo "inizio" -> "fine" (fine terminale). */
    private Chapter capitolo(String id) {
        Chapter capitolo = new Chapter(id, "Titolo " + id);
        Scene inizio = new Scene("inizio_" + id, "Inizio", "...");
        inizio.aggiungiTransizione("Prosegui", "fine_" + id);
        capitolo.aggiungiScena(inizio);
        capitolo.aggiungiScena(new Scene("fine_" + id, "Fine", "..."));
        return capitolo;
    }

    private GameEngine engineDueCapitoli(Player player) {
        Chapter c1 = capitolo("c1");
        Chapter c2 = capitolo("c2");
        return new GameEngine(new GameState(player, c1), List.of(c1, c2));
    }

    @Test
    void inPausaLeAzioniSonoBloccate() {
        Player player = new Player("Eroe", CharacterClass.STUDENTE_MODELLO);
        GameEngine engine = engineDueCapitoli(player);
        engine.pausa();

        assertTrue(engine.isInPausa());
        assertFalse(engine.esegui(new SimpleInteraction("a", "a", true, "ok", 0, 10)).successo());
        assertEquals(0, player.getXp());
        assertFalse(engine.avanza("fine_c1"));
        assertFalse(engine.trovaIndizio(new Clue("x", "X", "...")));

        engine.riprendi();
        assertFalse(engine.isInPausa());
        assertTrue(engine.avanza("fine_c1"));
    }

    @Test
    void concludiCapitoloRipristinaEnergiaPotenziaEAvanza() {
        Player player = new Player("Eroe", CharacterClass.STUDENTE_MODELLO);
        GameEngine engine = engineDueCapitoli(player);
        boolean[] completato = {false};
        engine.addListener(new GameListener() {
            @Override
            public void onChapterCompleted(Chapter capitolo) {
                completato[0] = true;
            }
        });

        engine.avanza("fine_c1");          // completa il primo capitolo
        player.consumaEnergia(60);          // energia a 40
        int carismaPrima = player.getStatistica(StatType.CARISMA);

        boolean esito = engine.concludiCapitolo(StatType.CARISMA);

        assertTrue(esito);
        assertEquals(Player.ENERGIA_MASSIMA, player.getEnergia());
        assertEquals(carismaPrima + 1, player.getStatistica(StatType.CARISMA));
        assertEquals("c2", engine.getCapitoloCorrente().getId());
        assertTrue(completato[0]);
    }
}
