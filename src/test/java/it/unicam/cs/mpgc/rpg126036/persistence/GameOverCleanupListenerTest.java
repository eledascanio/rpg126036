package it.unicam.cs.mpgc.rpg126036.persistence;

import it.unicam.cs.mpgc.rpg126036.engine.GameState;
import it.unicam.cs.mpgc.rpg126036.engine.GameSummary;
import it.unicam.cs.mpgc.rpg126036.model.CharacterClass;
import it.unicam.cs.mpgc.rpg126036.model.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica che lo slot venga liberato sia alla sconfitta sia al completamento.
 */
class GameOverCleanupListenerTest {

    private GameState statoSalvato(XmlSaveRepository repository) {
        Player player = new Player("Eroe", CharacterClass.STUDENTE_MODELLO);
        GameState stato = new GameState(player);
        repository.save(stato);
        return stato;
    }

    @Test
    void laSconfittaEliminaLoSlot(@TempDir Path dir) {
        XmlSaveRepository repository = new XmlSaveRepository(dir);
        GameState stato = statoSalvato(repository);
        assertTrue(repository.exists("Eroe"));

        new GameOverCleanupListener(repository, stato).onGameOver();

        assertFalse(repository.exists("Eroe"));
    }

    @Test
    void ilCompletamentoEliminaLoSlot(@TempDir Path dir) {
        XmlSaveRepository repository = new XmlSaveRepository(dir);
        GameState stato = statoSalvato(repository);
        GameSummary riepilogo = new GameSummary(stato.getPlayer().getNome(),
                stato.getPlayer().getClasse(), stato.getPlayer().getStatistiche(), stato.getPlayer().getXp());

        new GameOverCleanupListener(repository, stato).onGameCompleted(riepilogo);

        assertFalse(repository.exists("Eroe"));
    }
}
