package it.unicam.cs.mpgc.rpg126036.engine;

import it.unicam.cs.mpgc.rpg126036.interaction.SimpleInteraction;
import it.unicam.cs.mpgc.rpg126036.model.Chapter;
import it.unicam.cs.mpgc.rpg126036.model.CharacterClass;
import it.unicam.cs.mpgc.rpg126036.model.Player;
import it.unicam.cs.mpgc.rpg126036.model.Scene;
import it.unicam.cs.mpgc.rpg126036.model.StatType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica la condizione di completamento del gioco (vittoria) e la sua
 * distinzione dalla sconfitta, gestite dal {@link GameEngine}.
 */
class GameEngineCompletionTest {

    /** Registra l'esito notificato dal motore. */
    private static final class SpiaEsito implements GameListener {
        GameSummary completamento;
        int gameOver;

        @Override
        public void onGameCompleted(GameSummary riepilogo) {
            completamento = riepilogo;
        }

        @Override
        public void onGameOver() {
            gameOver++;
        }
    }

    /** Capitolo a due scene: {@code inizio} (con arco) e {@code fine} (terminale). */
    private Chapter capitoloConFinale(String id) {
        Chapter capitolo = new Chapter(id, "Titolo");
        Scene inizio = new Scene("inizio", "Inizio", "Descrizione");
        inizio.aggiungiTransizione("Concludi", "fine");
        capitolo.aggiungiScena(inizio);
        capitolo.aggiungiScena(new Scene("fine", "Fine", "Descrizione"));
        return capitolo;
    }

    @Test
    void completareLUltimoCapitoloEmetteOnGameCompleted() {
        Player player = new Player("Detective", CharacterClass.INVESTIGATORE);
        Chapter capitolo = capitoloConFinale("capitolo3");
        GameEngine engine = new GameEngine(new GameState(player, capitolo), List.of(capitolo));
        SpiaEsito spia = new SpiaEsito();
        engine.addListener(spia);
        engine.esegui(new SimpleInteraction("a", "Azione", true, "Ok", 0, 50));

        engine.avanza("fine");

        assertTrue(engine.isPartitaTerminata());
        // Vittoria: nessun game over, riepilogo coerente con lo stato finale.
        assertEquals(0, spia.gameOver);
        assertEquals("Detective", spia.completamento.nomePersonaggio());
        assertEquals(CharacterClass.INVESTIGATORE, spia.completamento.classe());
        assertEquals(50, spia.completamento.xpTotali());
        assertEquals(player.getStatistiche(), spia.completamento.statisticheFinali());
    }

    @Test
    void ilRiepilogoEImmutabileEFotografaLoStatoAlCompletamento() {
        Player player = new Player("Detective", CharacterClass.INVESTIGATORE);
        int investigazioneFinale = player.getStatistica(StatType.INVESTIGAZIONE);
        Chapter capitolo = capitoloConFinale("capitolo3");
        GameEngine engine = new GameEngine(new GameState(player, capitolo), List.of(capitolo));
        SpiaEsito spia = new SpiaEsito();
        engine.addListener(spia);

        engine.avanza("fine");
        // Modifiche successive al giocatore non alterano il riepilogo gia' emesso.
        player.aumentaStatistica(StatType.INVESTIGAZIONE, 5);

        assertEquals(investigazioneFinale,
                spia.completamento.statisticheFinali().get(StatType.INVESTIGAZIONE));
    }

    @Test
    void laSconfittaPerEnergiaEmetteOnGameOverNonOnGameCompleted() {
        Player player = new Player("Detective", CharacterClass.INVESTIGATORE);
        // Scena corrente non terminale: il capitolo non e' completato.
        Chapter capitolo = capitoloConFinale("capitolo3");
        GameEngine engine = new GameEngine(new GameState(player, capitolo), List.of(capitolo));
        SpiaEsito spia = new SpiaEsito();
        engine.addListener(spia);

        engine.esegui(new SimpleInteraction("crollo", "Sforzo", true, "Esausto",
                Player.ENERGIA_MASSIMA, 0));

        assertEquals(1, spia.gameOver);
        assertNull(spia.completamento);
    }
}
