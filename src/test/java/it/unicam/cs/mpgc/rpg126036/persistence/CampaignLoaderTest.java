package it.unicam.cs.mpgc.rpg126036.persistence;

import it.unicam.cs.mpgc.rpg126036.model.Chapter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica il caricamento della campagna a tre capitoli dai file XML.
 */
class CampaignLoaderTest {

    private final CampaignLoader loader = new CampaignLoader();

    @Test
    void caricaLaCampagnaStandardNellOrdineCorretto() {
        Campaign campagna = loader.caricaStandard();

        assertEquals(3, campagna.size());
        List<String> idCapitoli = campagna.getCapitoli().stream().map(Chapter::getId).toList();
        assertEquals(List.of("capitolo1", "capitolo2", "capitolo3"), idCapitoli);
    }

    @Test
    void iContenutiDelleSceneSonoIndicizzatiPerCapitoloEScena() {
        Campaign campagna = loader.caricaStandard();

        assertTrue(campagna.contenutiDi("capitolo2", "aula_la1").enigmi().contains("pc_vittima"));
        assertTrue(campagna.contenutiDi("capitolo3", "ingresso_aula_b").npc().contains("addetto_pulizie"));
    }

    @Test
    void contenutiDiCapitoloOScenaInesistentiSonoVuoti() {
        Campaign campagna = loader.caricaStandard();

        assertTrue(campagna.contenutiDi("capitolo1", "scena_inesistente").enigmi().isEmpty());
        assertTrue(campagna.contenutiDi("capitolo_inesistente", "x").npc().isEmpty());
    }
}
