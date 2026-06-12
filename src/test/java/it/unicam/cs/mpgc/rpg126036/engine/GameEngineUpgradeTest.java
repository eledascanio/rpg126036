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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica la meccanica di potenziamento statistica a 100 XP durante il gioco
 * gestita dal {@link GameEngine}.
 */
class GameEngineUpgradeTest {

    /** Conta le notifiche {@code onUpgradeStatisticaDisponibile}. */
    private static final class ContatoreUpgrade implements GameListener {
        int notifiche;

        @Override
        public void onUpgradeStatisticaDisponibile() {
            notifiche++;
        }
    }

    /** Costruisce un motore con un capitolo a scena singola e un investigatore. */
    private GameEngine motore(Player player) {
        Chapter capitolo = new Chapter("capitolo1", "Titolo");
        capitolo.aggiungiScena(new Scene("scena", "Scena", "Descrizione"));
        return new GameEngine(new GameState(player, capitolo), List.of(capitolo));
    }

    /** Interazione che assegna gli XP indicati senza consumare energia. */
    private SimpleInteraction guadagnoXp(int xp) {
        return new SimpleInteraction("azione", "Azione di test", true, "Fatto.", 0, xp);
    }

    @Test
    void raggiunti100XpSiNotificaUpgradeUnaSolaVolta() {
        Player player = new Player("Eroe", CharacterClass.INVESTIGATORE);
        GameEngine engine = motore(player);
        ContatoreUpgrade contatore = new ContatoreUpgrade();
        engine.addListener(contatore);

        engine.esegui(guadagnoXp(100));
        // Un'ulteriore azione con l'upgrade ancora in sospeso non deve rinotificare.
        engine.esegui(guadagnoXp(10));

        assertTrue(engine.isUpgradeInSospeso());
        assertEquals(1, contatore.notifiche);
    }

    @Test
    void sottoLaSogliaNonSiNotificaNulla() {
        Player player = new Player("Eroe", CharacterClass.INVESTIGATORE);
        GameEngine engine = motore(player);
        ContatoreUpgrade contatore = new ContatoreUpgrade();
        engine.addListener(contatore);

        engine.esegui(guadagnoXp(99));

        assertFalse(engine.isUpgradeInSospeso());
        assertEquals(0, contatore.notifiche);
    }

    @Test
    void applicaUpgradeSpende100XpEAumentaLaStatistica() {
        Player player = new Player("Eroe", CharacterClass.INVESTIGATORE);
        int investigazioneIniziale = player.getStatistica(StatType.INVESTIGAZIONE);
        GameEngine engine = motore(player);
        engine.esegui(guadagnoXp(120));

        boolean applicato = engine.applicaUpgrade(StatType.INVESTIGAZIONE);

        assertTrue(applicato);
        assertEquals(20, player.getXp());
        assertEquals(investigazioneIniziale + 1, player.getStatistica(StatType.INVESTIGAZIONE));
        assertFalse(engine.isUpgradeInSospeso());
    }

    @Test
    void conXpDoppiOSiNotificaDiNuovoDopoIlPrimoUpgrade() {
        Player player = new Player("Eroe", CharacterClass.INVESTIGATORE);
        GameEngine engine = motore(player);
        ContatoreUpgrade contatore = new ContatoreUpgrade();
        engine.addListener(contatore);
        engine.esegui(guadagnoXp(200));

        assertEquals(1, contatore.notifiche);

        // Primo upgrade: restano 100 XP, quindi scatta una seconda notifica.
        engine.applicaUpgrade(StatType.CARISMA);
        assertEquals(2, contatore.notifiche);
        assertTrue(engine.isUpgradeInSospeso());

        // Secondo upgrade: gli XP si azzerano, nessun'altra notifica.
        engine.applicaUpgrade(StatType.INTUIZIONE);
        assertEquals(2, contatore.notifiche);
        assertFalse(engine.isUpgradeInSospeso());
        assertEquals(0, player.getXp());
    }

    @Test
    void applicaUpgradeSenzaXpSufficientiNonFaNulla() {
        Player player = new Player("Eroe", CharacterClass.INVESTIGATORE);
        GameEngine engine = motore(player);

        assertFalse(engine.applicaUpgrade(StatType.INVESTIGAZIONE));
        assertEquals(0, player.getXp());
    }
}
