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
import it.unicam.cs.mpgc.rpg126036.model.ClueCatalog;
import it.unicam.cs.mpgc.rpg126036.model.ItemCatalog;
import it.unicam.cs.mpgc.rpg126036.model.Npc;
import it.unicam.cs.mpgc.rpg126036.model.NpcCatalog;
import it.unicam.cs.mpgc.rpg126036.model.PcVittimaPuzzle;
import it.unicam.cs.mpgc.rpg126036.model.Player;
import it.unicam.cs.mpgc.rpg126036.model.Puzzle;
import it.unicam.cs.mpgc.rpg126036.model.PuzzleOutcome;
import it.unicam.cs.mpgc.rpg126036.model.Scene;
import it.unicam.cs.mpgc.rpg126036.model.StatType;
import it.unicam.cs.mpgc.rpg126036.model.Transition;
import it.unicam.cs.mpgc.rpg126036.persistence.Campaign;
import it.unicam.cs.mpgc.rpg126036.persistence.SceneContents;
import javafx.animation.AnimationTimer;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
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
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
public class ExplorationView implements GameListener {

    private static final double MAPPA_LARGHEZZA = 860;
    private static final double MAPPA_ALTEZZA = 440;
    private static final double LATO_PERSONAGGIO = 28;
    // Altezza dello sprite a schermo: piu' alto del riquadro di collisione, che
    // resta ai piedi del personaggio (stile GdR 2D dall'alto).
    private static final double ALTEZZA_SPRITE = 60;
    private static final double VELOCITA = 3.0;
    private static final double RAGGIO_INTERAZIONE = 52;
    // Altezza degli oggetti raccoglibili che hanno uno sprite dedicato.
    private static final double ALTEZZA_OGGETTO = 32;
    // Altezza degli NPC con sprite dedicato (poco meno del personaggio giocante).
    private static final double ALTEZZA_NPC = 58;
    // Energia consumata a ogni dialogo con un NPC (regola di gioco).
    private static final int COSTO_ENERGIA_DIALOGO = 10;
    // Le regole numeriche dell'enigma del PC della vittima (binario, soluzione, costi,
    // XP) vivono in PcVittimaPuzzle: la vista vi applica gli effetti, non li ridefinisce.
    // Posizione dell'addetto alle pulizie nel cortile (capitolo 3): davanti al Polo B,
    // di lato rispetto alla porta e un po' arretrato verso la facciata.
    private static final double POLO_B_NPC_X = 0.73;
    private static final double POLO_B_NPC_Y = 0.58;
    // XP guadagnati convincendo l'addetto ad aprire la porta dell'Aula B.
    private static final int ADDETTO_XP = 60;

    private final AppContext context;
    private final GameEngine engine;
    private final GameState stato;
    private final Campaign campaign;
    private final AchievementManager achievementManager;
    private final ContentResolver resolver;
    private final StackPane root;

    // HUD.
    private final ProgressBar barraEnergia = new ProgressBar();
    private final Label valoreEnergia = new Label();
    private final Label valoreXp = new Label();
    private final Label titoloScena = new Label();
    private final Label suggerimento = new Label();
    private final HBox statistiche = new HBox(16);

    // Mappa e movimento.
    private final Pane mappa = new Pane();
    private final CharacterSprite sprite;
    private final ImageView personaggio = new ImageView();
    private CharacterSprite.Direzione direzione = CharacterSprite.Direzione.GIU;
    private final Set<KeyCode> tastiPremuti = EnumSet.noneOf(KeyCode.class);
    private final List<ElementoScena> elementi = new ArrayList<>();
    private final SceneEnvironment ambienti = new SceneEnvironment();
    private final List<Rectangle2D> muri = new ArrayList<>();
    // Rilevamento collisioni col personaggio: condivide la lista dei muri qui sopra.
    private final MappaCollisioni collisioni = new MappaCollisioni(muri, LATO_PERSONAGGIO);
    private final AnimationTimer ciclo;
    private double posX = (MAPPA_LARGHEZZA - LATO_PERSONAGGIO) / 2;
    private double posY = (MAPPA_ALTEZZA - LATO_PERSONAGGIO) / 2;
    private ElementoScena elementoVicino;
    // Porte degli edifici (Polo A/B del cortile e uscite a porta invisibili): la
    // trama delle porte vive qui, pilotata dalla scena che le offre i servizi.
    private final Porte porte;
    // Enigma del PC della vittima (capitolo 2): detiene le regole (binario, costi, XP)
    // e lo stato di risoluzione. La vista ne applica gli effetti invece di duplicarli.
    private PcVittimaPuzzle pcPuzzle;
    // Pensiero che indirizza verso il PC di Antonio, mostrato una sola volta quando
    // il giocatore ha sia la chiave sia l'indizio su Alex Kaur (in qualunque ordine).
    private boolean pensieroIndagineMostrato;
    // Diventa true non appena si esamina un PC sbagliato: serve a sbloccare il
    // traguardo "Cercatore d'oro" solo se il PC giusto è il primissimo a essere cliccato.
    private boolean pcSbagliatoToccato;
    // Pensiero mostrato una sola volta appena usciti dal Polo A nel cortile (capitolo 3).
    private boolean pensieroCortileMostrato;
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

    // Un solo pannello modale alla volta in sovrimpressione.
    private Node overlayCorrente;
    private boolean overlayChiudibile;
    // Traguardo appena sbloccato e in attesa di essere annunciato col dialog box, al
    // termine dell'interazione che lo ha generato (come l'annuncio "Nuovo indizio").
    private Achievement traguardoPendente;

    // Dialogo NPC eventualmente in corso: animazione e azione di avanzamento/chiusura.
    private Timeline dialogoMacchina;
    private Runnable avanzamentoDialogo;

    public ExplorationView(AppContext context, GameSession session) {
        this.context = Objects.requireNonNull(context, "Il contesto non puo' essere nullo.");
        Objects.requireNonNull(session, "La sessione non puo' essere nulla.");
        this.engine = session.getEngine();
        this.stato = session.getStato();
        this.campaign = session.getCampaign();
        this.achievementManager = session.getAchievementManager();
        this.resolver = context.contentResolver();
        this.sprite = new CharacterSprite(stato.getPlayer().getClasse());
        this.aulaB = new AulaB(this, stato, engine, MAPPA_LARGHEZZA, MAPPA_ALTEZZA);
        this.porte = new Porte(this, stato, engine, MAPPA_LARGHEZZA, MAPPA_ALTEZZA);

        root = new StackPane(costruisciLayout());
        root.getStyleClass().add("screen-root");
        aggiungiPulsanteInventario();

        this.ciclo = creaCicloDiGioco();
        collegaInputAllaScena();

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
        radice.setTop(costruisciHud());
        radice.setCenter(costruisciMappa());
        radice.setBottom(costruisciBarraInferiore());
        return radice;
    }

