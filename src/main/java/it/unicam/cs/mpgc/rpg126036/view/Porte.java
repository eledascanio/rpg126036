package it.unicam.cs.mpgc.rpg126036.view;

import it.unicam.cs.mpgc.rpg126036.app.ContentResolver;
import it.unicam.cs.mpgc.rpg126036.engine.GameEngine;
import it.unicam.cs.mpgc.rpg126036.engine.GameState;
import it.unicam.cs.mpgc.rpg126036.model.ClueCatalog;
import it.unicam.cs.mpgc.rpg126036.model.ItemCatalog;
import it.unicam.cs.mpgc.rpg126036.model.Player;
import it.unicam.cs.mpgc.rpg126036.model.PortaLaboratorioPuzzle;
import it.unicam.cs.mpgc.rpg126036.model.Puzzle;
import it.unicam.cs.mpgc.rpg126036.model.Transition;
import javafx.scene.paint.Color;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Porte degli edifici sul cortile e porte-uscita invisibili a posizione fissa.
 * Raccoglie la trama cablata delle porte del Polo A e del Polo B (capitoli 1 e 3)
 * e delle uscite "a porta" che, invece di un elemento visibile, si raggiungono
 * avvicinandosi a un punto della facciata:
 * <ul>
 *     <li>Polo A (capitolo 1): porta-enigma del laboratorio, esaminabile solo dopo
 *         aver raccolto la chiave e ottenuto l'indizio su Alex Kaur;</li>
 *     <li>Polo B (capitolo 1): sempre sbarrato, dà solo un avviso;</li>
 *     <li>Polo B (capitolo 3): sbarrato, attraversabile se l'addetto ha aperto la
 *         porta laterale, altrimenti forzabile a costo di energia;</li>
 *     <li>uscite a porta invisibili (aula LA1 e Polo A del capitolo 3).</li>
 * </ul>
 *
 * <p>Estratta dalla {@link ExplorationView} per isolare questa porzione di trama:
 * si appoggia alla schermata (collaboratore) per registrare gli elementi, mostrare
 * enigmi/dialoghi e seguire le transizioni, senza duplicarne l'infrastruttura.</p>
 */
final class Porte {

    // Posizioni delle porte degli edifici sul cortile (frazioni della mappa): Polo A
    // a sinistra, Polo B a destra, alla stessa altezza sulle facciate.
    private static final double CORTILE_PORTA_A_X = 0.20;
    private static final double CORTILE_PORTE_Y = 0.60;
    private static final double CORTILE_PORTA_B_X = 0.80;
    // Porta-uscita dell'aula LA1 (capitolo 3) verso il cortile, in basso a destra: è
    // anche il punto di comparsa quando si rientra in aula dal cortile, davanti ad essa.
    private static final double AULA_LA1_PORTA_X = 0.855;
    private static final double AULA_LA1_PORTA_Y = 0.84;
    // Energia persa forzando la porta dell'Aula B (via di riserva se l'addetto non aiuta).
    private static final int COSTO_FORZA_PORTA_AULA_B = 30;

    private final RegiaEsplorazione view;
    private final GameState stato;
    private final GameEngine engine;
    private final double larghezzaMappa;
    private final double altezzaMappa;

    // Porte del cortile (capitolo 1): la porta del Polo A nasconde l'enigma del
    // laboratorio (si rivela solo a enigma sbloccato), la porta del Polo B resta
    // sempre inaccessibile. Nulle nelle scene che non le prevedono.
    private ElementoScena portaPoloA;
    private ElementoScena portaPoloB;

    /**
     * @param view           la schermata di esplorazione che offre i servizi di scena
     * @param stato          lo stato di gioco (giocatore, inventario, diario, flag)
     * @param engine         il motore di gioco (game over, avanzamento)
     * @param larghezzaMappa larghezza della mappa, per posizionare le porte
     * @param altezzaMappa   altezza della mappa, per posizionare le porte
     */
    Porte(RegiaEsplorazione view, GameState stato, GameEngine engine,
          double larghezzaMappa, double altezzaMappa) {
        this.view = Objects.requireNonNull(view, "La schermata non puo' essere nulla.");
        this.stato = Objects.requireNonNull(stato, "Lo stato non puo' essere nullo.");
        this.engine = Objects.requireNonNull(engine, "Il motore non puo' essere nullo.");
        this.larghezzaMappa = larghezzaMappa;
        this.altezzaMappa = altezzaMappa;
    }

    /** Azzera i riferimenti alle porte del cortile alla ricostruzione della scena. */
    void reset() {
        portaPoloA = null;
        portaPoloB = null;
    }

