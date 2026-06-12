package it.unicam.cs.mpgc.rpg126036.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica il comportamento del diario degli indizi {@link ClueJournal}.
 */
class ClueJournalTest {

    @Test
    void aggiungeUnIndizioNuovoEloRitrova() {
        ClueJournal diario = new ClueJournal();

        assertTrue(diario.aggiungi(ClueCatalog.alexKaur()));
        assertTrue(diario.contiene(ClueCatalog.ID_ALEX_KAUR));
        assertEquals(1, diario.size());
        assertFalse(diario.isEmpty());
    }

    @Test
    void nonRegistraDueVolteLoStessoIndizio() {
        ClueJournal diario = new ClueJournal();
        diario.aggiungi(ClueCatalog.passwordPc());

        assertFalse(diario.aggiungi(ClueCatalog.passwordPc()));
        assertEquals(1, diario.size());
    }

    @Test
    void mantieneLOrdineDiScoperta() {
        ClueJournal diario = new ClueJournal();
        diario.aggiungi(ClueCatalog.alexKaur());
        diario.aggiungi(ClueCatalog.passwordPc());
        diario.aggiungi(ClueCatalog.emailMittente());

        List<Clue> indizi = diario.getIndizi();
        assertEquals(List.of(ClueCatalog.ID_ALEX_KAUR, ClueCatalog.ID_PASSWORD_PC, ClueCatalog.ID_EMAIL_MITTENTE),
                indizi.stream().map(Clue::id).toList());
    }
}
