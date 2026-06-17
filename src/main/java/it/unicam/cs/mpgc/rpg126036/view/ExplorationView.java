package it.unicam.cs.mpgc.rpg126036.view;

import it.unicam.cs.mpgc.rpg126036.achievement.Achievement;
import it.unicam.cs.mpgc.rpg126036.achievement.AchievementManager;
import it.unicam.cs.mpgc.rpg126036.app.ContentResolver;
import it.unicam.cs.mpgc.rpg126036.app.GameSession;
import it.unicam.cs.mpgc.rpg126036.engine.GameEngine;
import it.unicam.cs.mpgc.rpg126036.engine.GameListener;
import it.unicam.cs.mpgc.rpg126036.engine.GameState;
import it.unicam.cs.mpgc.rpg126036.engine.GameSummary;
import it.unicam.cs.mpgc.rpg126036.interaction.InteractionResult;
import it.unicam.cs.mpgc.rpg126036.interaction.ItemInteraction;
import it.unicam.cs.mpgc.rpg126036.model.Chapter;
import it.unicam.cs.mpgc.rpg126036.model.ItemCatalog;
import it.unicam.cs.mpgc.rpg126036.model.Npc;
import it.unicam.cs.mpgc.rpg126036.model.PcVittimaPuzzle;
import it.unicam.cs.mpgc.rpg126036.model.Player;
import it.unicam.cs.mpgc.rpg126036.model.Puzzle;
import it.unicam.cs.mpgc.rpg126036.model.PuzzleOutcome;
import it.unicam.cs.mpgc.rpg126036.model.Scene;
import it.unicam.cs.mpgc.rpg126036.model.StatType;
import it.unicam.cs.mpgc.rpg126036.model.Transition;
import it.unicam.cs.mpgc.rpg126036.persistence.Campaign;
import it.unicam.cs.mpgc.rpg126036.persistence.SceneContents;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Schermata di esplorazione: il giocatore si muove liberamente nell'ambientazione
 * (vista dall'alto, stile GdR 2D) con i tasti <b>WASD</b>. Gli NPC, gli oggetti,
 * gli enigmi e le uscite della scena corrente sono posizionati sulla mappa;
 * avvicinandosi a uno di essi compare un suggerimento e premendo <b>E</b> si
 * interagisce. Il tasto <b>ESC</b> mette in pausa.
 *
 * <p>Registrata come {@link GameListener} sul motore (Observer), reagisce ai cambi
 * di scena ricostruendo gli elementi e mostra i pannelli in sovrimpressione per il
 * dialogo, la raccolta, gli enigmi, l'upgrade di statistica, la pausa, la fine del
 * capitolo, il game over e il completamento.</p>
 */
public class ExplorationView implements GameListener, RegiaEsplorazione {

    private static final double MAPPA_LARGHEZZA = 860;
    private static final double MAPPA_ALTEZZA = 440;
    // Altezza degli oggetti raccoglibili che hanno uno sprite dedicato.
    private static final double ALTEZZA_OGGETTO = 32;
    // Altezza degli NPC con sprite dedicato (poco meno del personaggio giocante).
    private static final double ALTEZZA_NPC = 58;
    // Le regole numeriche dell'enigma del PC della vittima (binario, soluzione, costi,
    // XP) vivono in PcVittimaPuzzle: la vista vi applica gli effetti, non li ridefinisce.
    // Posizione dell'addetto alle pulizie nel cortile (capitolo 3): davanti al Polo B,
    // di lato rispetto alla porta e un po' arretrato verso la facciata.
    private static final double POLO_B_NPC_X = 0.73;
    private static final double POLO_B_NPC_Y = 0.58;

    private final AppContext context;
    private final GameEngine engine;
    private final GameState stato;
    private final Campaign campaign;
    private final AchievementManager achievementManager;
    private final ContentResolver resolver;
    private final StackPane root;

    // Barra di stato (energia, XP, statistiche): costruzione e aggiornamento isolati.
    private final HudEsplorazione hud = new HudEsplorazione();
    // Titolo della scena (sopra la mappa) e suggerimento di interazione (in basso):
    // legati alla scena corrente, restano qui.
    private final Label titoloScena = new Label();
    private final Label suggerimento = new Label();

    // Mappa e movimento.
    private final Pane mappa = new Pane();
    // Avatar giocante: sprite, posizione, ciclo di gioco, input e rilevamento di
    // prossimita'. La schermata gli offre la lista degli elementi e il suggerimento
    // da aggiornare e ne riceve le interazioni (E/ESC) come azioni di confine.
    private final Personaggio personaggio;
    private final List<ElementoScena> elementi = new ArrayList<>();
    private final SceneEnvironment ambienti = new SceneEnvironment();
    // Porte degli edifici (Polo A/B del cortile e uscite a porta invisibili): la
    // trama delle porte vive qui, pilotata dalla scena che le offre i servizi.
    private final Porte porte;
    // Dialoghi con gli NPC (standard e su misura: tecnico, addetto, studente ubriaco).
    private final DialoghiNpc dialoghi;
    // Enigma del PC della vittima (capitolo 2): i sei PC, le vie di sblocco e la mail.
    private final PcVittima pcVittima;
    // Pensieri trasversali del protagonista (indagine, cortile, inizio capitolo).
    private final Pensieri pensieri;
    // Effetto torcia dell'Aula B (velo nero col foro attorno al giocatore), non nullo
    // solo mentre si esplora l'Aula B al buio. Aggiornato a ogni frame.
    private EffettoTorcia torcia;
    // Trama dell'Aula B al buio (capitolo 3): allestimento ed esito della ricerca
    // degli indizi. Pilotata dalla scena, che le offre i servizi di esplorazione.
    private final AulaB aulaB;
    // Animazioni della scena corrente (es. la pulsazione dei luccichii), fermate alla
    // ricostruzione della scena per non lasciarle attive a vuoto.
    private final List<Timeline> animazioniScena = new ArrayList<>();
    // Quando true, onSceneChanged non ricostruisce la vista: usato per le scene "di
    // servizio" (email_vittima, epilogo) raggiunte solo per far avanzare il motore.
    private boolean sopprimiRicostruzioneScena;
    // Quando true, la prossima ricostruzione di scena non riposiziona il giocatore al
    // punto di comparsa ma ne conserva la posizione: serve all'avvio del capitolo 3,
    // che riprende l'aula LA1 esattamente come si era concluso il capitolo 2.
    private boolean preservaPosizione;

    // Pannelli modali in sovrimpressione (dialoghi, messaggi, enigmi, pausa, fine
    // capitolo): meccanismo isolato che tiene un solo overlay e l'eventuale dialogo.
    private final GestoreOverlay gestoreOverlay;
    // Traguardo appena sbloccato e in attesa di essere annunciato col dialog box, al
    // termine dell'interazione che lo ha generato (come l'annuncio "Nuovo indizio").
    private Achievement traguardoPendente;

    public ExplorationView(AppContext context, GameSession session) {
        this.context = Objects.requireNonNull(context, "Il contesto non puo' essere nullo.");
        Objects.requireNonNull(session, "La sessione non puo' essere nulla.");
        this.engine = session.getEngine();
        this.stato = session.getStato();
        this.campaign = session.getCampaign();
        this.achievementManager = session.getAchievementManager();
        this.resolver = context.contentResolver();
        this.aulaB = new AulaB(this, stato, engine, MAPPA_LARGHEZZA, MAPPA_ALTEZZA);
        this.porte = new Porte(this, stato, engine, MAPPA_LARGHEZZA, MAPPA_ALTEZZA);
        this.dialoghi = new DialoghiNpc(this, stato, engine, resolver);
        this.pcVittima = new PcVittima(this, stato, engine, achievementManager, MAPPA_LARGHEZZA, MAPPA_ALTEZZA);
        this.pensieri = new Pensieri(this, stato, engine, porte);
        this.personaggio = new Personaggio(new CharacterSprite(stato.getPlayer().getClasse()),
                MAPPA_LARGHEZZA, MAPPA_ALTEZZA, elementi, suggerimento,
                this::puoMuoversi,
                this::aggiornaTorcia, this::interagisci, this::gestisciEscape);

        root = new StackPane(costruisciLayout());
        root.getStyleClass().add("screen-root");
        this.gestoreOverlay = new GestoreOverlay(root, personaggio::azzeraTasti);
        aggiungiPulsanteInventario();

        personaggio.collegaA(root);

        engine.addListener(this);
        achievementManager.addAchievementListener(t -> traguardoPendente = t);
        costruisciElementiScena();
        aggiornaHud();
    }

    // ----------------------------------------------------------------------
    // Layout
    // ----------------------------------------------------------------------

    private BorderPane costruisciLayout() {
        BorderPane radice = new BorderPane();
        radice.setTop(hud.costruisci());
        radice.setCenter(costruisciMappa());
        radice.setBottom(costruisciBarraInferiore());
        return radice;
    }

    private Node costruisciMappa() {
        mappa.getChildren().add(personaggio.nodo());
        mappa.getStyleClass().add("map-pane");
        mappa.setPrefSize(MAPPA_LARGHEZZA, MAPPA_ALTEZZA);
        mappa.setMaxSize(MAPPA_LARGHEZZA, MAPPA_ALTEZZA);

        titoloScena.getStyleClass().addAll("scene-title", "pixel-font");
        VBox contenitore = new VBox(8, titoloScena, mappa);
        contenitore.setAlignment(Pos.CENTER);
        contenitore.setPadding(new Insets(12));
        return contenitore;
    }

    private Node costruisciBarraInferiore() {
        suggerimento.getStyleClass().add("hud-text");
        suggerimento.setMinHeight(20);

        Label aiuto = new Label("Muoviti con W A S D · E interagisci · ESC pausa");
        aiuto.getStyleClass().add("hud-text");

        Button menu = new Button("Esci al menu");
        menu.getStyleClass().add("game-button");
        menu.setOnAction(e -> vaiAlMenu());

        Region spazio = new Region();
        HBox.setHgrow(spazio, Priority.ALWAYS);
        HBox barra = new HBox(12, aiuto, spazio, suggerimento, menu);
        barra.setAlignment(Pos.CENTER);
        barra.setPadding(new Insets(12, 24, 20, 24));
        return barra;
    }

    // ----------------------------------------------------------------------
    // Elementi della scena
    // ----------------------------------------------------------------------

    /**
     * Ricostruisce gli elementi interattivi (NPC, oggetti, enigmi, uscite) della
     * scena corrente leggendo i contenuti dalla campagna, risolvendoli in istanze
     * e disponendoli sulla mappa. Riporta il personaggio al centro.
     */
    private void costruisciElementiScena() {
        elementi.clear();
        personaggio.azzeraMuri();
        mappa.getChildren().clear();
        porte.reset();
        torcia = null;
        animazioniScena.forEach(Timeline::stop);
        animazioniScena.clear();

        Scene scena = engine.getScenaCorrente();
        titoloScena.setText(scena.getTitolo());

        String idCapitolo = engine.getCapitoloCorrente().getId();
        SceneContents contenuti = campaign.contenutiDi(idCapitolo, scena.getId());

        // Scena puramente narrativa (terminale e senza elementi interattivi, come la
        // schermata dell'email a fine capitolo 2): la mostriamo come overlay a schermo
        // invece di una stanza esplorabile vuota. La fine partita resta esclusa: la
        // scena finale e' gestita da onGameCompleted.
        if (isScenaNarrativa(scena, contenuti)) {
            mostraOverlayNarrativo(scena);
            return;
        }

        // Sfondo, ostacoli e punto di comparsa dipendono dall'ambiente della scena.
        // Lo sfondo va aggiunto come primo figlio (sotto personaggio ed elementi).
        Optional<SceneEnvironment.Ambiente> ambiente = ambienti.di(scena.getId());
        ambiente.ifPresent(this::applicaAmbiente);

        mappa.getChildren().add(personaggio.nodo());

        for (String idNpc : contenuti.npc()) {
            resolver.npc(idNpc).ifPresent(this::aggiungiNpc);
        }
        for (String idOggetto : contenuti.oggetti()) {
            resolver.item(idOggetto).ifPresent(item -> aggiungiOggetto(new ItemInteraction(item)));
        }
        for (String idEnigma : contenuti.enigmi()) {
            resolver.creaEnigma(idEnigma, stato)
                    .ifPresent(puzzle -> aggiungiEnigma(idEnigma, puzzle));
        }
        // Alcune scene non rendono le uscite come elementi raggiungibili: l'aula della
        // mail (cap. 2) avanza aprendo la mail sul PC, l'Aula B al buio (cap. 3) avanza
        // trovando l'indizio. Negli altri casi le porte invisibili del cortile e
        // dell'aula LA1 (cap. 3) sono allestite dalle Porte; il resto è un'uscita generica.
        if (!aulaB.nascondeUscite() && !pcVittima.nascondeUscite()) {
            for (Transition transizione : engine.transizioniDisponibili()) {
                if (!porte.allestisciUscita(transizione)) {
                    aggiungiUscita(transizione);
                }
            }
        }

        disponiElementi(ambiente.orElse(null));
        // All'avvio del capitolo 3 il giocatore resta dov'era a fine capitolo 2
        // (davanti al PC); negli altri casi compare al punto di spawn della scena.
        if (preservaPosizione) {
            preservaPosizione = false;
            personaggio.aggiornaSprite();
        } else {
            posizionaPersonaggioIniziale(ambiente.orElse(null));
        }
        // Le porte del cortile hanno posizione fissa sulle facciate degli edifici:
        // vanno collocate dopo la disposizione a slot.
        porte.posizionaCortile();
        // L'Aula B (al buio) aggiunge torcia e luccichii sopra il resto; altrove non fa nulla.
        aulaB.allestisci();
    }

    /**
     * Applica l'ambiente della scena: inserisce l'immagine di sfondo (dietro a
     * tutto) e converte i muri normalizzati in rettangoli di collisione in pixel.
     */
    private void applicaAmbiente(SceneEnvironment.Ambiente ambiente) {
        if (ambiente.sfondo() != null) {
            InputStream risorsa = getClass().getResourceAsStream(ambiente.sfondo());
            if (risorsa != null) {
                // Sfondo come nodo figlio (non setBackground sul Pane, che il CSS
                // .map-pane sovrascriverebbe). Riempie esattamente la mappa, così
                // l'immagine combacia con le coordinate normalizzate dei muri.
                ImageView sfondo = new ImageView(new Image(risorsa));
                sfondo.setFitWidth(MAPPA_LARGHEZZA);
                sfondo.setFitHeight(MAPPA_ALTEZZA);
                mappa.getChildren().add(sfondo);
            }
        }
        for (SceneEnvironment.Muro m : ambiente.muri()) {
            personaggio.aggiungiMuro(new Rectangle2D(m.x() * MAPPA_LARGHEZZA, m.y() * MAPPA_ALTEZZA,
                    m.w() * MAPPA_LARGHEZZA, m.h() * MAPPA_ALTEZZA));
        }
    }

    private void posizionaPersonaggioIniziale(SceneEnvironment.Ambiente ambiente) {
        double x;
        double y;
        // Le scene con un'uscita "a porta" fanno comparire il giocatore davanti ad essa
        // (il punto lo conoscono le Porte); altrimenti vale lo spawn dell'ambiente, o il
        // centro-basso della mappa astratta se la scena non ha un ambiente registrato.
        Optional<SceneEnvironment.Punto> davantiAPorta = porte.comparsaScenaCorrente();
        if (davantiAPorta.isPresent()) {
            x = davantiAPorta.get().x() * MAPPA_LARGHEZZA - Personaggio.LATO / 2;
            y = davantiAPorta.get().y() * MAPPA_ALTEZZA - Personaggio.LATO / 2;
        } else if (ambiente != null) {
            x = ambiente.spawnX() * MAPPA_LARGHEZZA - Personaggio.LATO / 2;
            y = ambiente.spawnY() * MAPPA_ALTEZZA - Personaggio.LATO / 2;
        } else {
            x = (MAPPA_LARGHEZZA - Personaggio.LATO) / 2;
            y = MAPPA_ALTEZZA - Personaggio.LATO - 8;
        }
        personaggio.posiziona(x, y);
    }

    private void aggiungiNpc(Npc npc) {
        String etichettaAzione = "Parla con " + npc.getNome();
        ImageView sprite = caricaSprite("/images/sprite/npc/" + npc.getId() + ".png", ALTEZZA_NPC);
        // Con lo sprite dedicato l'NPC si mostra come immagine, senza il nome sotto;
        // senza sprite si ripiega sul segnaposto colorato con nome.
        ElementoScena e = (sprite != null)
                ? new ElementoScena(TipoElemento.NPC, etichettaAzione, sprite)
                : new ElementoScena(TipoElemento.NPC, npc.getNome(), etichettaAzione, Color.web("#4a90d9"));
        // Alcuni NPC hanno un'interazione su misura: lo studente ubriaco (recupero
        // energia col Carisma e scelte) e il tecnico (scambio a più battute); gli
        // altri usano il dialogo standard.
        String id = npc.getId();
        if ("studente_ubriaco".equals(id)) {
            e.azione = () -> dialoghi.conStudenteUbriaco(npc);
        } else if ("tecnico_laboratorio".equals(id)) {
            e.azione = () -> dialoghi.conTecnico(npc);
        } else if ("addetto_pulizie".equals(id)) {
            e.azione = () -> dialoghi.conAddetto(npc);
        } else {
            e.azione = () -> dialoghi.conNpc(npc);
        }
        registra(e);
        // L'addetto alle pulizie del cortile (capitolo 3) ha posizione fissa accanto
        // al Polo B, fuori dalla disposizione automatica a slot.
        if ("addetto_pulizie".equals(id)) {
            e.posizioneFissa = true;
            e.posiziona(POLO_B_NPC_X * MAPPA_LARGHEZZA, POLO_B_NPC_Y * MAPPA_ALTEZZA);
        }
    }

    private void aggiungiOggetto(ItemInteraction oggetto) {
        var item = oggetto.getItem();
        // La chiave del capitolo 1 è ancora anonima al ritrovamento: l'etichetta non
        // ne svela il proprietario (lo si scopre col pensiero subito dopo la raccolta).
        String etichettaAzione = ItemCatalog.ID_CHIAVE_CAPITOLO1.equals(item.id())
                ? "Raccogli la chiave insanguinata"
                : "Raccogli " + item.nome();
        ImageView sprite = caricaSprite("/images/sprite/oggetti/" + item.id() + ".png", ALTEZZA_OGGETTO);
        // Con lo sprite dedicato l'oggetto si mostra come immagine, senza il nome
        // sotto; senza sprite si ripiega sul vecchio segnaposto colorato con nome.
        ElementoScena e = (sprite != null)
                ? new ElementoScena(TipoElemento.OGGETTO, etichettaAzione, sprite)
                : new ElementoScena(TipoElemento.OGGETTO, item.nome(), etichettaAzione, Color.web("#e0c43a"));
        e.azione = () -> raccogliOggetto(oggetto, e);
        registra(e);
    }

    /**
     * Carica uno sprite dalle risorse, se presente, dimensionandolo all'altezza
     * indicata (larghezza proporzionale, bordi netti per la pixel art).
     *
     * @param percorso percorso classpath dello sprite
     * @param altezza  altezza desiderata in pixel
     * @return l'ImageView pronto, oppure {@code null} se la risorsa non esiste
     */
    private ImageView caricaSprite(String percorso, double altezza) {
        InputStream risorsa = getClass().getResourceAsStream(percorso);
        if (risorsa == null) {
            return null;
        }
        ImageView vista = new ImageView(new Image(risorsa));
        vista.setFitHeight(altezza);
        vista.setPreserveRatio(true);
        vista.setSmooth(false);
        return vista;
    }

    private void aggiungiEnigma(String idEnigma, Puzzle puzzle) {
        // L'enigma del laboratorio (porta del Polo A) è una porta a comparsa:
        // visibile e risolvibile solo dopo aver raccolto la chiave e ottenuto
        // l'indizio su Alex Kaur. Lo affianca la porta — sempre bloccata — del Polo B.
        if ("porta_laboratorio".equals(idEnigma)) {
            porte.aggiungiPortaPoloA(puzzle);
            porte.aggiungiPortaPoloB();
            return;
        }
        // Il PC della vittima è uno dei sei computer del laboratorio: il PC giusto
        // apre direttamente la mail, gli altri cinque sono "PC sbagliati".
        if ("pc_vittima".equals(idEnigma)) {
            pcVittima.aggiungi((PcVittimaPuzzle) puzzle);
            return;
        }
        ElementoScena e = new ElementoScena(TipoElemento.ENIGMA, "Enigma", "Esamina l'enigma", Color.web("#9b59b6"));
        e.puzzle = puzzle;
        e.azione = () -> mostraEnigma(puzzle, e);
        registra(e);
    }

    @Override
    public void forsePensieroIndagine() {
        pensieri.forseIndagine();
    }

    private void aggiungiUscita(Transition transizione) {
        ElementoScena e = new ElementoScena(TipoElemento.USCITA, transizione.etichetta(),
                transizione.etichetta(), Color.web("#2ecc71"));
        e.azione = () -> usaUscita(transizione);
        registra(e);
    }

    @Override
    public void registra(ElementoScena e) {
        elementi.add(e);
        mappa.getChildren().add(e.nodo);
        if (e.etichettaNodo != null) {
            mappa.getChildren().add(e.etichettaNodo);
        }
    }

    /**
     * Posiziona gli elementi sugli slot dell'ambiente, se definiti (sul pavimento,
     * lontano dai muri); altrimenti ripiega sulla disposizione automatica a fasce.
     */
    /**
     * @return {@code true} se la scena va presentata come schermata narrativa
     *         (overlay) anziché come stanza esplorabile: scena terminale, priva di
     *         elementi interattivi e che non coincide con la fine della partita.
     */
    private boolean isScenaNarrativa(Scene scena, SceneContents contenuti) {
        return scena.isTerminale()
                && contenuti.npc().isEmpty()
                && contenuti.oggetti().isEmpty()
                && contenuti.enigmi().isEmpty()
                && !engine.isPartitaTerminata();
    }

    // ----------------------------------------------------------------------
    // Effetto torcia dell'Aula B (capitolo 3)
    // ----------------------------------------------------------------------

    /**
     * Aggiunge il velo della torcia sopra lo scenario e il personaggio: un foro di
     * luce lo rende trasparente solo attorno al giocatore, lasciando il resto
     * dell'aula al buio. Invocato dalla trama dell'{@link AulaB}.
     */
    @Override
    public void aggiungiTorcia() {
        torcia = new EffettoTorcia(MAPPA_LARGHEZZA, MAPPA_ALTEZZA);
        mappa.getChildren().add(torcia.nodo());
    }

    /** Ricalcola il foro di luce della torcia centrandolo sul giocatore. */
    @Override
    public void aggiornaTorcia() {
        if (torcia != null) {
            torcia.centraSu(personaggio.centroX(), personaggio.centroY());
        }
    }

    /** Registra un'animazione di scena, da fermare alla ricostruzione della stessa. */
    @Override
    public void registraAnimazione(Timeline animazione) {
        animazioniScena.add(animazione);
    }

    private void disponiElementi(SceneEnvironment.Ambiente ambiente) {
        // Gli elementi a posizione fissa (le porte degli edifici) sono collocati a
        // parte: qui si dispongono solo gli altri, su slot o a fasce.
        List<ElementoScena> daDisporre = new ArrayList<>();
        for (ElementoScena e : elementi) {
            if (!e.posizioneFissa) {
                daDisporre.add(e);
            }
        }
        if (ambiente != null && !ambiente.slot().isEmpty()) {
            disponiSuSlot(daDisporre, ambiente.slot());
        } else {
            disponiAFasce(daDisporre);
        }
    }

    private void disponiSuSlot(List<ElementoScena> daDisporre, List<SceneEnvironment.Punto> slot) {
        for (int i = 0; i < daDisporre.size(); i++) {
            if (i < slot.size()) {
                daDisporre.get(i).posiziona(slot.get(i).x() * MAPPA_LARGHEZZA, slot.get(i).y() * MAPPA_ALTEZZA);
            } else {
                // Piu' elementi che slot: fallback in basso, distribuiti in larghezza.
                int extra = i - slot.size();
                double x = MAPPA_LARGHEZZA * (extra + 1.0) / (daDisporre.size() - slot.size() + 1);
                daDisporre.get(i).posiziona(x, MAPPA_ALTEZZA - 48);
            }
        }
    }

    /**
     * Dispone gli elementi su tre fasce orizzontali (NPC in alto, oggetti ed enigmi
     * al centro, uscite in basso), distribuendoli uniformemente in larghezza.
     */
    private void disponiAFasce(List<ElementoScena> daDisporre) {
        List<ElementoScena> npc = new ArrayList<>();
        List<ElementoScena> centro = new ArrayList<>();
        List<ElementoScena> uscite = new ArrayList<>();
        for (ElementoScena e : daDisporre) {
            switch (e.tipo) {
                case NPC -> npc.add(e);
                case OGGETTO, ENIGMA -> centro.add(e);
                case USCITA -> uscite.add(e);
                case PORTA -> { /* le porte hanno posizione fissa, non a fasce */ }
            }
        }
        disponiFascia(npc, 64);
        disponiFascia(centro, MAPPA_ALTEZZA / 2);
        disponiFascia(uscite, MAPPA_ALTEZZA - 64);
    }

    private void disponiFascia(List<ElementoScena> fascia, double y) {
        for (int i = 0; i < fascia.size(); i++) {
            double x = MAPPA_LARGHEZZA * (i + 1) / (fascia.size() + 1);
            fascia.get(i).posiziona(x, y);
        }
    }

    // ----------------------------------------------------------------------
    // Input: interazione e pausa (il movimento vive nel Personaggio)
    // ----------------------------------------------------------------------

    /**
     * Reazione al tasto E, delegata dal {@link Personaggio}: con un dialogo aperto
     * lo fa avanzare (completa il testo o lo chiude); altrimenti, se nulla è in
     * sovrimpressione e fuori dalla pausa, attiva l'elemento più vicino.
     */
    /**
     * @return {@code true} se il personaggio può muoversi: nessun overlay aperto e
     *         gioco non in pausa. Interrogato dal ciclo di gioco del {@link Personaggio}.
     */
    private boolean puoMuoversi() {
        return !gestoreOverlay.isAperto() && !engine.isInPausa();
    }

    private void interagisci() {
        if (gestoreOverlay.avanzaDialogoSePresente()) {
            return;
        }
        ElementoScena vicino = personaggio.vicino();
        if (puoMuoversi() && vicino != null) {
            vicino.azione.run();
        }
    }

    private void gestisciEscape() {
        if (engine.isInPausa()) {
            engine.riprendi();
        } else if (gestoreOverlay.isAperto()) {
            if (gestoreOverlay.isChiudibile()) {
                chiudiOverlay();
            }
        } else {
            engine.pausa();
        }
    }

    // ----------------------------------------------------------------------
    // Interazioni
    // ----------------------------------------------------------------------

    private void raccogliOggetto(ItemInteraction oggetto, ElementoScena elemento) {
        InteractionResult esito = engine.raccogli(oggetto);
        rimuoviElemento(elemento);
        aggiornaHud();
        boolean eChiave = ItemCatalog.ID_CHIAVE_CAPITOLO1.equals(oggetto.getItem().id());
        // Coda comune dopo il messaggio di raccolta: l'eventuale annuncio di traguardo
        // (es. "Chiave insanguinata") e poi, se il giocatore ha sia la chiave sia
        // l'indizio, il pensiero che lo indirizza verso il PC di Antonio.
        Runnable coda = () -> annunciaTraguardoSePresente(this::forsePensieroIndagine);
        // Per la chiave, prima della coda si inserisce il pensiero di riconoscimento.
        Runnable dopoMessaggio = eChiave
                ? () -> {
                    chiudiOverlay();
                    mostraDialogo(stato.getPlayer().getNome(),
                            "Ma questo portachiavi lo riconosco... sono le chiavi di Antonio!",
                            () -> {
                                chiudiOverlay();
                                coda.run();
                            }, List.of());
                }
                : () -> {
                    chiudiOverlay();
                    coda.run();
                };
        String testoRaccolta = eChiave ? "Hai raccolto una chiave insanguinata." : esito.messaggio();
        mostraMessaggio("Oggetto", testoRaccolta, dopoMessaggio);
    }

    @Override
    public void usaUscita(Transition transizione) {
        if (haEnigmaNonRisolto()) {
            mostraMessaggio("Passaggio bloccato",
                    "Un enigma sbarra ancora la strada: risolvilo prima di proseguire.");
            return;
        }
        engine.avanza(transizione.idDestinazione());
        // Dopo lo spostamento ripresenta il cartello con il titolo della nuova scena,
        // come a inizio capitolo (es. "Cortile del Polo di Informatica", "Aula LA1").
        mostraCartelloScenaCorrente();
    }

    /**
     * Mostra il cartello (schermata nera con la scritta) del titolo della scena
     * corrente e poi torna all'esplorazione. Si applica solo alle scene esplorabili:
     * le scene narrative/terminali e la fine partita hanno già il proprio overlay,
     * perciò in quel caso il cartello viene saltato.
     */
    private void mostraCartelloScenaCorrente() {
        if (engine.isPartitaTerminata() || gestoreOverlay.isAperto()) {
            return;
        }
        String titolo = engine.getScenaCorrente().getTitolo();
        context.navigator().mostra(new ChapterTitleView(context, titolo, () -> {
            context.navigator().mostra(root);
            pensieri.forseCortile();
            aulaB.forseIngresso();
        }).getRoot());
    }

    private boolean haEnigmaNonRisolto() {
        return elementi.stream()
                .anyMatch(e -> e.tipo == TipoElemento.ENIGMA && e.puzzle != null && !e.puzzle.isRisolto());
    }

    // ----------------------------------------------------------------------
    // Pannelli in sovrimpressione
    // ----------------------------------------------------------------------

    private void mostraMessaggio(String titolo, String corpo) {
        mostraMessaggio(titolo, corpo, this::chiudiOverlay);
    }

    // ----------------------------------------------------------------------
    // Inventario (libro): indizi, prove e obiettivi
    // ----------------------------------------------------------------------

    /**
     * Aggiunge in basso a destra la piccola icona a forma di libro che apre
     * l'inventario. Resta sotto agli eventuali overlay (dialoghi, enigmi, pausa),
     * così è cliccabile solo durante la libera esplorazione.
     */
    private void aggiungiPulsanteInventario() {
        Button libro = new Button("📖");
        libro.getStyleClass().add("book-button");
        libro.setFocusTraversable(false);
        libro.setOnAction(e -> {
            if (puoMuoversi()) {
                mostraInventario();
            }
        });
        StackPane.setAlignment(libro, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(libro, new Insets(0, 24, 72, 0));
        root.getChildren().add(libro);
    }

    /**
     * Apre l'inventario come overlay con tre sezioni selezionabili: "Indizi"
     * (il diario), "Prove" (gli oggetti raccolti) e "Obiettivi" (i traguardi).
     */
    private void mostraInventario() {
        VBox pannello = PannelloInventario.crea(stato, achievementManager, this::chiudiOverlay);
        mostraOverlay(velo(pannello), true);
    }

    /**
     * Variante con azione personalizzata alla chiusura (il pulsante "Chiudi"): utile
     * per concatenare al messaggio un dialogo successivo, come il pensiero d'indagine.
     */
    private void mostraMessaggio(String titolo, String corpo, Runnable allaChiusura) {
        Label etichettaTitolo = new Label(titolo);
        etichettaTitolo.getStyleClass().add("scene-title");
        etichettaTitolo.setTextAlignment(TextAlignment.CENTER);
        Label etichettaCorpo = new Label(corpo);
        etichettaCorpo.getStyleClass().add("overlay-subtitle");
        etichettaCorpo.setWrapText(true);
        etichettaCorpo.setMaxWidth(560);
        etichettaCorpo.setTextAlignment(TextAlignment.CENTER);

        Button chiudi = new Button("Chiudi");
        chiudi.getStyleClass().add("game-button");
        chiudi.setOnAction(e -> allaChiusura.run());

        VBox pannello = new VBox(20, etichettaTitolo, etichettaCorpo, chiudi);
        pannello.setAlignment(Pos.CENTER);
        // Senza fillWidth ogni scritta è larga quanto il suo testo e il VBox la centra
        // come nodo: titolo e corpo restano centrati rispetto al centro dello schermo.
        pannello.setFillWidth(false);
        // Font pixel VT323, come nel resto del gioco (es. messaggio di raccolta chiave).
        pannello.getStyleClass().add("pixel-font");
        mostraOverlay(velo(pannello), true);
    }

    /**
     * Mostra una scena narrativa come overlay a schermo: titolo, testo e un pulsante
     * "Continua" che conclude il capitolo (scelta del potenziamento e avanzamento).
     */
    private void mostraOverlayNarrativo(Scene scena) {
        Label titolo = new Label(scena.getTitolo());
        titolo.getStyleClass().add("overlay-title");
        Label corpo = new Label(scena.getDescrizione());
        corpo.getStyleClass().add("overlay-subtitle");
        corpo.setWrapText(true);
        corpo.setMaxWidth(560);

        Button continua = new Button("Continua");
        continua.getStyleClass().add("game-button");
        continua.setOnAction(e -> {
            chiudiOverlay();
            verificaCompletamentoCapitolo();
        });

        VBox pannello = new VBox(24, titolo, corpo, continua);
        pannello.setAlignment(Pos.CENTER);
        // Non chiudibile con ESC: si prosegue solo dal pulsante.
        mostraOverlay(velo(pannello), false);
    }

    /**
     * Avanza alla scena indicata senza far ricostruire la vista: la schermata
     * corrente (sfondo, titolo) resta invariata. Serve per le scene "di servizio"
     * (email_vittima, epilogo) che esistono solo per far avanzare il motore mentre
     * il contenuto vero è mostrato come overlay.
     *
     * @param idScena id della scena di servizio verso cui avanzare
     */
    @Override
    public void avanzaSenzaRicostruire(String idScena) {
        sopprimiRicostruzioneScena = true;
        engine.avanza(idScena);
        sopprimiRicostruzioneScena = false;
    }

    /**
     * Mostra un dialogo NPC come finestra (dialog box) nella parte bassa dello
     * schermo, con il testo che compare un carattere alla volta (effetto macchina
     * da scrivere). Un clic o il tasto E completano subito il testo se è in corso,
     * altrimenti chiudono il dialogo; lo stesso fa ESC.
     */
    @Override
    public void mostraDialogo(String nome, String testo) {
        gestoreOverlay.mostraDialogo(nome, testo);
    }

    @Override
    public void mostraDialogo(String nome, String testo, Runnable alTermine, List<OpzioneDialogo> opzioni) {
        gestoreOverlay.mostraDialogo(nome, testo, alTermine, opzioni);
    }

    @Override
    public void mostraEnigma(Puzzle puzzle, ElementoScena elemento) {
        if (puzzle.isRisolto()) {
            mostraMessaggio("Enigma", "Hai già superato questo enigma.");
            return;
        }
        // Enigma a combinazione numerica (porta del laboratorio): tastierino a 4 cifre.
        mostraEnigmaTastierino(puzzle, elemento);
    }

    /**
     * Enigma a combinazione numerica (porta del laboratorio): tastierino a quattro
     * cifre, pulsante di forza bruta e, in base alla classe, il pensiero-indizio.
     */
    private void mostraEnigmaTastierino(Puzzle puzzle, ElementoScena elemento) {
        Player player = stato.getPlayer();

        Label etichettaTitolo = new Label("Tastierino numerico");
        etichettaTitolo.getStyleClass().add("scene-title");
        Label etichettaTesto = new Label(puzzle.getTesto());
        etichettaTesto.getStyleClass().add("overlay-subtitle");
        etichettaTesto.setWrapText(true);
        etichettaTesto.setMaxWidth(420);
        etichettaTesto.setTextAlignment(TextAlignment.CENTER);

        Label esito = new Label();
        esito.getStyleClass().add("overlay-subtitle");
        esito.setWrapText(true);
        esito.setMaxWidth(420);
        esito.setTextAlignment(TextAlignment.CENTER);

        VBox tastierino = TastierinoNumerico.crea(4, codice -> {
            PuzzleOutcome outcome = puzzle.tenta(player, codice);
            esito.setText(outcome.messaggio());
            if (outcome.risolto()) {
                enigmaRisolto(elemento);
                return true;
            }
            return false;
        });

        Button forza = new Button("Forza bruta (perdi energia)");
        forza.getStyleClass().add("game-button");
        forza.setOnAction(e -> {
            PuzzleOutcome outcome = puzzle.forzaBruta(player);
            esito.setText(outcome.messaggio());
            if (outcome.risolto()) {
                enigmaRisolto(elemento);
            }
        });

        VBox pannello = new VBox(16, etichettaTitolo, etichettaTesto, tastierino, forza, esito);
        pannello.setAlignment(Pos.CENTER);
        pannello.setMaxWidth(480);

        // Pensiero del giocatore in base alla classe: appare come dialog box in basso
        // e fornisce l'indizio (testo diverso per Investigazione, Intuizione o Carisma).
        List<String> pensieri = puzzle.suggerimentiPer(player);

        BorderPane contenuto = new BorderPane(pannello);
        if (!pensieri.isEmpty()) {
            Node boxPensiero = costruisciBoxPensiero(player.getNome(), String.join("\n\n", pensieri));
            contenuto.setBottom(boxPensiero);
            BorderPane.setMargin(boxPensiero, new Insets(0, 16, 16, 16));
        }

        StackPane velo = new StackPane(contenuto);
        velo.getStyleClass().addAll("overlay-veil", "pixel-font");
        mostraOverlay(velo, true);
    }

    /**
     * Costruisce un dialog box statico (nome + testo) nello stile delle finestre di
     * dialogo, usato per il pensiero del giocatore durante l'enigma.
     */
    private Node costruisciBoxPensiero(String nome, String testo) {
        Label etichettaNome = new Label(nome);
        etichettaNome.getStyleClass().add("dialog-name");
        Label etichettaTesto = new Label(testo);
        etichettaTesto.getStyleClass().add("dialog-text");
        etichettaTesto.setWrapText(true);
        etichettaTesto.setMaxWidth(Double.MAX_VALUE);

        BorderPane box = new BorderPane();
        box.getStyleClass().addAll("dialog-box", "pixel-font");
        box.setTop(etichettaNome);
        box.setCenter(etichettaTesto);
        BorderPane.setAlignment(etichettaTesto, Pos.TOP_LEFT);
        BorderPane.setMargin(etichettaTesto, new Insets(6, 0, 0, 0));
        box.setMaxWidth(Double.MAX_VALUE);
        return box;
    }

    /**
     * Aggiorna lo stato dopo la risoluzione di un enigma: gli XP e l'energia sono
     * gia' applicati dall'enigma, quindi vanno notificati al motore (game over e
     * upgrade), poi si verifica l'eventuale completamento del capitolo.
     */
    private void enigmaRisolto(ElementoScena elemento) {
        chiudiOverlay();
        rimuoviElemento(elemento);
        aggiornaHud();
        if (engine.verificaGameOver()) {
            return;
        }
        engine.verificaUpgradeDisponibile();
        if (!gestoreOverlay.isAperto()) {
            verificaCompletamentoCapitolo();
        }
    }

    @Override
    public void mostraSceltaUpgrade(String titolo, String sottotitolo, Consumer<StatType> azione) {
        Label etichettaTitolo = new Label(titolo);
        etichettaTitolo.getStyleClass().add("scene-title");
        Label etichettaSub = new Label(sottotitolo);
        etichettaSub.getStyleClass().add("overlay-subtitle");

        VBox opzioni = new VBox(12);
        opzioni.setAlignment(Pos.CENTER);
        VBox pannello = new VBox(20, etichettaTitolo, etichettaSub, opzioni);
        pannello.setAlignment(Pos.CENTER);
        // Font pixel VT323, come nel resto del gioco (schermata di potenziamento).
        pannello.getStyleClass().add("pixel-font");

        Player player = stato.getPlayer();
        for (StatType tipo : StatType.values()) {
            Button scelta = new Button(tipo.getIcona() + " " + tipo.getNomeVisualizzato()
                    + " (attuale: " + player.getStatistica(tipo) + ")");
            scelta.getStyleClass().add("game-button");
            scelta.setMaxWidth(360);
            scelta.setOnAction(e -> {
                chiudiOverlay();
                azione.accept(tipo);
                aggiornaHud();
            });
            opzioni.getChildren().add(scelta);
        }
        mostraOverlay(velo(pannello), false);
    }

    /**
     * Se il giocatore ha raggiunto la soglia di {@value Player#COSTO_XP_POTENZIAMENTO}
     * XP, mostra la schermata di scelta della statistica da potenziare (consumando i
     * 100 XP, così il contatore si riazzera) e ricontrolla per eventuali potenziamenti
     * a catena; al termine — o subito, se sotto soglia — esegue {@code dopo}.
     *
     * @param dopo azione da eseguire una volta gestiti gli eventuali potenziamenti
     */
    @Override
    public void mostraPotenziamentoSeDovuto(Runnable dopo) {
        if (stato.getPlayer().getXp() >= Player.COSTO_XP_POTENZIAMENTO) {
            mostraSceltaUpgrade("Potenziamento disponibile",
                    "Hai raggiunto 100 XP! Scegli una statistica da potenziare:",
                    statistica -> {
                        stato.getPlayer().potenziaStatistica(statistica);
                        aggiornaHud();
                        mostraPotenziamentoSeDovuto(dopo);
                    });
        } else if (dopo != null) {
            dopo.run();
        }
    }

    private void mostraOverlayFinale(String titolo, String sottotitolo) {
        Label etichettaTitolo = new Label(titolo);
        etichettaTitolo.getStyleClass().add("overlay-title");
        Label etichettaSub = new Label(sottotitolo);
        etichettaSub.getStyleClass().add("overlay-subtitle");
        etichettaSub.setWrapText(true);
        etichettaSub.setMaxWidth(520);
        // Testo centrato anche su più righe (altrimenti resta allineato a sinistra).
        etichettaSub.setTextAlignment(TextAlignment.CENTER);

        Button menu = new Button("Torna al menu principale");
        menu.getStyleClass().add("game-button");
        menu.setOnAction(e -> vaiAlMenu());

        VBox pannello = new VBox(24, etichettaTitolo, etichettaSub, menu);
        pannello.setAlignment(Pos.CENTER);
        // Senza fillWidth ogni scritta è larga quanto il suo testo e il VBox la
        // centra come nodo: titolo e sottotitolo restano allineati al centro.
        pannello.setFillWidth(false);
        // Font pixel VT323, come nel resto del gioco.
        pannello.getStyleClass().add("pixel-font");
        mostraOverlay(velo(pannello), false);
    }

    private void mostraPausa() {
        Label etichettaTitolo = new Label("PAUSA");
        etichettaTitolo.getStyleClass().add("overlay-title");

        Button riprendi = new Button("Riprendi");
        riprendi.getStyleClass().add("game-button");
        riprendi.setOnAction(e -> engine.riprendi());

        VBox pannello = new VBox(24, etichettaTitolo, riprendi);
        pannello.setAlignment(Pos.CENTER);
        mostraOverlay(velo(pannello), false);
    }

    @Override
    public StackPane velo(Node contenuto) {
        return gestoreOverlay.velo(contenuto);
    }

    @Override
    public void mostraOverlay(Node velo, boolean chiudibile) {
        gestoreOverlay.mostra(velo, chiudibile);
    }

    /**
     * Se un traguardo è stato appena sbloccato, lo annuncia con il dialog box
     * ("Obiettivo sbloccato: ..."), sullo stile dell'annuncio "Nuovo indizio";
     * alla chiusura esegue {@code dopo}. Se non c'è nulla da annunciare esegue
     * subito {@code dopo}. Così l'avviso non viene sovrascritto dai pannelli che
     * seguono lo sblocco (es. il dialogo del PC o il messaggio di raccolta).
     *
     * @param dopo azione da eseguire dopo l'eventuale annuncio (può essere nulla)
     */
    @Override
    public void annunciaTraguardoSePresente(Runnable dopo) {
        if (traguardoPendente == null) {
            if (dopo != null) {
                dopo.run();
            }
            return;
        }
        Achievement traguardo = traguardoPendente;
        traguardoPendente = null;
        mostraDialogo("", "🏆 Obiettivo sbloccato: " + traguardo.titolo(), () -> {
            chiudiOverlay();
            if (dopo != null) {
                dopo.run();
            }
        }, List.of());
    }

    @Override
    public void chiudiOverlay() {
        gestoreOverlay.chiudi();
    }

    // ----------------------------------------------------------------------
    // Reazione agli eventi del motore
    // ----------------------------------------------------------------------

    @Override
    public void aggiornaHud() {
        hud.aggiorna(engine.getEnergia(), stato.getPlayer());
    }

    @Override
    public void onSceneChanged(Scene scena) {
        // Le scene "di servizio" (email_vittima, epilogo) servono solo al motore per
        // completare il capitolo: non vanno mostrate, così la schermata corrente (e lo
        // sfondo già presente) resta invariata sotto gli overlay di potenziamento/fine.
        if (sopprimiRicostruzioneScena) {
            return;
        }
        costruisciElementiScena();
        aggiornaHud();
        verificaCompletamentoCapitolo();
    }

    /**
     * All'inizio di un nuovo capitolo ripresenta la sequenza di cartelli usata a
     * inizio partita: "Capitolo N" e il nome dell'ambientazione, poi l'esplorazione.
     * La scena corrente è già quella iniziale del nuovo capitolo; {@code onSceneChanged}
     * (notificato subito dopo) ne ricostruisce gli elementi sulla mappa, pronta a
     * ricomparire al termine dei cartelli.
     */
    @Override
    public void onChapterAdvanced(Chapter nuovo) {
        // Dal titolo "Capitolo N: ..." si estrae la sola dicitura "Capitolo N".
        // Il capitolo 3 riprende l'aula LA1 dov'era finito il capitolo 2: la
        // ricostruzione di scena (notificata subito dopo) non deve riposizionare il
        // giocatore al punto di spawn ma conservarne la posizione davanti al PC.
        if ("capitolo3".equals(nuovo.getId())) {
            preservaPosizione = true;
        }
        String etichettaCapitolo = nuovo.getTitolo().split(":", 2)[0].trim();
        String titoloScena = engine.getScenaCorrente().getTitolo();
        Runnable vaiAllEsplorazione = () -> {
            context.navigator().mostra(root);
            pensieri.forseInizioCapitolo(nuovo);
        };
        Runnable vaiAllaScena = () -> context.navigator().mostra(
                new ChapterTitleView(context, titoloScena, vaiAllEsplorazione).getRoot());
        context.navigator().mostra(
                new ChapterTitleView(context, etichettaCapitolo, vaiAllaScena).getRoot());
    }

    /**
     * Se la scena corrente e' terminale e nessun enigma la sbarra piu', conclude il
     * capitolo: per i capitoli intermedi propone la scelta del potenziamento e
     * avanza; per l'ultimo capitolo lascia che sia il motore a segnalare la fine.
     */
    private void verificaCompletamentoCapitolo() {
        // Un overlay aperto (es. la schermata dell'email) attende l'azione dell'utente:
        // non concludere il capitolo finché non viene chiuso col "Continua".
        if (gestoreOverlay.isAperto()) {
            return;
        }
        Scene scena = engine.getScenaCorrente();
        if (!scena.isTerminale() || haEnigmaNonRisolto() || engine.isPartitaTerminata()) {
            return;
        }
        if (engine.getCapitoloCorrente().isCompletato()) {
            mostraSceltaUpgrade("Capitolo completato",
                    "Scegli una statistica da potenziare prima del prossimo capitolo:",
                    engine::concludiCapitolo);
        }
    }

    @Override
    public void onUpgradeStatisticaDisponibile() {
        mostraSceltaUpgrade("Potenziamento disponibile",
                "Hai raggiunto 100 XP! Scegli una statistica da potenziare:", engine::applicaUpgrade);
    }

    @Override
    public void onPaused() {
        mostraPausa();
    }

    @Override
    public void onResumed() {
        chiudiOverlay();
    }

    @Override
    public void onGameOver() {
        mostraOverlayFinale("GAME OVER",
                "Hai esaurito la lucidità: la polizia ti ha trovato sulla scena.");
    }

    @Override
    public void onGameCompleted(GameSummary riepilogo) {
        String dettaglio = "Investigazione: " + riepilogo.statisticheFinali().get(StatType.INVESTIGAZIONE)
                + "   Carisma: " + riepilogo.statisticheFinali().get(StatType.CARISMA)
                + "   Intuizione: " + riepilogo.statisticheFinali().get(StatType.INTUIZIONE)
                + "\nXP totali: " + riepilogo.xpTotali();
        mostraOverlayFinale("TO BE CONTINUED", dettaglio);
    }

    // ----------------------------------------------------------------------
    // Utilita'
    // ----------------------------------------------------------------------

    @Override
    public void rimuoviElemento(ElementoScena elemento) {
        elementi.remove(elemento);
        mappa.getChildren().remove(elemento.nodo);
        if (elemento.etichettaNodo != null) {
            mappa.getChildren().remove(elemento.etichettaNodo);
        }
        personaggio.dimenticaSeVicino(elemento);
    }

    private void vaiAlMenu() {
        personaggio.fermaCiclo();
        engine.removeListener(this);
        context.navigator().mostra(new HomeView(context).getRoot());
    }

    /**
     * @return il nodo radice della schermata
     */
    public Parent getRoot() {
        return root;
    }

    // ----------------------------------------------------------------------
    // Modello interno di un elemento interattivo sulla mappa
    // ----------------------------------------------------------------------

}
