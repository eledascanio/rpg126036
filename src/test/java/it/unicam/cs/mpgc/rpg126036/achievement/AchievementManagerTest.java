package it.unicam.cs.mpgc.rpg126036.achievement;

import it.unicam.cs.mpgc.rpg126036.engine.GameState;
import it.unicam.cs.mpgc.rpg126036.model.Chapter;
import it.unicam.cs.mpgc.rpg126036.model.CharacterClass;
import it.unicam.cs.mpgc.rpg126036.model.Item;
import it.unicam.cs.mpgc.rpg126036.model.ItemCatalog;
import it.unicam.cs.mpgc.rpg126036.model.Player;
import it.unicam.cs.mpgc.rpg126036.model.Scene;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica lo sblocco del traguardo della chiave nel Capitolo 1 da parte dell'
 * {@link AchievementManager}.
 */
class AchievementManagerTest {

    /** Costruisce uno stato di partita posizionato nel capitolo indicato. */
    private GameState statoNelCapitolo(String idCapitolo) {
        Chapter capitolo = new Chapter(idCapitolo, "Titolo");
        capitolo.aggiungiScena(new Scene("scena", "Scena", "Descrizione"));
        Player player = new Player("Eroe", CharacterClass.STUDENTE_MODELLO);
        return new GameState(player, capitolo);
    }

    @Test
    void chiaveNelCapitolo1SbloccaIlTraguardo() {
        GameState stato = statoNelCapitolo("capitolo1");
        AchievementManager manager = new AchievementManager(stato);

        manager.onItemFound(ItemCatalog.chiaveCapitoloUno());

        assertTrue(manager.isSbloccato(AchievementCatalog.ID_CHIAVE_CAPITOLO1));
        assertEquals(1, manager.getSbloccati().size());
    }

    @Test
    void laStessaChiaveInUnAltroCapitoloNonSbloccaNulla() {
        GameState stato = statoNelCapitolo("capitolo2");
        AchievementManager manager = new AchievementManager(stato);

        manager.onItemFound(ItemCatalog.chiaveCapitoloUno());

        assertFalse(manager.isSbloccato(AchievementCatalog.ID_CHIAVE_CAPITOLO1));
        assertTrue(manager.getSbloccati().isEmpty());
    }

    @Test
    void unAltroOggettoNelCapitolo1NonSbloccaNulla() {
        GameState stato = statoNelCapitolo("capitolo1");
        AchievementManager manager = new AchievementManager(stato);

        manager.onItemFound(new Item("moneta", "Moneta", "Una moneta qualunque."));

        assertFalse(manager.isSbloccato(AchievementCatalog.ID_CHIAVE_CAPITOLO1));
    }

    @Test
    void ilPcAlPrimoColpoNelCapitolo2SbloccaIlTraguardo() {
        GameState stato = statoNelCapitolo("capitolo2");
        AchievementManager manager = new AchievementManager(stato);

        manager.segnalaPcVittimaAlPrimoColpo();

        assertTrue(manager.isSbloccato(AchievementCatalog.ID_PC_PRIMO_COLPO));
    }

    @Test
    void ilPcAlPrimoColpoFuoriDalCapitolo2NonSbloccaNulla() {
        GameState stato = statoNelCapitolo("capitolo1");
        AchievementManager manager = new AchievementManager(stato);

        manager.segnalaPcVittimaAlPrimoColpo();

        assertFalse(manager.isSbloccato(AchievementCatalog.ID_PC_PRIMO_COLPO));
    }

    @Test
    void ilTraguardoVieneNotificatoUnaSolaVolta() {
        GameState stato = statoNelCapitolo("capitolo1");
        AchievementManager manager = new AchievementManager(stato);
        List<Achievement> notificati = new ArrayList<>();
        manager.addAchievementListener(notificati::add);

        // Raccolta ripetuta della stessa chiave: una sola notifica attesa.
        manager.onItemFound(ItemCatalog.chiaveCapitoloUno());
        manager.onItemFound(ItemCatalog.chiaveCapitoloUno());

        assertEquals(1, notificati.size());
        assertEquals(AchievementCatalog.ID_CHIAVE_CAPITOLO1, notificati.get(0).id());
    }
}