    private Node costruisciHud() {
        barraEnergia.setPrefWidth(180);
        valoreEnergia.getStyleClass().add("hud-text");
        valoreXp.getStyleClass().add("hud-text");
        Label etichettaEnergia = new Label("⚡ Energia");
        etichettaEnergia.getStyleClass().add("hud-text");

        VBox sinistra = new VBox(4, etichettaEnergia, new HBox(8, barraEnergia, valoreEnergia), valoreXp);
        statistiche.setAlignment(Pos.CENTER_RIGHT);

        Region spazio = new Region();
        HBox.setHgrow(spazio, Priority.ALWAYS);
        HBox hud = new HBox(sinistra, spazio, statistiche);
        hud.setAlignment(Pos.CENTER_LEFT);
        hud.getStyleClass().add("hud-bar");
        return hud;
    }

    private Node costruisciMappa() {
        mappa.getChildren().add(personaggio);
        mappa.getStyleClass().add("map-pane");
        mappa.setPrefSize(MAPPA_LARGHEZZA, MAPPA_ALTEZZA);
        mappa.setMaxSize(MAPPA_LARGHEZZA, MAPPA_ALTEZZA);
        personaggio.setImage(sprite.foglio());
        personaggio.setFitHeight(ALTEZZA_SPRITE);
        personaggio.setPreserveRatio(true);
        // Pixel art: bordi netti anche scalando.
        personaggio.setSmooth(false);
        personaggio.setViewport(sprite.viewport(direzione));
        aggiornaPosizioneSprite();

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
        muri.clear();
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

        mappa.getChildren().add(personaggio);

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
        for (Transition transizione : engine.transizioniDisponibili()) {
            if (isAulaLa1Capitolo2(scena) || isAulaBCapitolo3(scena)) {
                // Capitolo 2: la mail si apre come overlay sbloccando il PC. Aula B
                // (capitolo 3): la transizione all'epilogo è innescata trovando
                // l'indizio, non da un'uscita visibile. In entrambi i casi la
                // transizione non diventa un'uscita da raggiungere.
                continue;
            }
            if (isAulaLa1Capitolo3(scena)) {
                // Capitolo 3: l'uscita non è un elemento visibile ma la porta in
                // basso a destra, a cui avvicinarsi per tornare nel cortile.
                porte.aggiungiPortaUscita(transizione, Porte.AULA_LA1_PORTA_X, Porte.AULA_LA1_PORTA_Y);
            } else if (isCortileCapitolo3(scena)) {
                // Capitolo 3: anche le uscite del cortile sono porte invisibili sulle
                // facciate. Il Polo A (rientro in aula LA1) a sinistra è un'uscita
                // diretta; il Polo B (Aula B) a destra è invece sbarrato: si apre solo
                // se l'addetto ha aiutato, altrimenti va forzato.
                if ("aula_la1".equals(transizione.idDestinazione())) {
                    porte.aggiungiPortaUscita(transizione, Porte.CORTILE_PORTA_A_X, Porte.CORTILE_PORTE_Y);
                } else {
                    porte.aggiungiPortaPoloB(transizione);
                }
            } else {
                aggiungiUscita(transizione);
            }
        }

        disponiElementi(ambiente.orElse(null));
        // All'avvio del capitolo 3 il giocatore resta dov'era a fine capitolo 2
        // (davanti al PC); negli altri casi compare al punto di spawn della scena.
        if (preservaPosizione) {
            preservaPosizione = false;
            aggiornaPosizioneSprite();
        } else {
            posizionaPersonaggioIniziale(ambiente.orElse(null));
        }
        // Le porte del cortile hanno posizione fissa sulle facciate degli edifici:
        // vanno collocate dopo la disposizione a slot.
        porte.posizionaCortile();
        // L'Aula B aggiunge l'effetto torcia e i luccichii degli indizi sopra il resto.
        if (isAulaBCapitolo3(scena)) {
            aulaB.costruisci();
        }
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
            muri.add(new Rectangle2D(m.x() * MAPPA_LARGHEZZA, m.y() * MAPPA_ALTEZZA,
                    m.w() * MAPPA_LARGHEZZA, m.h() * MAPPA_ALTEZZA));
        }
    }

    private void posizionaPersonaggioIniziale(SceneEnvironment.Ambiente ambiente) {
        if (isCortileCapitolo3(engine.getScenaCorrente())) {
            // Si esce dal Polo A: il giocatore compare davanti alla sua porta.
            posX = Porte.CORTILE_PORTA_A_X * MAPPA_LARGHEZZA - LATO_PERSONAGGIO / 2;
            posY = (Porte.CORTILE_PORTE_Y + 0.06) * MAPPA_ALTEZZA - LATO_PERSONAGGIO / 2;
        } else if ("aula_la1".equals(engine.getScenaCorrente().getId())) {
            // Entrando in aula LA1 (in qualsiasi capitolo) il giocatore compare davanti
            // alla porta in basso a destra, dove si trova l'uscita verso il cortile.
            // L'avvio del capitolo 3 fa eccezione: lì la posizione (davanti al PC) è
            // conservata a monte da preservaPosizione, senza passare di qui.
            posX = Porte.AULA_LA1_PORTA_X * MAPPA_LARGHEZZA - LATO_PERSONAGGIO / 2;
            posY = Porte.AULA_LA1_PORTA_Y * MAPPA_ALTEZZA - LATO_PERSONAGGIO / 2;
        } else if (ambiente != null) {
            posX = ambiente.spawnX() * MAPPA_LARGHEZZA - LATO_PERSONAGGIO / 2;
            posY = ambiente.spawnY() * MAPPA_ALTEZZA - LATO_PERSONAGGIO / 2;
        } else {
            posX = (MAPPA_LARGHEZZA - LATO_PERSONAGGIO) / 2;
            posY = MAPPA_ALTEZZA - LATO_PERSONAGGIO - 8;
        }
        aggiornaPosizioneSprite();
    }

    /**
     * Allinea lo sprite alla posizione logica: centrato in orizzontale sul
     * riquadro di collisione e appoggiato col suo bordo inferiore alla base del
     * riquadro, così che i "piedi" coincidano col punto di collisione.
     */
    private void aggiornaPosizioneSprite() {
        double larghezzaSprite = sprite.larghezzaPer(ALTEZZA_SPRITE);
        personaggio.setLayoutX(posX + (LATO_PERSONAGGIO - larghezzaSprite) / 2);
        personaggio.setLayoutY(posY + LATO_PERSONAGGIO - ALTEZZA_SPRITE);
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
            e.azione = () -> interagisciConStudenteUbriaco(npc);
        } else if ("tecnico_laboratorio".equals(id)) {
            e.azione = () -> interagisciConTecnico(npc);
        } else if ("addetto_pulizie".equals(id)) {
            e.azione = () -> interagisciConAddetto(npc);
        } else {
            e.azione = () -> interagisciConNpc(npc);
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
            aggiungiPcAula((PcVittimaPuzzle) puzzle);
            return;
        }
        ElementoScena e = new ElementoScena(TipoElemento.ENIGMA, "Enigma", "Esamina l'enigma", Color.web("#9b59b6"));
        e.puzzle = puzzle;
        e.azione = () -> mostraEnigma(puzzle, e);
        registra(e);
    }

    /**
     * Dispone i sei PC del laboratorio (due per banco) come punti interattivi
     * invisibili e indistinguibili. Solo l'ultimo a destra, verso il muro, è il PC
     * della vittima: interagendovi la password si inserisce da sé e si apre la mail.
     * Sui restanti cinque si ottiene "PC sbagliato" e una penalità di energia. Sono
     * a posizione fissa, in corrispondenza dei monitor.
     */
    private void aggiungiPcAula(PcVittimaPuzzle puzzle) {
        // L'enigma del PC porta con sé le proprie regole: lo conserviamo per applicarne
        // gli effetti (energia, XP) nelle varie vie di risoluzione.
        this.pcPuzzle = puzzle;
        // Nuova disposizione dei PC: azzera la traccia degli errori per il traguardo
        // "Cercatore d'oro" (PC giusto al primissimo click).
        pcSbagliatoToccato = false;
        double[][] posizioni = {
                {0.14, 0.50}, {0.25, 0.50},   // banco di sinistra
                {0.45, 0.50}, {0.56, 0.50},   // banco centrale
                {0.75, 0.50}, {0.86, 0.50}    // banco di destra: l'ultimo è il PC della vittima
        };
        int indiceCorretto = posizioni.length - 1;
        for (int i = 0; i < posizioni.length; i++) {
            boolean corretto = (i == indiceCorretto);
            ElementoScena e = new ElementoScena(TipoElemento.PORTA, "", "Esamina il PC", Color.web("#9b59b6"));
            e.posizioneFissa = true;
            e.azione = corretto ? () -> apriPcVittima(e) : this::pcSbagliato;
            e.setVisibile(false);
            registra(e);
            e.posiziona(posizioni[i][0] * MAPPA_LARGHEZZA, posizioni[i][1] * MAPPA_ALTEZZA);
        }
    }

    /**
     * Interazione con il PC della vittima. Le vie, in ordine di priorità:
     * <ul>
     *     <li>password ottenuta dal tecnico (Carisma + dialogo): inserimento diretto;</li>
     *     <li>Investigazione e Intuizione &ge; 1: scelta tra enigma binario e ricerca indizi;</li>
     *     <li>solo Investigazione &ge; 1: enigma binario;</li>
     *     <li>solo Intuizione &ge; 1: ricerca del foglietto con la password;</li>
     *     <li>nessuna: solo forza bruta.</li>
     * </ul>
     * In ogni caso lo sblocco assegna {@value PcVittimaPuzzle#XP} XP, applicati dalla
     * via di risoluzione del {@link PcVittimaPuzzle}.
     */
    private void apriPcVittima(ElementoScena elemento) {
        // Traguardo "Cercatore d'oro": il PC di Antonio è il primissimo computer
        // esaminato (nessuno dei cinque sbagliati toccato prima). Lo si annuncia col
        // dialog box prima di aprire il terminale.
        if (!pcSbagliatoToccato) {
            achievementManager.segnalaPcVittimaAlPrimoColpo();
        }
        annunciaTraguardoSePresente(() -> procediAperturaPc(elemento));
    }

    /** Apertura vera e propria del terminale, secondo le vie disponibili. */
    private void procediAperturaPc(ElementoScena elemento) {
        Player player = stato.getPlayer();
        if (stato.hasFlag(ContentResolver.FLAG_ASSISTENTE)) {
            pcPuzzle.usaPasswordAssistente(player);
            aggiornaHud();
            mostraDialogo("", "Hai inserito la password.", () -> pcVittimaRisolto(elemento), List.of());
            return;
        }
        boolean investigatore = player.getStatistica(StatType.INVESTIGAZIONE) >= 1;
        boolean intuitivo = player.getStatistica(StatType.INTUIZIONE) >= 1;
        if (investigatore && intuitivo) {
            mostraSceltaViePc(elemento);
        } else if (investigatore) {
            mostraEnigmaBinario(elemento);
        } else if (intuitivo) {
            cercaIndiziPc(elemento);
        } else {
            mostraForzaBrutaPc(elemento);
        }
    }

    /**
     * Con Investigazione e Intuizione il giocatore sceglie la via: analizzare il
     * terminale (enigma binario) o cercare indizi (il foglietto con la password).
     * Resta comunque disponibile la forza bruta.
     */
    private void mostraSceltaViePc(ElementoScena elemento) {
        VBox pannello = pannelloPc("Il terminale è protetto. Come procedi?");

        Button analizza = new Button("Analizza il terminale");
        analizza.getStyleClass().add("game-button");
        analizza.setMaxWidth(380);
        analizza.setOnAction(e -> mostraEnigmaBinario(elemento));

        Button cerca = new Button("Cerca indizi intorno alla postazione");
        cerca.getStyleClass().add("game-button");
        cerca.setMaxWidth(380);
        cerca.setOnAction(e -> cercaIndiziPc(elemento));

        pannello.getChildren().addAll(analizza, cerca, bottoneForzaBrutaPc(elemento), bottoneAnnullaPc());
        mostraOverlay(velo(pannello), true);
    }

    /**
     * Enigma binario (via Investigazione): converti il binario {@link PcVittimaPuzzle#BINARIO}
     * in decimale. La valutazione del tentativo (sblocco o penalita' di energia) e' delegata
     * a {@link PcVittimaPuzzle#tenta(Player, String)}; resta disponibile la forza bruta.
     */
    private void mostraEnigmaBinario(ElementoScena elemento) {
        Player player = stato.getPlayer();
        VBox pannello = pannelloPc(pcPuzzle.testoEnigmaBinario());

        Label esito = new Label();
        esito.getStyleClass().add("overlay-subtitle");
        esito.setWrapText(true);
        esito.setMaxWidth(460);
        esito.setTextAlignment(TextAlignment.CENTER);

        VBox tastierino = TastierinoNumerico.crea(2, codice -> {
            PuzzleOutcome risultato = pcPuzzle.tenta(player, codice);
            aggiornaHud();
            if (risultato.risolto()) {
                mostraDialogo("", "Hai inserito la password.", () -> pcVittimaRisolto(elemento), List.of());
                return true;
            }
            if (engine.verificaGameOver()) {
                return true;
            }
            esito.setText(risultato.messaggio());
            return false;
        });

        pannello.getChildren().addAll(tastierino, bottoneForzaBrutaPc(elemento), bottoneAnnullaPc(), esito);
        mostraOverlay(velo(pannello), true);
    }

    /**
     * Via Intuizione: il sesto senso del personaggio trova il foglietto con la
     * password sotto la tastiera. Nessun costo di energia; lo sblocco è immediato.
     */
    private void cercaIndiziPc(ElementoScena elemento) {
        PuzzleOutcome risultato = pcPuzzle.cercaIndizi(stato.getPlayer());
        aggiornaHud();
        mostraDialogo("", risultato.messaggio(), () -> pcVittimaRisolto(elemento), List.of());
    }

    /**
     * Schermata per chi non ha vie agevolate: resta solo la forza bruta.
     */
    private void mostraForzaBrutaPc(ElementoScena elemento) {
        VBox pannello = pannelloPc("Il terminale è protetto da una password che non conosci.");
        pannello.getChildren().addAll(bottoneForzaBrutaPc(elemento), bottoneAnnullaPc());
        mostraOverlay(velo(pannello), true);
    }

    /** Pannello base (titolo + testo) condiviso dalle schermate del PC della vittima. */
    private VBox pannelloPc(String descrizione) {
        Label titolo = new Label("Terminale di Antonio");
        titolo.getStyleClass().add("scene-title");
        titolo.setAlignment(Pos.CENTER);
        Label testo = new Label(descrizione);
        testo.getStyleClass().add("overlay-subtitle");
        testo.setWrapText(true);
        testo.setMaxWidth(460);
        testo.setTextAlignment(TextAlignment.CENTER);
        // Senza questo il testo, in un VBox fillWidth, resterebbe ancorato a sinistra.
        testo.setAlignment(Pos.CENTER);
        testo.setMaxWidth(Double.MAX_VALUE);

        VBox pannello = new VBox(16, titolo, testo);
        pannello.setAlignment(Pos.CENTER);
        pannello.setMaxWidth(480);
        pannello.getStyleClass().add("pixel-font");
        return pannello;
    }

    /**
     * Pulsante "Annulla" del PC: chiude il terminale senza alcun costo, così il
     * giocatore può tornare indietro (ad esempio per parlare prima col tecnico) invece
     * di essere costretto alla forza bruta.
     */
    private Button bottoneAnnullaPc() {
        Button annulla = new Button("Annulla");
        annulla.getStyleClass().add("game-button");
        annulla.setMaxWidth(380);
        annulla.setOnAction(e -> chiudiOverlay());
        return annulla;
    }

    /** Pulsante "Forza bruta" del PC: penalità di energia, poi sblocco e mail. */
    private Button bottoneForzaBrutaPc(ElementoScena elemento) {
        Button forza = new Button("Forza bruta (perdi energia)");
        forza.getStyleClass().add("game-button");
        forza.setOnAction(e -> {
            pcPuzzle.forzaBruta(stato.getPlayer());
            aggiornaHud();
            if (engine.verificaGameOver()) {
                return;
            }
            mostraDialogo("", "Dopo svariati tentativi il terminale cede.",
                    () -> pcVittimaRisolto(elemento), List.of());
        });
        return forza;
    }

    /**
     * Interazione con un PC sbagliato: penalità di energia e avviso. Se l'energia
     * si esaurisce scatta il game over.
     */
    private void pcSbagliato() {
        // Un PC sbagliato esclude definitivamente il traguardo "Cercatore d'oro".
        pcSbagliatoToccato = true;
        Player player = stato.getPlayer();
        player.riduciEnergia(COSTO_ENERGIA_DIALOGO);
        aggiornaHud();
        if (engine.verificaGameOver()) {
            return;
        }
        mostraDialogo("", "PC sbagliato.");
    }

    /**
     * Mostra una sola volta il pensiero che indirizza verso il PC di Antonio, non
     * appena il giocatore possiede sia la chiave sia l'indizio su Alex Kaur (in
     * qualunque ordine abbia compiuto le due azioni).
     */
    private void forsePensieroIndagine() {
        if (!pensieroIndagineMostrato && porte.isEnigmaPortaSbloccato()) {
            pensieroIndagineMostrato = true;
            mostraDialogo(stato.getPlayer().getNome(),
                    "Quel ragazzo ha detto di aver visto Antonio litigare con Alex… ma perché "
                            + "l'aggressore avrebbe dovuto rubargli le chiavi? Mi servono altre "
                            + "informazioni… forse riesco a controllare il PC di Antonio al Polo A.");
        }
    }

    private void aggiungiUscita(Transition transizione) {
        ElementoScena e = new ElementoScena(TipoElemento.USCITA, transizione.etichetta(),
                transizione.etichetta(), Color.web("#2ecc71"));
        e.azione = () -> usaUscita(transizione);
        registra(e);
    }

    void registra(ElementoScena e) {
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

    /**
     * @return {@code true} se la scena è l'aula LA1 del capitolo 2, dove l'email
     *         si apre come overlay sbloccando il PC e la sua transizione non va
     *         resa come uscita (a differenza dell'aula LA1 del capitolo 3)
     */
    private boolean isAulaLa1Capitolo2(Scene scena) {
        return "aula_la1".equals(scena.getId())
                && "capitolo2".equals(engine.getCapitoloCorrente().getId());
    }

    /**
     * @return {@code true} se la scena è l'aula LA1 del capitolo 3, dove l'uscita
     *         verso il cortile è una porta invisibile in basso a destra
     */
    private boolean isAulaLa1Capitolo3(Scene scena) {
        return "aula_la1".equals(scena.getId())
                && "capitolo3".equals(engine.getCapitoloCorrente().getId());
    }

    /**
     * @return {@code true} se la scena è il cortile del capitolo 3, dove le uscite
     *         verso il Polo A e il Polo B sono porte invisibili sulle facciate
     */
    private boolean isCortileCapitolo3(Scene scena) {
        return "cortile".equals(scena.getId())
                && "capitolo3".equals(engine.getCapitoloCorrente().getId());
    }

    /**
     * @return {@code true} se la scena è l'Aula B del capitolo 3, esplorabile al buio
     *         con l'effetto torcia e gli indizi da trovare
     */
    boolean isAulaBCapitolo3(Scene scena) {
        return "aula_b".equals(scena.getId())
                && "capitolo3".equals(engine.getCapitoloCorrente().getId());
    }

    // ----------------------------------------------------------------------
    // Effetto torcia dell'Aula B (capitolo 3)
    // ----------------------------------------------------------------------

    /**
     * Aggiunge il velo della torcia sopra lo scenario e il personaggio: un foro di
     * luce lo rende trasparente solo attorno al giocatore, lasciando il resto
     * dell'aula al buio. Invocato dalla trama dell'{@link AulaB}.
     */
    void aggiungiTorcia() {
        torcia = new EffettoTorcia(MAPPA_LARGHEZZA, MAPPA_ALTEZZA);
        mappa.getChildren().add(torcia.nodo());
    }

    /** Ricalcola il foro di luce della torcia centrandolo sul giocatore. */
    void aggiornaTorcia() {
        if (torcia != null) {
            torcia.centraSu(posX + LATO_PERSONAGGIO / 2.0, posY + LATO_PERSONAGGIO / 2.0);
        }
    }

    /** Registra un'animazione di scena, da fermare alla ricostruzione della stessa. */
    void registraAnimazione(Timeline animazione) {
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
    // Movimento e prossimita'
    // ----------------------------------------------------------------------

    private AnimationTimer creaCicloDiGioco() {
        return new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (overlayCorrente != null || engine.isInPausa()) {
                    return;
                }
                double dx = 0;
                double dy = 0;
                if (tastiPremuti.contains(KeyCode.W)) {
                    dy -= VELOCITA;
                }
                if (tastiPremuti.contains(KeyCode.S)) {
                    dy += VELOCITA;
                }
                if (tastiPremuti.contains(KeyCode.A)) {
                    dx -= VELOCITA;
                }
                if (tastiPremuti.contains(KeyCode.D)) {
                    dx += VELOCITA;
                }
                // Spostamento separato sui due assi: cosi' il personaggio scivola
                // lungo un muro invece di bloccarsi del tutto contro uno spigolo.
                if (dx != 0) {
                    double nuovaX = clamp(posX + dx, MAPPA_LARGHEZZA - LATO_PERSONAGGIO);
                    if (!collisioni.collide(nuovaX, posY)) {
                        posX = nuovaX;
                    }
                }
                if (dy != 0) {
                    double nuovaY = clamp(posY + dy, MAPPA_ALTEZZA - LATO_PERSONAGGIO);
                    if (!collisioni.collide(posX, nuovaY)) {
                        posY = nuovaY;
                    }
                }
                if (dx != 0 || dy != 0) {
                    aggiornaDirezione(dx, dy);
                    aggiornaPosizioneSprite();
                    aggiornaTorcia();
                }
                aggiornaElementoVicino();
            }
        };
    }

    /**
     * Aggiorna la direzione dello sprite in base allo spostamento, dando
     * priorità all'asse orizzontale, e cambia la cella mostrata solo se la
     * direzione è effettivamente cambiata.
     */
    private void aggiornaDirezione(double dx, double dy) {
        CharacterSprite.Direzione nuova;
        if (dx > 0) {
            nuova = CharacterSprite.Direzione.DESTRA;
        } else if (dx < 0) {
            nuova = CharacterSprite.Direzione.SINISTRA;
        } else if (dy < 0) {
            nuova = CharacterSprite.Direzione.SU;
        } else {
            nuova = CharacterSprite.Direzione.GIU;
        }
        if (nuova != direzione) {
            direzione = nuova;
            personaggio.setViewport(sprite.viewport(direzione));
        }
    }

    /**
     * Individua l'elemento piu' vicino al personaggio entro il raggio di
     * interazione e aggiorna il suggerimento a schermo.
     */
    private void aggiornaElementoVicino() {
        double centroX = posX + LATO_PERSONAGGIO / 2;
        double centroY = posY + LATO_PERSONAGGIO / 2;
        ElementoScena piuVicino = null;
        double minDistanza = RAGGIO_INTERAZIONE;
        for (ElementoScena e : elementi) {
            double distanza = Math.hypot(centroX - e.x, centroY - e.y);
            if (distanza <= minDistanza) {
                minDistanza = distanza;
                piuVicino = e;
            }
        }
        if (piuVicino != elementoVicino) {
            evidenzia(elementoVicino, false);
            evidenzia(piuVicino, true);
            elementoVicino = piuVicino;
        }
        suggerimento.setText(piuVicino == null ? "" : "▲ Premi E — " + piuVicino.etichettaAzione);
    }

    private void evidenzia(ElementoScena e, boolean attivo) {
        if (e != null) {
            e.evidenzia(attivo);
        }
    }

    private double clamp(double valore, double massimo) {
        return Math.max(0, Math.min(valore, massimo));
    }

    /**
     * Collega la cattura dei tasti alla scena quando la vista vi viene inserita e
     * la scollega (fermando il ciclo) quando viene rimossa, evitando dispersioni.
     */
    private void collegaInputAllaScena() {
        root.sceneProperty().addListener((obs, vecchia, nuova) -> {
            if (nuova != null) {
                abilitaInput(nuova);
                ciclo.start();
            } else {
                if (vecchia != null) {
                    vecchia.setOnKeyPressed(null);
                    vecchia.setOnKeyReleased(null);
                }
                ciclo.stop();
            }
        });
    }

    private void abilitaInput(javafx.scene.Scene scena) {
        scena.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case E -> interagisci();
                case ESCAPE -> gestisciEscape();
                default -> tastiPremuti.add(e.getCode());
            }
        });
        scena.setOnKeyReleased(e -> tastiPremuti.remove(e.getCode()));
    }

    private void interagisci() {
        // Con un dialogo aperto, E lo fa avanzare (completa il testo o lo chiude).
        if (avanzamentoDialogo != null) {
            avanzamentoDialogo.run();
            return;
        }
        if (overlayCorrente == null && !engine.isInPausa() && elementoVicino != null) {
            elementoVicino.azione.run();
        }
    }

    private void gestisciEscape() {
        if (engine.isInPausa()) {
            engine.riprendi();
        } else if (overlayCorrente != null) {
            if (overlayChiudibile) {
                chiudiOverlay();
            }
        } else {
            engine.pausa();
        }
    }

    // ----------------------------------------------------------------------
    // Interazioni
    // ----------------------------------------------------------------------

    private void interagisciConNpc(Npc npc) {
        Player player = stato.getPlayer();
        // Parlare con un NPC costa energia; se si esaurisce, scatta il game over.
        player.riduciEnergia(COSTO_ENERGIA_DIALOGO);
        aggiornaHud();
        if (engine.verificaGameOver()) {
            return;
        }
        String battuta = npc.parla(player);

        StringBuilder testo = new StringBuilder(battuta);
        if (npc.getDialogo().isAccessibile(player)) {
            resolver.indizioDi(npc.getId()).ifPresent(indizio -> {
                if (engine.trovaIndizio(indizio)) {
                    testo.append("\n\n🔎 Nuovo indizio nel diario: ").append(indizio.titolo());
                }
            });
        }
        mostraDialogo(npc.getNome(), testo.toString());
    }

    /**
     * Interazione su misura con il tecnico di laboratorio. Costa energia come ogni
     * dialogo; con Carisma sufficiente lo scambio si svolge in tre battute scorrevoli
     * (tecnico, giocatore, tecnico) e sblocca la password del PC. Sotto soglia il
     * tecnico resta sotto shock e non rivela nulla.
     */
    private void interagisciConTecnico(Npc npc) {
        Player player = stato.getPlayer();
        player.riduciEnergia(COSTO_ENERGIA_DIALOGO);
        aggiornaHud();
        if (engine.verificaGameOver()) {
            return;
        }
        if (!npc.getDialogo().isAccessibile(player)) {
            mostraDialogo(npc.getNome(), npc.parla(player));
            return;
        }
        // L'aver parlato con l'assistente abilita la via "Carisma" del PC della vittima.
        stato.setFlag(ContentResolver.FLAG_ASSISTENTE);
        List<String[]> battute = List.of(
                new String[]{npc.getNome(), NpcCatalog.TECNICO_BATTUTA_1},
                new String[]{player.getNome(), NpcCatalog.TECNICO_BATTUTA_GIOCATORE_1},
                new String[]{player.getNome(), NpcCatalog.TECNICO_BATTUTA_GIOCATORE_2},
                new String[]{npc.getNome(), NpcCatalog.TECNICO_BATTUTA_3});
        mostraSequenzaDialogo(battute, 0, () -> concludiDialogoTecnico(npc));
    }

    /**
     * Mostra in sequenza le battute (nome, testo): premendo E si passa alla
     * successiva; dopo l'ultima si esegue {@code alTermine}.
     */
    private void mostraSequenzaDialogo(List<String[]> battute, int indice, Runnable alTermine) {
        String[] battuta = battute.get(indice);
        boolean ultima = indice >= battute.size() - 1;
        mostraDialogo(battuta[0], battuta[1], () -> {
            if (ultima) {
                alTermine.run();
            } else {
                mostraSequenzaDialogo(battute, indice + 1, alTermine);
            }
        }, List.of());
    }

    /**
     * Conclusione del dialogo con il tecnico: registra l'indizio sulla password e,
     * se è nuovo, lo annuncia in un'ultima battuta; altrimenti chiude.
     */
    private void concludiDialogoTecnico(Npc npc) {
        var indizio = resolver.indizioDi(npc.getId());
        if (indizio.isPresent() && engine.trovaIndizio(indizio.get())) {
            mostraDialogo("", "🔎 Nuovo indizio nel diario: " + indizio.get().titolo());
        } else {
            chiudiOverlay();
        }
    }

    /**
     * Interazione su misura con l'addetto alle pulizie nel cortile (capitolo 3).
     * L'addetto respinge tutti; con Carisma &ge; 2 il giocatore lo convince in una
     * sequenza di battute (rifiuto, replica, accettazione): l'addetto apre la porta
     * laterale dell'Aula B, assegna {@value #ADDETTO_XP} XP e rivela l'indizio. Sotto
     * soglia il rifiuto costa {@link #COSTO_ENERGIA_DIALOGO} di energia. Una volta
     * aperta la porta, ulteriori interazioni si limitano a sollecitare il giocatore.
     */
    private void interagisciConAddetto(Npc npc) {
        if (stato.hasFlag(ContentResolver.FLAG_PORTA_AULA_B)) {
            mostraDialogo(npc.getNome(), "Sbrigati, ti ho già aperto la porta laterale.");
            return;
        }
        Player player = stato.getPlayer();
        if (npc.getDialogo().isAccessibile(player)) {
            List<String[]> battute = List.of(
                    new String[]{npc.getNome(), NpcCatalog.ADDETTO_RIFIUTO},
                    new String[]{player.getNome(), NpcCatalog.ADDETTO_GIOCATORE},
                    new String[]{npc.getNome(), NpcCatalog.ADDETTO_ACCETTA});
            mostraSequenzaDialogo(battute, 0, () -> concludiDialogoAddetto(npc));
            return;
        }
        // Carisma insufficiente: rifiuto secco, a costo di energia.
        player.riduciEnergia(COSTO_ENERGIA_DIALOGO);
        aggiornaHud();
        if (engine.verificaGameOver()) {
            return;
        }
        mostraDialogo(npc.getNome(), NpcCatalog.ADDETTO_RIFIUTO);
    }

    /**
     * Conclusione del dialogo riuscito con l'addetto: sblocca la porta dell'Aula B,
     * assegna gli XP e registra (annunciandolo) l'indizio sullo studente in fuga.
     */
    private void concludiDialogoAddetto(Npc npc) {
        stato.setFlag(ContentResolver.FLAG_PORTA_AULA_B);
        stato.getPlayer().aggiungiXp(ADDETTO_XP);
        aggiornaHud();
        var indizio = resolver.indizioDi(npc.getId());
        if (indizio.isPresent() && engine.trovaIndizio(indizio.get())) {
            mostraDialogo("", "🔎 Nuovo indizio nel diario: " + indizio.get().titolo(),
                    () -> mostraPotenziamentoSeDovuto(this::chiudiOverlay), List.of());
        } else {
            mostraPotenziamentoSeDovuto(this::chiudiOverlay);
        }
    }

    /**
     * Interazione su misura con lo studente ubriaco. Come ogni dialogo costa
     * {@link #COSTO_ENERGIA_DIALOGO} di energia; se il giocatore ha Carisma &gt; 0
     * (ha scelto il rappresentante) l'NPC lo riconosce e gli offre un sorso,
     * restituendo l'energia appena spesa quando il giocatore passa la battuta
     * (premendo E). In ogni caso prosegue con la battuta comune a scelte.
     */
    private void interagisciConStudenteUbriaco(Npc npc) {
        Player player = stato.getPlayer();
        player.riduciEnergia(COSTO_ENERGIA_DIALOGO);
        aggiornaHud();
        if (engine.verificaGameOver()) {
            return;
        }
        if (player.getStatistica(StatType.CARISMA) > 0) {
            String saluto = "Ehi, ma tu sei il rappresentante! Tieni, fatti un goccio... "
                    + "alla salute di Antonio, povero ragazzo.";
            // Al passaggio della battuta si recupera l'energia spesa per parlargli.
            mostraDialogo(npc.getNome(), saluto, () -> {
                player.ripristinaEnergia(COSTO_ENERGIA_DIALOGO);
                aggiornaHud();
                mostraDialogoUbriacoConScelte(npc);
            }, List.of());
        } else {
            mostraDialogoUbriacoConScelte(npc);
        }
    }

    /**
     * Battuta comune dello studente ubriaco (mostrata a tutti i giocatori) con le
     * due scelte: chiedere dettagli per ottenere l'indizio o lasciar perdere.
     */
    private void mostraDialogoUbriacoConScelte(Npc npc) {
        String battuta = "Ho visto Antonio discutere con qualcuno prima... mi pare.";
        mostraDialogo(npc.getNome(), battuta, this::chiudiOverlay, List.of(
                new OpzioneDialogo("Chiedi dettagli", () -> chiediDettagliUbriaco(npc)),
                new OpzioneDialogo("Lascia stare", this::chiudiOverlay)));
    }

    /**
     * Esito della scelta "chiedi dettagli": la prima volta assegna +20 XP e
     * registra nel diario l'indizio sul litigante; poi mostra la rivelazione.
     */
    private void chiediDettagliUbriaco(Npc npc) {
        Player player = stato.getPlayer();
        StringBuilder testo = new StringBuilder("Mi sembrava di averlo visto litigare con Alex Kaur.");
        resolver.indizioDi(npc.getId()).ifPresent(indizio -> {
            if (engine.trovaIndizio(indizio)) {
                player.aggiungiXp(20);
                testo.append("\n\n🔎 Nuovo indizio nel diario: ").append(indizio.titolo());
            }
        });
        aggiornaHud();
        // Alla chiusura della battuta, se ora il giocatore ha sia la chiave sia
        // l'indizio, parte il pensiero che lo indirizza verso il PC di Antonio.
        mostraDialogo(npc.getNome(), testo.toString(),
                () -> mostraPotenziamentoSeDovuto(() -> {
                    chiudiOverlay();
                    forsePensieroIndagine();
                }), List.of());
    }

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

    void usaUscita(Transition transizione) {
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
        if (engine.isPartitaTerminata() || overlayCorrente != null) {
            return;
        }
        String titolo = engine.getScenaCorrente().getTitolo();
        context.navigator().mostra(new ChapterTitleView(context, titolo, () -> {
            context.navigator().mostra(root);
            forsePensieroCortile();
            aulaB.forseIngresso();
        }).getRoot());
    }

    /**
     * Mostra una sola volta, appena usciti dal Polo A nel cortile (capitolo 3), il
     * pensiero che sprona il giocatore a sbrigarsi.
     */
    private void forsePensieroCortile() {
        if (!pensieroCortileMostrato && isCortileCapitolo3(engine.getScenaCorrente())) {
            pensieroCortileMostrato = true;
            mostraDialogo(stato.getPlayer().getNome(),
                    "Stanno tutti scappando, devo muovermi prima che sia tardi.");
        }
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
            if (overlayCorrente == null && !engine.isInPausa()) {
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
     * Esito dello sblocco del PC della vittima: il contenuto (la mail) si mostra
     * come overlay sopra l'aula, senza cambiare scena, così alla chiusura si torna
     * all'aula con personaggio e NPC invariati.
     */
    private void pcVittimaRisolto(ElementoScena elemento) {
        // Gli XP dello sblocco sono già stati assegnati dalla via di risoluzione
        // (PcVittimaPuzzle), qualunque essa sia stata.
        rimuoviElemento(elemento);
        aggiornaHud();
        if (engine.verificaGameOver()) {
            return;
        }
        mostraEmailVittima();
    }

    /**
     * Mostra a tutto schermo la mail minatoria trovata sul PC: mittente in alto,
     * corpo al centro. Registra l'indizio sul mittente e, alla chiusura, fa partire
     * il pensiero del giocatore sulle iniziali.
     */
    private void mostraEmailVittima() {
        engine.trovaIndizio(ClueCatalog.emailMittente());

        Label mittente = new Label("Mittente: em.informatica@gmail.com");
        mittente.getStyleClass().add("scene-title");
        Label corpo = new Label("Pensavi non me ne accorgessi? Dobbiamo parlare. Vediamoci in Aula B a "
                + "00:30, o stavolta finisce malissimo, non scherzo.");
        corpo.getStyleClass().add("overlay-subtitle");
        corpo.setWrapText(true);
        corpo.setMaxWidth(640);

        Button chiudi = new Button("Chiudi");
        chiudi.getStyleClass().add("game-button");
        chiudi.setOnAction(e -> {
            chiudiOverlay();
            mostraPensieroEmail();
        });

        // Mittente ancorato in alto, corpo al centro, pulsante in basso: come una mail.
        VBox intestazione = new VBox(mittente);
        intestazione.setAlignment(Pos.TOP_LEFT);
        BorderPane mail = new BorderPane();
        mail.setTop(intestazione);
        mail.setCenter(corpo);
        mail.setBottom(chiudi);
        BorderPane.setAlignment(chiudi, Pos.CENTER);
        BorderPane.setMargin(corpo, new Insets(32, 0, 32, 0));
        mail.setPadding(new Insets(48));
        // Riempie lo schermo, così il mittente resta ancorato in alto.
        mail.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        mail.getStyleClass().add("pixel-font");
        mostraOverlay(velo(mail), false);
    }

    /**
     * Pensiero del giocatore dopo aver letto la mail: le iniziali del mittente non
     * combaciano con Alex. Alla chiusura conclude il capitolo.
     */
    private void mostraPensieroEmail() {
        mostraDialogo(stato.getPlayer().getNome(),
                "Mmhhh… EM... delle iniziali? Non corrispondono però al nome di Alex con cui era "
                        + "stato visto litigare prima.",
                this::concludiCapitoloDue, List.of());
    }

    /**
     * Fine del capitolo 2: completa formalmente il capitolo spostando il motore
     * sulla scena terminale dell'email (la transizione era stata mostrata come
     * overlay), poi propone la scelta del potenziamento. Confermandola, il motore
     * avanza al capitolo 3 con la consueta sequenza di cartelli e il pensiero che
     * indirizza il giocatore verso il Polo B.
     */
    private void concludiCapitoloDue() {
        // Completa il capitolo sulla scena terminale dell'email senza ricostruire la
        // vista: lo sfondo resta l'aula LA1 già presente sotto gli overlay.
        avanzaSenzaRicostruire("email_vittima");
        // Prima l'eventuale potenziamento a 100 XP (es. dallo sblocco del PC), poi la
        // scelta di fine capitolo (potenziamento gratuito prima del capitolo 3).
        mostraPotenziamentoSeDovuto(() -> mostraSceltaUpgrade("Capitolo completato",
                "Scegli una statistica da potenziare prima del prossimo capitolo:",
                engine::concludiCapitolo));
    }

    /**
     * Avanza alla scena indicata senza far ricostruire la vista: la schermata
     * corrente (sfondo, titolo) resta invariata. Serve per le scene "di servizio"
     * (email_vittima, epilogo) che esistono solo per far avanzare il motore mentre
     * il contenuto vero è mostrato come overlay.
     *
     * @param idScena id della scena di servizio verso cui avanzare
     */
    void avanzaSenzaRicostruire(String idScena) {
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
    void mostraDialogo(String nome, String testo) {
        mostraDialogo(nome, testo, this::chiudiOverlay, List.of());
    }

    /**
     * Variante completa del dialog box. Quando il testo è interamente rivelato:
     * se {@code opzioni} è vuota un ulteriore input (E o clic) esegue {@code alTermine}
     * (di norma la chiusura); altrimenti compaiono i pulsanti di scelta e l'avanzamento
     * da tastiera/clic diventa inerte (si prosegue scegliendo un'opzione).
     *
     * @param nome     nome dell'NPC mostrato in alto
     * @param testo    battuta rivelata a macchina da scrivere
     * @param alTermine azione eseguita al termine quando non ci sono scelte
     * @param opzioni  le scelte proposte al termine (eventualmente vuote)
     */
    void mostraDialogo(String nome, String testo, Runnable alTermine, List<OpzioneDialogo> opzioni) {
        DialogoBox.Sessione dialogo = DialogoBox.crea(root, nome, testo, alTermine, opzioni);
        // I campi vanno impostati dopo mostraOverlay, che azzera lo stato precedente.
        mostraOverlay(dialogo.nodo(), true);
        dialogoMacchina = dialogo.macchina();
        avanzamentoDialogo = dialogo.avanzamento();
        dialogo.avvia();
    }

    void mostraEnigma(Puzzle puzzle, ElementoScena elemento) {
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
        if (overlayCorrente == null) {
            verificaCompletamentoCapitolo();
        }
    }

    private void mostraSceltaUpgrade(String titolo, String sottotitolo, Consumer<StatType> azione) {
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
            Button scelta = new Button(icona(tipo) + " " + tipo.getNomeVisualizzato()
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
    void mostraPotenziamentoSeDovuto(Runnable dopo) {
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

    private StackPane velo(Node contenuto) {
        StackPane overlay = new StackPane(contenuto);
        overlay.getStyleClass().add("overlay-veil");
        return overlay;
    }

    private void mostraOverlay(Node velo, boolean chiudibile) {
        chiudiOverlay();
        overlayCorrente = velo;
        overlayChiudibile = chiudibile;
        tastiPremuti.clear();
        root.getChildren().add(velo);
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
    private void annunciaTraguardoSePresente(Runnable dopo) {
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

    void chiudiOverlay() {
        // Ferma l'eventuale dialogo in corso prima di rimuovere il pannello.
        if (dialogoMacchina != null) {
            dialogoMacchina.stop();
            dialogoMacchina = null;
        }
        avanzamentoDialogo = null;
        if (overlayCorrente != null) {
            root.getChildren().remove(overlayCorrente);
            overlayCorrente = null;
        }
    }

    // ----------------------------------------------------------------------
    // Reazione agli eventi del motore
    // ----------------------------------------------------------------------

    void aggiornaHud() {
        Player player = stato.getPlayer();
        int energia = engine.getEnergia();
        barraEnergia.setProgress((double) energia / Player.ENERGIA_MASSIMA);
        barraEnergia.setStyle("-fx-accent: " + coloreEnergia(energia) + ";");
        valoreEnergia.setText(energia + " / " + Player.ENERGIA_MASSIMA);
        valoreXp.setText("XP: " + player.getXp() + " / " + Player.COSTO_XP_POTENZIAMENTO);

        statistiche.getChildren().clear();
        for (StatType tipo : StatType.values()) {
            Label stat = new Label(icona(tipo) + " " + player.getStatistica(tipo));
            stat.getStyleClass().add("hud-stat");
            statistiche.getChildren().add(stat);
        }
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
            mostraPensieroInizioCapitolo(nuovo);
        };
        Runnable vaiAllaScena = () -> context.navigator().mostra(
                new ChapterTitleView(context, titoloScena, vaiAllEsplorazione).getRoot());
        context.navigator().mostra(
                new ChapterTitleView(context, etichettaCapitolo, vaiAllaScena).getRoot());
    }

    /**
     * All'inizio di alcuni capitoli il personaggio esprime un pensiero che ne
     * inquadra l'obiettivo. Nel capitolo 3 lo spinge a investigare al Polo B.
     *
     * @param nuovo il capitolo appena iniziato
     */
    private void mostraPensieroInizioCapitolo(Chapter nuovo) {
        if ("capitolo3".equals(nuovo.getId())) {
            mostraDialogo(stato.getPlayer().getNome(),
                    "Devo assolutamente andare a investigare al polo B prima che sia troppo tardi.");
        }
    }

    /**
     * Se la scena corrente e' terminale e nessun enigma la sbarra piu', conclude il
     * capitolo: per i capitoli intermedi propone la scelta del potenziamento e
     * avanza; per l'ultimo capitolo lascia che sia il motore a segnalare la fine.
     */
    private void verificaCompletamentoCapitolo() {
        // Un overlay aperto (es. la schermata dell'email) attende l'azione dell'utente:
        // non concludere il capitolo finché non viene chiuso col "Continua".
        if (overlayCorrente != null) {
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

    void rimuoviElemento(ElementoScena elemento) {
        elementi.remove(elemento);
        mappa.getChildren().remove(elemento.nodo);
        if (elemento.etichettaNodo != null) {
            mappa.getChildren().remove(elemento.etichettaNodo);
        }
        if (elementoVicino == elemento) {
            elementoVicino = null;
            suggerimento.setText("");
        }
    }

    private void vaiAlMenu() {
        ciclo.stop();
        engine.removeListener(this);
        context.navigator().mostra(new HomeView(context).getRoot());
    }

    private String coloreEnergia(int energia) {
        if (energia > 50) {
            return "#2ecc71";
        }
        if (energia > 20) {
            return "#e67e22";
        }
        return "#c0392b";
    }

    private String icona(StatType tipo) {
        return switch (tipo) {
            case INVESTIGAZIONE -> "🔍";
            case CARISMA -> "💬";
            case INTUIZIONE -> "💡";
        };
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
