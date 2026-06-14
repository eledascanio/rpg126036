package it.unicam.cs.mpgc.rpg126036.app;

import it.unicam.cs.mpgc.rpg126036.engine.GameState;
import it.unicam.cs.mpgc.rpg126036.model.CharacterClass;
import it.unicam.cs.mpgc.rpg126036.model.Clue;
import it.unicam.cs.mpgc.rpg126036.model.ClueCatalog;
import it.unicam.cs.mpgc.rpg126036.model.Player;
import it.unicam.cs.mpgc.rpg126036.model.Puzzle;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica che il {@link ContentResolver} risolva gli id dei contenuti nelle
 * istanze concrete e l'associazione tra NPC utili e indizi.
 */
class ContentResolverTest {

    private final ContentResolver resolver = new ContentResolver();

    private GameState statoVuoto() {
        return new GameState(new Player("Tester", CharacterClass.STUDENTE_MODELLO));
    }

    @Test
    void risolveGliNpcDiTuttiICapitoli() {
        assertTrue(resolver.npc("studente_ubriaco").isPresent());
        assertTrue(resolver.npc("tecnico_laboratorio").isPresent());
        assertTrue(resolver.npc("addetto_pulizie").isPresent());
        assertTrue(resolver.npc("inesistente").isEmpty());
    }

    @Test
    void risolveGliOggettiRaccoglibili() {
        Optional<?> chiave = resolver.item("chiave_capitolo1");
        assertTrue(chiave.isPresent());
        assertTrue(resolver.item("oggetto_ignoto").isEmpty());
    }

    @Test
    void creaIstanzeDistinteDegliEnigmiNoti() {
        Optional<Puzzle> primo = resolver.creaEnigma("porta_laboratorio", statoVuoto());
        Optional<Puzzle> secondo = resolver.creaEnigma("porta_laboratorio", statoVuoto());
        assertTrue(primo.isPresent());
        assertTrue(secondo.isPresent());
        // Gli enigmi sono stateful: due richieste devono produrre istanze separate.
        assertFalse(primo.get() == secondo.get());

        assertTrue(resolver.creaEnigma("pc_vittima", statoVuoto()).isPresent());
        assertTrue(resolver.creaEnigma("enigma_ignoto", statoVuoto()).isEmpty());
    }

    @Test
    void associaGliNpcUtiliAiLoroIndizi() {
        Optional<Clue> indizio = resolver.indizioDi("studente_ubriaco");
        assertTrue(indizio.isPresent());
        assertEquals(ClueCatalog.ID_ALEX_KAUR, indizio.get().id());

        assertEquals(ClueCatalog.ID_PASSWORD_PC, resolver.indizioDi("tecnico_laboratorio").orElseThrow().id());
        assertEquals(ClueCatalog.ID_FUGA_STUDENTE, resolver.indizioDi("addetto_pulizie").orElseThrow().id());
        // Gli NPC di contorno non sbloccano indizi.
        assertTrue(resolver.indizioDi("studente_panico").isEmpty());
    }
}
