package it.unicam.cs.mpgc.rpg126036.view;

import it.unicam.cs.mpgc.rpg126036.model.Puzzle;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

/**
 * Ruolo degli overlay personalizzati, oltre al dialog box standard: l'enigma a
 * tastierino e i pannelli modali avvolti nel velo scuro.
 *
 * <p>Uno dei ruoli in cui è segregata la {@link RegiaEsplorazione} (ISP). Vi
 * dipendono solo i componenti con pannelli su misura: {@link Porte} (enigma della
 * porta del laboratorio) e {@link PcVittima} (pannelli del terminale).</p>
 */
public interface ServiziOverlay {

    /** Mostra l'enigma a tastierino dell'elemento indicato (porta del laboratorio). */
    void mostraEnigma(Puzzle puzzle, ElementoScena elemento);

    /** Avvolge un contenuto nel velo scuro dell'overlay. */
    StackPane velo(Node contenuto);

    /** Mostra un overlay (eventualmente chiudibile con ESC), sostituendo il precedente. */
    void mostraOverlay(Node velo, boolean chiudibile);
}
