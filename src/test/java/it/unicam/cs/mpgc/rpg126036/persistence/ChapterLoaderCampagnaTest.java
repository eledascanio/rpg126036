package it.unicam.cs.mpgc.rpg126036.persistence;

import it.unicam.cs.mpgc.rpg126036.model.Chapter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica che gli XML dei tre capitoli della campagna siano ben formati,
 * caricabili dal {@link ChapterLoader} e coerenti (scena iniziale, scena finale
 * terminale, riferimenti ai contenuti).
 */
class ChapterLoaderCampagnaTest {

    private final ChapterLoader loader = new ChapterLoader();

    private ChapterDefinition carica(String risorsa) {
        return loader.loadFromResource(risorsa);
    }

    @Test
    void ilCapitolo1SiCaricaConIlCortileComeScenaUnica() {
        ChapterDefinition def = carica("/capitoli/capitolo1.xml");
        Chapter capitolo = def.capitolo();

        assertEquals("capitolo1", capitolo.getId());
        assertEquals("cortile", capitolo.getIdScenaIniziale().orElseThrow());
        // Il cortile raccoglie NPC, chiave ed enigma della porta del laboratorio.
        assertTrue(def.contenutiDi("cortile").npc().contains("informatore"));
        assertTrue(def.contenutiDi("cortile").oggetti().contains("chiave_capitolo1"));
        assertTrue(def.contenutiDi("cortile").enigmi().contains("porta_laboratorio"));
        // E' la scena finale del capitolo: risolverla conclude il capitolo 1.
        assertTrue(capitolo.getScena("cortile").orElseThrow().isTerminale());
    }

    @Test
    void ilCapitolo2SiCaricaConISuoiContenuti() {
        ChapterDefinition def = carica("/capitoli/capitolo2.xml");
        Chapter capitolo = def.capitolo();

        assertEquals("capitolo2", capitolo.getId());
        assertEquals("aula_la1", capitolo.getIdScenaIniziale().orElseThrow());
        // L'aula LA1 contiene l'assistente e l'enigma del PC della vittima.
        assertTrue(def.contenutiDi("aula_la1").npc().contains("tecnico_laboratorio"));
        assertTrue(def.contenutiDi("aula_la1").enigmi().contains("pc_vittima"));
        // L'email e' la scena finale del capitolo.
        assertTrue(capitolo.getScena("email_vittima").orElseThrow().isTerminale());
    }

    @Test
    void ilCapitolo3SiCaricaConISuoiContenuti() {
        ChapterDefinition def = carica("/capitoli/capitolo3.xml");
        Chapter capitolo = def.capitolo();

        assertEquals("capitolo3", capitolo.getId());
        assertEquals("cortile", capitolo.getIdScenaIniziale().orElseThrow());
        // L'addetto alle pulizie e' all'ingresso dell'Aula B.
        assertTrue(def.contenutiDi("ingresso_aula_b").npc().contains("addetto_pulizie"));
        // L'Aula B e' la scena finale della campagna (terminale).
        assertTrue(capitolo.getScena("aula_b").orElseThrow().isTerminale());
    }

    @Test
    void leSceneIntermedieNonSonoTerminali() {
        Chapter cap3 = carica("/capitoli/capitolo3.xml").capitolo();

        assertFalse(cap3.getScena("cortile").orElseThrow().isTerminale());
        assertFalse(cap3.getScena("ingresso_aula_b").orElseThrow().isTerminale());
    }
}
