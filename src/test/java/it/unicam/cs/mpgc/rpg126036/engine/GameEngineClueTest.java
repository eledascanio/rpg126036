package it.unicam.cs.mpgc.rpg126036.engine;

import it.unicam.cs.mpgc.rpg126036.model.Chapter;
import it.unicam.cs.mpgc.rpg126036.model.CharacterClass;
import it.unicam.cs.mpgc.rpg126036.model.Clue;
import it.unicam.cs.mpgc.rpg126036.model.ClueCatalog;
import it.unicam.cs.mpgc.rpg126036.model.Player;
import it.unicam.cs.mpgc.rpg126036.model.Scene;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica la scoperta degli indizi tramite il {@link GameEngine}.
 */
class GameEngineClueTest {

    /** Raccoglie gli indizi notificati da {@code onClueFound}. */
    private static final class RaccoglitoreIndizi implements GameListener {
        final List<Clue> trovati = new ArrayList<>();

        @Override
        public void onClueFound(Clue indizio) {
            trovati.add(indizio);
        }
    }

    private GameEngine motore(Player player) {
        Chapter capitolo = new Chapter("capitolo1", "Titolo");
        capitolo.aggiungiScena(new Scene("scena", "Scena", "Descrizione"));
        return new GameEngine(new GameState(player, capitolo), List.of(capitolo));
    }

    @Test
    void scoprireUnIndizioLoRegistraEloNotifica() {
        Player player = new Player("Eroe", CharacterClass.INVESTIGATORE);
        GameEngine engine = motore(player);
        RaccoglitoreIndizi listener = new RaccoglitoreIndizi();
        engine.addListener(listener);

        boolean nuovo = engine.trovaIndizio(ClueCatalog.alexKaur());

        assertTrue(nuovo);
        assertTrue(player.getDiario().contiene(ClueCatalog.ID_ALEX_KAUR));
        assertEquals(1, listener.trovati.size());
        assertEquals(ClueCatalog.ID_ALEX_KAUR, listener.trovati.get(0).id());
    }

    @Test
    void unIndizioGiaNotoNonVieneRinotificato() {
        Player player = new Player("Eroe", CharacterClass.INVESTIGATORE);
        GameEngine engine = motore(player);
        RaccoglitoreIndizi listener = new RaccoglitoreIndizi();
        engine.addListener(listener);

        engine.trovaIndizio(ClueCatalog.alexKaur());
        boolean secondaVolta = engine.trovaIndizio(ClueCatalog.alexKaur());

        assertFalse(secondaVolta);
        assertEquals(1, player.getDiario().size());
        assertEquals(1, listener.trovati.size());
    }

    @Test
    void inPausaNonSiScopronoIndizi() {
        Player player = new Player("Eroe", CharacterClass.INVESTIGATORE);
        GameEngine engine = motore(player);
        engine.pausa();

        boolean esito = engine.trovaIndizio(ClueCatalog.alexKaur());

        assertFalse(esito);
        assertTrue(player.getDiario().isEmpty());
    }
}
