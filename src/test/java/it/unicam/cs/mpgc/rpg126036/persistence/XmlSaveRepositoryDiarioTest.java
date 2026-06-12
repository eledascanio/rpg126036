package it.unicam.cs.mpgc.rpg126036.persistence;

import it.unicam.cs.mpgc.rpg126036.engine.GameState;
import it.unicam.cs.mpgc.rpg126036.model.CharacterClass;
import it.unicam.cs.mpgc.rpg126036.model.Clue;
import it.unicam.cs.mpgc.rpg126036.model.ClueCatalog;
import it.unicam.cs.mpgc.rpg126036.model.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifica che il diario degli indizi sopravviva al ciclo di salvataggio e
 * ricaricamento tramite {@link XmlSaveRepository}.
 */
class XmlSaveRepositoryDiarioTest {

    @Test
    void ilDiarioVienePersistitoERicaricato(@TempDir Path cartella) {
        XmlSaveRepository repository = new XmlSaveRepository(cartella);
        Player player = new Player("Detective", CharacterClass.STUDENTE_MODELLO);
        player.scopriIndizio(ClueCatalog.alexKaur());
        player.scopriIndizio(ClueCatalog.emailMittente());
        repository.save(new GameState(player));

        GameState ricaricato = repository.load("Detective").orElseThrow();

        List<Clue> indizi = ricaricato.getDiario().getIndizi();
        assertEquals(2, indizi.size());
        assertTrue(ricaricato.getDiario().contiene(ClueCatalog.ID_ALEX_KAUR));
        // Il contenuto testuale dell'indizio e' preservato fedelmente.
        assertEquals(ClueCatalog.emailMittente().testo(),
                ricaricato.getDiario().get(ClueCatalog.ID_EMAIL_MITTENTE).orElseThrow().testo());
    }
}
