package it.unicam.cs.mpgc.rpg126036.app;

import it.unicam.cs.mpgc.rpg126036.achievement.AchievementCatalog;
import it.unicam.cs.mpgc.rpg126036.interaction.ItemInteraction;
import it.unicam.cs.mpgc.rpg126036.model.CharacterClass;
import it.unicam.cs.mpgc.rpg126036.model.ItemCatalog;
import it.unicam.cs.mpgc.rpg126036.persistence.Campaign;
import it.unicam.cs.mpgc.rpg126036.persistence.CampaignLoader;
import it.unicam.cs.mpgc.rpg126036.persistence.XmlSaveRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica l'assemblaggio di una sessione di gioco tramite il bootstrap.
 */
class GameSessionFactoryTest {

    private GameSessionFactory factory(Path cartella) {
        Campaign campagna = new CampaignLoader().caricaStandard();
        return new GameSessionFactory(new XmlSaveRepository(cartella), campagna);
    }

    @Test
    void nuovaPartitaParteDalPrimoCapitolo(@TempDir Path cartella) {
        GameSession sessione = factory(cartella)
                .nuovaPartita("Detective", CharacterClass.STUDENTE_MODELLO);

        assertEquals("capitolo1", sessione.getEngine().getCapitoloCorrente().getId());
        assertEquals("Detective", sessione.getStato().getPlayer().getNome());
        assertEquals(3, sessione.getCampaign().size());
    }

    @Test
    void ilGestoreDeiTraguardiERegistratoSulMotore(@TempDir Path cartella) {
        GameSession sessione = factory(cartella)
                .nuovaPartita("Detective", CharacterClass.STUDENTE_MODELLO);

        // Trovare la chiave nel capitolo 1 deve sbloccare il traguardo: prova che
        // l'AchievementManager e' effettivamente collegato all'engine dal bootstrap.
        sessione.getEngine().raccogli(new ItemInteraction(ItemCatalog.chiaveCapitoloUno()));

        assertTrue(sessione.getAchievementManager().isSbloccato(AchievementCatalog.ID_CHIAVE_CAPITOLO1));
    }
}
