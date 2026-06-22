package it.unicam.cs.mpgc.rpg126036.view;

import it.unicam.cs.mpgc.rpg126036.engine.GameEngine;
import it.unicam.cs.mpgc.rpg126036.engine.GameState;
import it.unicam.cs.mpgc.rpg126036.model.ClueCatalog;
import it.unicam.cs.mpgc.rpg126036.model.Player;
import it.unicam.cs.mpgc.rpg126036.model.StatType;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Trama dell'Aula B (capitolo 3): l'aula si esplora al buio con l'effetto torcia,
 * cercando le tracce dell'incontro. In base alle statistiche del giocatore offre
 * vie diverse, che possono coesistere:
 * <ul>
 *     <li>Investigazione &ge; 2: un luccichio rivela l'orologio rotto in fondo;</li>
 *     <li>Intuizione &ge; 2: un luccichio rivela il tessuto impigliato nella porta;</li>
 *     <li>nessuna delle due: resta solo la perquisizione "a tentoni", a costo di energia.</li>
 * </ul>
 * Trovata una traccia (l'indizio), la campagna prosegue verso l'epilogo.
 *
 * <p>Estratta dalla {@link ExplorationView} per isolare questa porzione di trama
 * cablata: usa i ruoli {@link ServiziTorcia} (buio), {@link ServiziMappa} (luccichii),
 * {@link ServiziDialoghi} (scoperte) e {@link ServiziProgressione} (HUD, potenziamento,
 * avanzamento), non l'intera regia (ISP).</p>
 */
final class AulaB {

    // XP per ogni indizio trovato con le scorciatoie (Investigazione/Intuizione)
    // e per la perquisizione "a tentoni", più l'energia spesa da quest'ultima.
    private static final int XP_INDIZIO = 60;
    private static final int XP_PERQUISIZIONE = 10;
    private static final int COSTO_PERQUISIZIONE = 40;
    // Pensieri e testi narrativi dell'Aula B.
    private static final String PENSIERO_LUCCICHIO =
            "Cos'è quel luccichio in fondo all'aula?";
    private static final String PENSIERO_CHIAVI =
            "Aspetta... nel cortile ho trovato le chiavi di casa di Antonio. Se l'assassino le ha "
                    + "rubate… deve averle prese dalla sua tasca dopo l'aggressione! Se fosse avvenuta "
                    + "qua dentro allora…";
    private static final String TESTO_OROLOGIO =
            "Sulla cattedra in fondo noti qualcosa: è un orologio da polso rotto, con il cinturino "
                    + "strappato. Sul retro c'è un'incisione con due lettere: «EM». Le stesse della mail.";
    private static final String TESTO_TESSUTO =
            "Esamini la porta. Un pezzo di tessuto strappato è rimasto impigliato.";
    private static final String PENSIERO_TESSUTO =
            "E se fosse un pezzo della maglia dell'assassino, scappato da qua?";
    private static final String TESTO_PERQUISIZIONE =
            "È buio totale e non vedi nulla. Vuoi perquisire l'aula a tentoni, fila per fila?";
    private static final String TESTO_PERQUISIZIONE_ESITO =
            "Dopo una lunga e affannosa ricerca, con la polizia ormai in arrivo, trovi comunque un "
                    + "orologio rotto a terra.";
    private static final String PENSIERO_PERQUISIZIONE =
            "Dietro c'è un'incisione: E.M. Ma sono le stesse iniziali della mail!";

    private final ServiziMappa mappa;
    private final ServiziTorcia torcia;
    private final ServiziDialoghi dialoghi;
    private final ServiziProgressione progressione;
    private final GameState stato;
    private final GameEngine engine;
    private final double larghezzaMappa;
    private final double altezzaMappa;
    // Luccichii degli indizi attualmente sulla mappa: man mano che vengono raccolti
    // si svuota; quando è vuota non resta nulla da trovare e la campagna si conclude.
    private final List<ElementoScena> luccichii = new ArrayList<>();

    /**
     * @param regia      la regia della scena, da cui si attingono i ruoli usati
     * @param stato      lo stato di gioco (giocatore, flag)
     * @param engine     il motore di gioco (indizi, game over, avanzamento)
     * @param dimensione le dimensioni della mappa, per posizionare gli elementi (non nulle)
     */
    AulaB(RegiaEsplorazione regia, GameState stato, GameEngine engine, DimensioneMappa dimensione) {
        Objects.requireNonNull(regia, "La regia non puo' essere nulla.");
        Objects.requireNonNull(dimensione, "Le dimensioni della mappa non possono essere nulle.");
        this.mappa = regia;
        this.torcia = regia;
        this.dialoghi = regia;
        this.progressione = regia;
        this.stato = Objects.requireNonNull(stato, "Lo stato non puo' essere nullo.");
        this.engine = Objects.requireNonNull(engine, "Il motore non puo' essere nullo.");
        this.larghezzaMappa = dimensione.larghezza();
        this.altezzaMappa = dimensione.altezza();
    }

    /**
     * Se la scena corrente è l'Aula B al buio, la allestisce: sovrappone l'effetto
     * torcia (buio col foro di luce attorno al giocatore) e, in base alle statistiche,
     * i luccichii degli indizi — l'orologio in fondo (Investigazione &ge; 2) e il
     * tessuto sulla porta (Intuizione &ge; 2), che possono coesistere. Senza statistiche
     * adatte resta solo la perquisizione. Nelle altre scene non fa nulla.
     */
    void allestisci() {
        if (!eAulaBAlBuio()) {
            return;
        }
        luccichii.clear();
        torcia.aggiungiTorcia();
        Player player = stato.getPlayer();
        boolean investigatore = player.getStatistica(StatType.INVESTIGAZIONE) >= 2;
        boolean intuitivo = player.getStatistica(StatType.INTUIZIONE) >= 2;
        // I luccichii sono aggiunti sopra la torcia: restano visibili anche al buio.
        if (investigatore) {
            // Orologio: sopra la cattedra (tavolo) in fondo al centro.
            ElementoScena[] rif = new ElementoScena[1];
            rif[0] = aggiungiLuccichio(0.50, 0.33, "Esamina il luccichio in fondo",
                    () -> trovaOrologio(rif[0]));
        }
        if (intuitivo) {
            // Tessuto: impigliato nella porta in basso al centro, accanto al giocatore.
            ElementoScena[] rif = new ElementoScena[1];
            rif[0] = aggiungiLuccichio(0.50, 0.88, "Esamina la porta",
                    () -> trovaTessuto(rif[0]));
        }
        if (!investigatore && !intuitivo) {
            // Nessuna scorciatoia: l'unica via è perquisire l'aula a tentoni.
            aggiungiPerquisizione();
        }
        torcia.aggiornaTorcia();
    }

    /**
     * All'ingresso nell'Aula B, dopo il cartello, fa scattare il pensiero adeguato
     * alle statistiche: la deduzione sull'Intuizione (chiavi rubate) ha priorità e
     * scatta anche quando convive con l'Investigazione; con la sola Investigazione si
     * nota il luccichio in fondo; senza nessuna delle due parte la perquisizione.
     */
    void forseIngresso() {
        if (!eAulaBAlBuio()) {
            return;
        }
        Player player = stato.getPlayer();
        boolean investigatore = player.getStatistica(StatType.INVESTIGAZIONE) >= 2;
        boolean intuitivo = player.getStatistica(StatType.INTUIZIONE) >= 2;
        if (intuitivo) {
            dialoghi.mostraDialogo(player.getNome(), PENSIERO_CHIAVI);
        } else if (investigatore) {
            dialoghi.mostraDialogo(player.getNome(), PENSIERO_LUCCICHIO);
        } else {
            mostraPerquisizione();
        }
    }

    /**
     * @return {@code true} se nell'Aula B al buio le uscite non vanno rese come
     *         elementi raggiungibili: l'avanzamento all'epilogo è innescato dal
     *         ritrovamento dell'indizio, non da un'uscita visibile
     */
    boolean nascondeUscite() {
        return eAulaBAlBuio();
    }

    /** @return {@code true} se la scena corrente è l'Aula B del capitolo 3 (al buio). */
    private boolean eAulaBAlBuio() {
        return "aula_b".equals(engine.getScenaCorrente().getId())
                && "capitolo3".equals(engine.getCapitoloCorrente().getId());
    }

    /**
     * Aggiunge un luccichio (punto d'interesse) all'Aula B: un piccolo cerchio
     * luminoso e pulsante, a posizione fissa, sopra la torcia così da restare
     * visibile al buio. Interagendovi si esegue {@code azione}.
     *
     * @return l'elemento creato, da rimuovere quando l'indizio è stato raccolto
     */
    private ElementoScena aggiungiLuccichio(double fx, double fy, String etichettaAzione, Runnable azione) {
        Circle luccichio = new Circle(7, Color.web("#fff3b0"));
        luccichio.setEffect(new Glow(1.0));
        // Pulsazione luminosa, per attirare lo sguardo nel buio.
        Timeline pulsazione = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(luccichio.opacityProperty(), 0.4)),
                new KeyFrame(Duration.seconds(0.75), new KeyValue(luccichio.opacityProperty(), 1.0)));
        pulsazione.setAutoReverse(true);
        pulsazione.setCycleCount(Animation.INDEFINITE);
        pulsazione.play();
        mappa.registraAnimazione(pulsazione);

        ElementoScena elemento = new ElementoScena(TipoElemento.OGGETTO, etichettaAzione, luccichio);
        elemento.posizioneFissa = true;
        elemento.azione = azione;
        mappa.registra(elemento);
        elemento.posiziona(fx * larghezzaMappa, fy * altezzaMappa);
        luccichii.add(elemento);
        return elemento;
    }

    /**
     * Aggiunge l'azione di perquisizione (per chi non ha scorciatoie): un punto
     * interattivo invisibile al centro dell'aula da cui avviare la ricerca a tentoni.
     */
    private void aggiungiPerquisizione() {
        ElementoScena elemento = new ElementoScena(TipoElemento.OGGETTO, "", "Perquisisci l'aula",
                Color.color(0, 0, 0, 0));
        elemento.posizioneFissa = true;
        elemento.azione = this::mostraPerquisizione;
        elemento.setVisibile(false);
        mappa.registra(elemento);
        elemento.posiziona(0.50 * larghezzaMappa, 0.55 * altezzaMappa);
    }

    /**
     * Ritrovamento dell'orologio (via Investigazione, 0 energia): mostra la scoperta,
     * registra l'indizio, assegna {@value #XP_INDIZIO} XP e toglie il luccichio.
     */
    private void trovaOrologio(ElementoScena luccichio) {
        dialoghi.mostraDialogo("", TESTO_OROLOGIO, () -> {
            engine.trovaIndizio(ClueCatalog.orologio());
            stato.getPlayer().aggiungiXp(XP_INDIZIO);
            progressione.aggiornaHud();
            rimuoviLuccichio(luccichio);
            progressione.mostraPotenziamentoSeDovuto(this::proseguiOFine);
        }, List.of());
    }

    /**
     * Ritrovamento del tessuto (via Intuizione, 0 energia): la scoperta, poi il
     * pensiero sull'assassino, infine indizio, {@value #XP_INDIZIO} XP e rimozione
     * del luccichio.
     */
    private void trovaTessuto(ElementoScena luccichio) {
        dialoghi.mostraDialogo("", TESTO_TESSUTO,
                () -> dialoghi.mostraDialogo(stato.getPlayer().getNome(), PENSIERO_TESSUTO, () -> {
                    engine.trovaIndizio(ClueCatalog.tessuto());
                    stato.getPlayer().aggiungiXp(XP_INDIZIO);
                    progressione.aggiornaHud();
                    rimuoviLuccichio(luccichio);
                    progressione.mostraPotenziamentoSeDovuto(this::proseguiOFine);
                }, List.of()), List.of());
    }

    /** Chiede se perquisire l'Aula B (via senza scorciatoie). */
    private void mostraPerquisizione() {
        dialoghi.mostraDialogo("", TESTO_PERQUISIZIONE, dialoghi::chiudiOverlay, List.of(
                new OpzioneDialogo("Perquisisci (-" + COSTO_PERQUISIZIONE + " energia)",
                        this::eseguiPerquisizione),
                new OpzioneDialogo("Non ora", dialoghi::chiudiOverlay)));
    }

    /**
     * Perquisizione a tentoni: costa {@value #COSTO_PERQUISIZIONE} di energia; se si
     * sopravvive si trova comunque l'orologio (indizio) e si ottengono
     * {@value #XP_PERQUISIZIONE} XP, poi la campagna si conclude.
     */
    private void eseguiPerquisizione() {
        stato.getPlayer().riduciEnergia(COSTO_PERQUISIZIONE);
        progressione.aggiornaHud();
        if (engine.verificaGameOver()) {
            return;
        }
        // Prima il ritrovamento a terra, poi lo stesso identico pensiero della via
        // Investigazione sull'orologio (incisione "EM", le iniziali della mail).
        dialoghi.mostraDialogo("", TESTO_PERQUISIZIONE_ESITO,
                () -> dialoghi.mostraDialogo(stato.getPlayer().getNome(), PENSIERO_PERQUISIZIONE, () -> {
                    engine.trovaIndizio(ClueCatalog.orologio());
                    stato.getPlayer().aggiungiXp(XP_PERQUISIZIONE);
                    progressione.aggiornaHud();
                    progressione.mostraPotenziamentoSeDovuto(() -> {
                        dialoghi.chiudiOverlay();
                        concludi();
                    });
                }, List.of()), List.of());
    }

    /** Rimuove un luccichio raccolto dalla mappa e dall'elenco di quelli da trovare. */
    private void rimuoviLuccichio(ElementoScena luccichio) {
        mappa.rimuoviElemento(luccichio);
        luccichii.remove(luccichio);
    }

    /**
     * Dopo aver raccolto un indizio: se restano altri luccichii (caso Investigazione
     * <i>e</i> Intuizione) si resta nell'aula per trovarli, altrimenti la campagna
     * si conclude.
     */
    private void proseguiOFine() {
        dialoghi.chiudiOverlay();
        if (luccichii.isEmpty()) {
            concludi();
        }
    }

    /**
     * Conclude la permanenza nell'Aula B avanzando alla scena terminale (epilogo):
     * il motore segnala la fine della campagna (TO BE CONTINUED).
     */
    private void concludi() {
        // Avanza all'epilogo (che fa scattare la fine partita) senza ricostruire la
        // vista: l'Aula B al buio resta sotto l'overlay di fine campagna.
        progressione.avanzaSenzaRicostruire("epilogo");
    }
}