    /**
     * Allestisce, se la scena corrente lo prevede, l'uscita come porta invisibile
     * sulla facciata anziché come elemento-uscita visibile. Riguarda le scene del
     * capitolo 3 con uscite "a porta": l'aula LA1 (porta in basso a destra verso il
     * cortile) e il cortile (Polo A come uscita diretta, Polo B sbarrato).
     *
     * @param transizione l'uscita da allestire
     * @return {@code true} se la porta è stata gestita qui, {@code false} se l'uscita
     *         va resa come elemento generico dalla schermata
     */
    boolean allestisciUscita(Transition transizione) {
        if (isAulaLa1Capitolo3()) {
            // L'uscita non è un elemento visibile ma la porta in basso a destra, a cui
            // avvicinarsi per tornare nel cortile.
            aggiungiPortaUscita(transizione, AULA_LA1_PORTA_X, AULA_LA1_PORTA_Y);
            return true;
        }
        if (isCortileCapitolo3()) {
            // Anche le uscite del cortile sono porte invisibili sulle facciate: il Polo A
            // (rientro in aula LA1) a sinistra è un'uscita diretta; il Polo B (Aula B) a
            // destra è sbarrato e si apre solo se l'addetto ha aiutato, altrimenti va forzato.
            if ("aula_la1".equals(transizione.idDestinazione())) {
                aggiungiPortaUscita(transizione, CORTILE_PORTA_A_X, CORTILE_PORTE_Y);
            } else {
                aggiungiPortaPoloB(transizione);
            }
            return true;
        }
        return false;
    }

    /**
     * @return il punto di comparsa "davanti alla porta" per le scene che lo prevedono
     *         — il cortile del capitolo 3 (davanti al Polo A, da cui si esce) e l'aula
     *         LA1 di qualunque capitolo (davanti alla porta verso il cortile) — oppure
     *         vuoto se la scena non comporta una comparsa legata a una porta
     */
    Optional<SceneEnvironment.Punto> comparsaScenaCorrente() {
        if (isCortileCapitolo3()) {
            // Si esce dal Polo A: il giocatore compare davanti alla sua porta.
            return Optional.of(new SceneEnvironment.Punto(CORTILE_PORTA_A_X, CORTILE_PORTE_Y + 0.06));
        }
        if ("aula_la1".equals(engine.getScenaCorrente().getId())) {
            // Entrando in aula LA1 (in qualsiasi capitolo) si compare davanti alla porta
            // in basso a destra, dove si trova l'uscita verso il cortile. L'avvio del
            // capitolo 3 fa eccezione (posizione conservata a monte dalla schermata).
            return Optional.of(new SceneEnvironment.Punto(AULA_LA1_PORTA_X, AULA_LA1_PORTA_Y));
        }
        return Optional.empty();
    }

    private boolean isAulaLa1Capitolo3() {
        return "aula_la1".equals(engine.getScenaCorrente().getId())
                && "capitolo3".equals(engine.getCapitoloCorrente().getId());
    }

    private boolean isCortileCapitolo3() {
        return "cortile".equals(engine.getScenaCorrente().getId())
                && "capitolo3".equals(engine.getCapitoloCorrente().getId());
    }

    /**
     * Allestisce l'enigma della porta del laboratorio (capitolo 1): crea il proprio
     * {@link PortaLaboratorioPuzzle} e dispone la porta del Polo A (l'enigma vero e
     * proprio) affiancata da quella del Polo B, sempre inaccessibile. Concentrando qui
     * la creazione del puzzle col suo tipo concreto, la schermata di esplorazione non
     * deve conoscere quale enigma istanziare né effettuare conversioni di tipo.
     */
    void allestisciPortaLaboratorio() {
        PortaLaboratorioPuzzle puzzle = new PortaLaboratorioPuzzle();
        aggiungiPortaPoloA(puzzle);
        aggiungiPortaPoloB();
    }

    /**
     * Crea la porta del laboratorio (Polo A) come elemento-enigma a posizione fissa.
     * Resta un enigma a tutti gli effetti — blocca la conclusione del capitolo finché
     * non risolto — e il suo nodo è sempre invisibile (la porta è sullo sfondo): a
     * cambiare è solo l'interazione. Prima dello sblocco mostra un avviso; dopo aver
     * raccolto la chiave e ottenuto l'indizio su Alex Kaur apre l'enigma.
     */
    private void aggiungiPortaPoloA(Puzzle puzzle) {
        ElementoScena elemento = new ElementoScena(TipoElemento.ENIGMA, "",
                "Esamina la porta del Polo A", Color.web("#9b59b6"));
        elemento.puzzle = puzzle;
        elemento.posizioneFissa = true;
        elemento.azione = () -> {
            if (isEnigmaPortaSbloccato()) {
                view.mostraEnigma(puzzle, elemento);
            } else {
                view.mostraDialogo("", "Non puoi ancora entrare qui.");
            }
        };
        elemento.setVisibile(false);
        portaPoloA = elemento;
        view.registra(elemento);
    }

