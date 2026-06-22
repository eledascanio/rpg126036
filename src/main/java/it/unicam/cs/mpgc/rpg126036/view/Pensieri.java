package it.unicam.cs.mpgc.rpg126036.view;

import it.unicam.cs.mpgc.rpg126036.engine.GameEngine;
import it.unicam.cs.mpgc.rpg126036.engine.GameState;
import it.unicam.cs.mpgc.rpg126036.model.Chapter;

import java.util.Objects;

/**
 * Pensieri del protagonista mostrati in momenti chiave dell'esplorazione: piccole
 * battute fra sé che inquadrano l'indagine o spronano ad agire. Sono trasversali
 * (non appartengono a un singolo luogo o oggetto) e per questo raccolti qui anziché
 * nei componenti di trama specifici.
 *
 * <p>Estratti dalla {@link ExplorationView}: ognuno scatta una sola volta, alle
 * condizioni di trama del proprio capitolo, e usa il solo ruolo {@link ServiziDialoghi}
 * per mostrare il dialogo (ISP). Il pensiero d'indagine dipende dallo sblocco della
 * porta del laboratorio, noto alle {@link Porte}.</p>
 */
final class Pensieri {

    private final ServiziDialoghi dialoghi;
    private final GameState stato;
    private final GameEngine engine;
    private final Porte porte;

    // Ogni pensiero "una tantum" ricorda di essere già stato mostrato.
    private boolean indagineMostrato;
    private boolean cortileMostrato;

    /**
     * @param dialoghi i servizi di dialogo della scena (per mostrare i pensieri)
     * @param stato    lo stato di gioco (nome del giocatore)
     * @param engine   il motore di gioco (scena e capitolo correnti)
     * @param porte    le porte degli edifici (sblocco dell'enigma del laboratorio)
     */
    Pensieri(ServiziDialoghi dialoghi, GameState stato, GameEngine engine, Porte porte) {
        this.dialoghi = Objects.requireNonNull(dialoghi, "I servizi di dialogo non possono essere nulli.");
        this.stato = Objects.requireNonNull(stato, "Lo stato non puo' essere nullo.");
        this.engine = Objects.requireNonNull(engine, "Il motore non puo' essere nullo.");
        this.porte = Objects.requireNonNull(porte, "Le porte non possono essere nulle.");
    }

    /**
     * Mostra una sola volta il pensiero che indirizza verso il PC di Antonio, non
     * appena il giocatore possiede sia la chiave sia l'indizio su Alex Kaur (in
     * qualunque ordine abbia compiuto le due azioni).
     */
    void forseIndagine() {
        if (!indagineMostrato && porte.isEnigmaPortaSbloccato()) {
            indagineMostrato = true;
            dialoghi.mostraDialogo(stato.getPlayer().getNome(),
                    "Quel ragazzo ha detto di aver visto Antonio litigare con Alex… ma perché "
                            + "l'aggressore avrebbe dovuto rubargli le chiavi? Mi servono altre "
                            + "informazioni… forse riesco a controllare il PC di Antonio al Polo A.");
        }
    }

    /**
     * Mostra una sola volta, appena usciti dal Polo A nel cortile (capitolo 3), il
     * pensiero che sprona il giocatore a sbrigarsi.
     */
    void forseCortile() {
        if (!cortileMostrato && eCortileCapitolo3()) {
            cortileMostrato = true;
            dialoghi.mostraDialogo(stato.getPlayer().getNome(),
                    "Stanno tutti scappando, devo muovermi prima che sia tardi.");
        }
    }

    /**
     * All'inizio di alcuni capitoli il personaggio esprime un pensiero che ne inquadra
     * l'obiettivo. Nel capitolo 3 lo spinge a investigare al Polo B.
     *
     * @param nuovo il capitolo appena iniziato
     */
    void forseInizioCapitolo(Chapter nuovo) {
        if ("capitolo3".equals(nuovo.getId())) {
            dialoghi.mostraDialogo(stato.getPlayer().getNome(),
                    "Devo assolutamente andare a investigare al polo B prima che sia troppo tardi.");
        }
    }

    private boolean eCortileCapitolo3() {
        return "cortile".equals(engine.getScenaCorrente().getId())
                && "capitolo3".equals(engine.getCapitoloCorrente().getId());
    }
}