    /**
     * Crea la porta del Polo B: in questo capitolo è sempre inaccessibile, perciò
     * interagendovi si ottiene solo l'avviso. Non è un enigma e quindi non incide
     * sulla conclusione del capitolo.
     */
    private void aggiungiPortaPoloB() {
        ElementoScena elemento = new ElementoScena(TipoElemento.PORTA, "",
                "Esamina la porta del Polo B", Color.web("#9b59b6"));
        elemento.posizioneFissa = true;
        elemento.azione = () -> view.mostraDialogo("", "Non puoi ancora entrare qui.");
        elemento.setVisibile(false);
        portaPoloB = elemento;
        view.registra(elemento);
    }

    /**
     * @return {@code true} se la porta del laboratorio (Polo A) è sbloccata, ossia
     *         il giocatore ha raccolto la chiave e ottenuto l'indizio su Alex Kaur
     */
    boolean isEnigmaPortaSbloccato() {
        Player player = stato.getPlayer();
        return player.getInventario().contiene(ItemCatalog.ID_CHIAVE_CAPITOLO1)
                && player.getDiario().contiene(ClueCatalog.ID_ALEX_KAUR);
    }

    /**
     * Colloca le porte degli edifici sulle rispettive facciate (Polo A a sinistra,
     * Polo B a destra), appena davanti all'ingresso e fuori dai muri, così da
     * restare raggiungibili dal pavimento della piazza.
     */
    void posizionaCortile() {
        if (portaPoloA != null) {
            portaPoloA.posiziona(CORTILE_PORTA_A_X * larghezzaMappa, CORTILE_PORTE_Y * altezzaMappa);
        }
        if (portaPoloB != null) {
            portaPoloB.posiziona(CORTILE_PORTA_B_X * larghezzaMappa, CORTILE_PORTE_Y * altezzaMappa);
        }
    }

    /**
     * Aggiunge l'uscita come porta invisibile a posizione fissa: non c'è nulla di
     * visibile sulla mappa, ci si avvicina al punto della porta e con E si segue la
     * transizione. Usata per l'uscita dall'aula LA1 (capitolo 3) verso il cortile.
     *
     * @param transizione la transizione da seguire interagendo con la porta
     * @param fx          ascissa della porta in frazione della larghezza (0..1)
     * @param fy          ordinata della porta in frazione dell'altezza (0..1)
     */
    void aggiungiPortaUscita(Transition transizione, double fx, double fy) {
        ElementoScena elemento = new ElementoScena(TipoElemento.PORTA, "",
                transizione.etichetta(), Color.web("#2ecc71"));
        elemento.posizioneFissa = true;
        elemento.azione = () -> view.usaUscita(transizione);
        elemento.setVisibile(false);
        view.registra(elemento);
        elemento.posiziona(fx * larghezzaMappa, fy * altezzaMappa);
    }

    /**
     * Porta del Polo B nel cortile (capitolo 3): invisibile e a posizione fissa sulla
     * facciata. È sbarrata: si attraversa liberamente solo se l'addetto alle pulizie
     * ha aperto la porta laterale; altrimenti propone di forzarla a costo di energia.
     */
    void aggiungiPortaPoloB(Transition transizione) {
        ElementoScena elemento = new ElementoScena(TipoElemento.PORTA, "",
                transizione.etichetta(), Color.web("#2ecc71"));
        elemento.posizioneFissa = true;
        elemento.azione = () -> interagisciPortaPoloB(transizione);
        elemento.setVisibile(false);
        view.registra(elemento);
        elemento.posiziona(CORTILE_PORTA_B_X * larghezzaMappa, CORTILE_PORTE_Y * altezzaMappa);
    }

    /**
     * Interazione con la porta del Polo B: se l'addetto l'ha già aperta si entra
     * direttamente, altrimenti la porta risulta bloccata e si può scegliere di forzarla.
     */
    private void interagisciPortaPoloB(Transition transizione) {
        if (stato.hasFlag(ContentResolver.FLAG_PORTA_AULA_B)) {
            view.usaUscita(transizione);
            return;
        }
        view.mostraDialogo("", "La porta dell'aula risulta bloccata. Vuoi forzare la porta?",
                view::chiudiOverlay, List.of(
                        new OpzioneDialogo("Forza la porta", () -> forzaPortaPoloB(transizione)),
                        new OpzioneDialogo("Lascia stare", view::chiudiOverlay)));
    }

    /** Forza la porta del Polo B: penalità di energia, poi (se si sopravvive) si entra. */
    private void forzaPortaPoloB(Transition transizione) {
        stato.getPlayer().riduciEnergia(COSTO_FORZA_PORTA_AULA_B);
        view.aggiornaHud();
        if (engine.verificaGameOver()) {
            return;
        }
        view.chiudiOverlay();
        view.usaUscita(transizione);
    }
}
