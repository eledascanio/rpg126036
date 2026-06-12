package it.unicam.cs.mpgc.rpg126036.view;

import it.unicam.cs.mpgc.rpg126036.app.ContentResolver;
import it.unicam.cs.mpgc.rpg126036.app.GameSession;
import it.unicam.cs.mpgc.rpg126036.engine.GameEngine;
import it.unicam.cs.mpgc.rpg126036.engine.GameListener;
import it.unicam.cs.mpgc.rpg126036.engine.GameState;
import it.unicam.cs.mpgc.rpg126036.engine.GameSummary;
import it.unicam.cs.mpgc.rpg126036.interaction.InteractionResult;
import it.unicam.cs.mpgc.rpg126036.interaction.ItemInteraction;
import it.unicam.cs.mpgc.rpg126036.model.Clue;
import it.unicam.cs.mpgc.rpg126036.model.Npc;
import it.unicam.cs.mpgc.rpg126036.model.Player;
import it.unicam.cs.mpgc.rpg126036.model.Puzzle;
import it.unicam.cs.mpgc.rpg126036.model.PuzzleOutcome;
import it.unicam.cs.mpgc.rpg126036.model.Scene;
import it.unicam.cs.mpgc.rpg126036.model.StatType;
import it.unicam.cs.mpgc.rpg126036.model.Transition;
import it.unicam.cs.mpgc.rpg126036.persistence.Campaign;
import it.unicam.cs.mpgc.rpg126036.persistence.SceneContents;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
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
    private static final double VELOCITA = 3.0;
    private static final double RAGGIO_ELEMENTO = 16;
    private static final double RAGGIO_INTERAZIONE = 52;

    private final AppContext context;
    private final GameEngine engine;
    private final GameState stato;
    private final Campaign campaign;
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
    private final Rectangle personaggio = new Rectangle(LATO_PERSONAGGIO, LATO_PERSONAGGIO, Color.web("#e0483a"));
    private final Set<KeyCode> tastiPremuti = EnumSet.noneOf(KeyCode.class);
    private final List<ElementoScena> elementi = new ArrayList<>();
    private final SceneEnvironment ambienti = new SceneEnvironment();
    private final List<Rectangle2D> muri = new ArrayList<>();
    private final AnimationTimer ciclo;
    private double posX = (MAPPA_LARGHEZZA - LATO_PERSONAGGIO) / 2;
    private double posY = (MAPPA_ALTEZZA - LATO_PERSONAGGIO) / 2;
    private ElementoScena elementoVicino;

    // Un solo pannello modale alla volta in sovrimpressione.
    private Node overlayCorrente;
    private boolean overlayChiudibile;

    public ExplorationView(AppContext context, GameSession session) {
        this.context = Objects.requireNonNull(context, "Il contesto non puo' essere nullo.");
        Objects.requireNonNull(session, "La sessione non puo' essere nulla.");
        this.engine = session.getEngine();
        this.stato = session.getStato();
        this.campaign = session.getCampaign();
        this.resolver = context.contentResolver();

        root = new StackPane(costruisciLayout());
        root.getStyleClass().add("screen-root");

        this.ciclo = creaCicloDiGioco();
        collegaInputAllaScena();

        engine.addListener(this);
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
        personaggio.setLayoutX(posX);
        personaggio.setLayoutY(posY);
        personaggio.setArcWidth(8);
        personaggio.setArcHeight(8);

        titoloScena.getStyleClass().add("scene-title");
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

        Scene scena = engine.getScenaCorrente();
        titoloScena.setText(scena.getTitolo());

        // Sfondo, ostacoli e punto di comparsa dipendono dall'ambiente della scena.
        // Lo sfondo va aggiunto come primo figlio (sotto personaggio ed elementi).
        Optional<SceneEnvironment.Ambiente> ambiente = ambienti.di(scena.getId());
        ambiente.ifPresent(this::applicaAmbiente);

        mappa.getChildren().add(personaggio);

        String idCapitolo = engine.getCapitoloCorrente().getId();
        SceneContents contenuti = campaign.contenutiDi(idCapitolo, scena.getId());

        for (String idNpc : contenuti.npc()) {
            resolver.npc(idNpc).ifPresent(this::aggiungiNpc);
        }
        for (String idOggetto : contenuti.oggetti()) {
            resolver.item(idOggetto).ifPresent(item -> aggiungiOggetto(new ItemInteraction(item)));
        }
        for (String idEnigma : contenuti.enigmi()) {
            resolver.creaEnigma(idEnigma, stato).ifPresent(this::aggiungiEnigma);
        }
        for (Transition transizione : engine.transizioniDisponibili()) {
            aggiungiUscita(transizione);
        }

        disponiElementi(ambiente.orElse(null));
        posizionaPersonaggioIniziale(ambiente.orElse(null));
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
        if (ambiente != null) {
            posX = ambiente.spawnX() * MAPPA_LARGHEZZA - LATO_PERSONAGGIO / 2;
            posY = ambiente.spawnY() * MAPPA_ALTEZZA - LATO_PERSONAGGIO / 2;
        } else {
            posX = (MAPPA_LARGHEZZA - LATO_PERSONAGGIO) / 2;
            posY = MAPPA_ALTEZZA - LATO_PERSONAGGIO - 8;
        }
        personaggio.setLayoutX(posX);
        personaggio.setLayoutY(posY);
    }

    private void aggiungiNpc(Npc npc) {
        ElementoScena e = new ElementoScena(TipoElemento.NPC, npc.getNome(),
                "Parla con " + npc.getNome(), Color.web("#4a90d9"));
        e.azione = () -> interagisciConNpc(npc);
        registra(e);
    }

    private void aggiungiOggetto(ItemInteraction oggetto) {
        ElementoScena e = new ElementoScena(TipoElemento.OGGETTO, oggetto.getItem().nome(),
                "Raccogli " + oggetto.getItem().nome(), Color.web("#e0c43a"));
        e.azione = () -> raccogliOggetto(oggetto, e);
        registra(e);
    }

    private void aggiungiEnigma(Puzzle puzzle) {
        ElementoScena e = new ElementoScena(TipoElemento.ENIGMA, "Enigma", "Esamina l'enigma", Color.web("#9b59b6"));
        e.puzzle = puzzle;
        e.azione = () -> mostraEnigma(puzzle, e);
        registra(e);
    }

    private void aggiungiUscita(Transition transizione) {
        ElementoScena e = new ElementoScena(TipoElemento.USCITA, transizione.etichetta(),
                transizione.etichetta(), Color.web("#2ecc71"));
        e.azione = () -> usaUscita(transizione);
        registra(e);
    }

    private void registra(ElementoScena e) {
        elementi.add(e);
        mappa.getChildren().addAll(e.forma, e.etichettaNodo);
    }

    /**
     * Posiziona gli elementi sugli slot dell'ambiente, se definiti (sul pavimento,
     * lontano dai muri); altrimenti ripiega sulla disposizione automatica a fasce.
     */
    private void disponiElementi(SceneEnvironment.Ambiente ambiente) {
        if (ambiente != null && !ambiente.slot().isEmpty()) {
            disponiSuSlot(ambiente.slot());
        } else {
            disponiAFasce();
        }
    }

    private void disponiSuSlot(List<SceneEnvironment.Punto> slot) {
        for (int i = 0; i < elementi.size(); i++) {
            if (i < slot.size()) {
                elementi.get(i).posiziona(slot.get(i).x() * MAPPA_LARGHEZZA, slot.get(i).y() * MAPPA_ALTEZZA);
            } else {
                // Piu' elementi che slot: fallback in basso, distribuiti in larghezza.
                int extra = i - slot.size();
                double x = MAPPA_LARGHEZZA * (extra + 1.0) / (elementi.size() - slot.size() + 1);
                elementi.get(i).posiziona(x, MAPPA_ALTEZZA - 48);
            }
        }
    }

    /**
     * Dispone gli elementi su tre fasce orizzontali (NPC in alto, oggetti ed enigmi
     * al centro, uscite in basso), distribuendoli uniformemente in larghezza.
     */
    private void disponiAFasce() {
        List<ElementoScena> npc = new ArrayList<>();
        List<ElementoScena> centro = new ArrayList<>();
        List<ElementoScena> uscite = new ArrayList<>();
        for (ElementoScena e : elementi) {
            switch (e.tipo) {
                case NPC -> npc.add(e);
                case OGGETTO, ENIGMA -> centro.add(e);
                case USCITA -> uscite.add(e);
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
                    if (!collide(nuovaX, posY)) {
                        posX = nuovaX;
                        personaggio.setLayoutX(posX);
                    }
                }
                if (dy != 0) {
                    double nuovaY = clamp(posY + dy, MAPPA_ALTEZZA - LATO_PERSONAGGIO);
                    if (!collide(posX, nuovaY)) {
                        posY = nuovaY;
                        personaggio.setLayoutY(posY);
                    }
                }
                aggiornaElementoVicino();
            }
        };
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
            e.forma.setStrokeWidth(attivo ? 3 : 0);
        }
    }

    private double clamp(double valore, double massimo) {
        return Math.max(0, Math.min(valore, massimo));
    }

    /**
     * @return {@code true} se il personaggio, posizionato in (x, y), si
     *         sovrapporrebbe a un muro della scena
     */
    private boolean collide(double x, double y) {
        for (Rectangle2D muro : muri) {
            if (muro.intersects(x, y, LATO_PERSONAGGIO, LATO_PERSONAGGIO)) {
                return true;
            }
        }
        return false;
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
        String battuta = npc.parla(player);

        StringBuilder testo = new StringBuilder(battuta);
        if (npc.getDialogo().isAccessibile(player)) {
            // L'aver parlato con l'assistente abilita la via "Carisma" del PC della vittima.
            if ("tecnico_laboratorio".equals(npc.getId())) {
                stato.setFlag(ContentResolver.FLAG_ASSISTENTE);
            }
            resolver.indizioDi(npc.getId()).ifPresent(indizio -> {
                if (engine.trovaIndizio(indizio)) {
                    testo.append("\n\n🔎 Nuovo indizio nel diario: ").append(indizio.titolo());
                }
            });
        }
        mostraMessaggio(npc.getNome(), testo.toString());
    }

    private void raccogliOggetto(ItemInteraction oggetto, ElementoScena elemento) {
        InteractionResult esito = engine.raccogli(oggetto);
        rimuoviElemento(elemento);
        aggiornaHud();
        mostraMessaggio("Oggetto", esito.messaggio());
    }

    private void usaUscita(Transition transizione) {
        if (haEnigmaNonRisolto()) {
            mostraMessaggio("Passaggio bloccato",
                    "Un enigma sbarra ancora la strada: risolvilo prima di proseguire.");
            return;
        }
        engine.avanza(transizione.idDestinazione());
    }

    private boolean haEnigmaNonRisolto() {
        return elementi.stream()
                .anyMatch(e -> e.tipo == TipoElemento.ENIGMA && e.puzzle != null && !e.puzzle.isRisolto());
    }

    // ----------------------------------------------------------------------
    // Pannelli in sovrimpressione
    // ----------------------------------------------------------------------

    private void mostraMessaggio(String titolo, String corpo) {
        Label etichettaTitolo = new Label(titolo);
        etichettaTitolo.getStyleClass().add("scene-title");
        Label etichettaCorpo = new Label(corpo);
        etichettaCorpo.getStyleClass().add("overlay-subtitle");
        etichettaCorpo.setWrapText(true);
        etichettaCorpo.setMaxWidth(560);

        Button chiudi = new Button("Chiudi");
        chiudi.getStyleClass().add("game-button");
        chiudi.setOnAction(e -> chiudiOverlay());

        VBox pannello = new VBox(20, etichettaTitolo, etichettaCorpo, chiudi);
        pannello.setAlignment(Pos.CENTER);
        mostraOverlay(velo(pannello), true);
    }

    private void mostraEnigma(Puzzle puzzle, ElementoScena elemento) {
        if (puzzle.isRisolto()) {
            mostraMessaggio("Enigma", "Hai già superato questo enigma.");
            return;
        }
        Player player = stato.getPlayer();

        Label etichettaTitolo = new Label("Enigma");
        etichettaTitolo.getStyleClass().add("scene-title");
        Label etichettaTesto = new Label(puzzle.getTesto());
        etichettaTesto.getStyleClass().add("overlay-subtitle");
        etichettaTesto.setWrapText(true);
        etichettaTesto.setMaxWidth(560);

        VBox pannello = new VBox(16, etichettaTitolo, etichettaTesto);
        pannello.setAlignment(Pos.CENTER);

        for (String suggerimentoEnigma : puzzle.suggerimentiPer(player)) {
            Label nota = new Label("• " + suggerimentoEnigma);
            nota.getStyleClass().add("overlay-subtitle");
            nota.setWrapText(true);
            nota.setMaxWidth(560);
            pannello.getChildren().add(nota);
        }

        Label esito = new Label();
        esito.getStyleClass().add("overlay-subtitle");
        esito.setWrapText(true);
        esito.setMaxWidth(560);

        TextField tentativo = new TextField();
        tentativo.setPromptText("La tua risposta");
        tentativo.setMaxWidth(220);

        Button tenta = new Button("Tenta");
        tenta.getStyleClass().add("game-button");
        Button forza = new Button("Forza bruta (perdi energia)");
        forza.getStyleClass().add("game-button");

        Consumer<PuzzleOutcome> gestisci = outcome -> {
            esito.setText(outcome.messaggio());
            if (outcome.risolto()) {
                enigmaRisolto(elemento);
            }
        };
        tenta.setOnAction(e -> gestisci.accept(puzzle.tenta(player, tentativo.getText())));
        forza.setOnAction(e -> gestisci.accept(puzzle.forzaBruta(player)));

        pannello.getChildren().addAll(new HBox(8, tentativo, tenta), forza, esito);
        mostraOverlay(velo(pannello), true);
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

    private void mostraOverlayFinale(String titolo, String sottotitolo) {
        Label etichettaTitolo = new Label(titolo);
        etichettaTitolo.getStyleClass().add("overlay-title");
        Label etichettaSub = new Label(sottotitolo);
        etichettaSub.getStyleClass().add("overlay-subtitle");
        etichettaSub.setWrapText(true);
        etichettaSub.setMaxWidth(520);

        Button menu = new Button("Torna al menu principale");
        menu.getStyleClass().add("game-button");
        menu.setOnAction(e -> vaiAlMenu());

        VBox pannello = new VBox(24, etichettaTitolo, etichettaSub, menu);
        pannello.setAlignment(Pos.CENTER);
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

    private void chiudiOverlay() {
        if (overlayCorrente != null) {
            root.getChildren().remove(overlayCorrente);
            overlayCorrente = null;
        }
    }

    // ----------------------------------------------------------------------
    // Reazione agli eventi del motore
    // ----------------------------------------------------------------------

    private void aggiornaHud() {
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
        costruisciElementiScena();
        aggiornaHud();
        verificaCompletamentoCapitolo();
    }

    /**
     * Se la scena corrente e' terminale e nessun enigma la sbarra piu', conclude il
     * capitolo: per i capitoli intermedi propone la scelta del potenziamento e
     * avanza; per l'ultimo capitolo lascia che sia il motore a segnalare la fine.
     */
    private void verificaCompletamentoCapitolo() {
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
        mostraSceltaUpgrade("Upgrade statistica disponibile",
                "Scegli la statistica da potenziare:", engine::applicaUpgrade);
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

    private void rimuoviElemento(ElementoScena elemento) {
        elementi.remove(elemento);
        mappa.getChildren().removeAll(elemento.forma, elemento.etichettaNodo);
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

    private enum TipoElemento {NPC, OGGETTO, ENIGMA, USCITA}

    /**
     * Elemento interattivo posizionato sulla mappa: una forma colorata con
     * un'etichetta, un'azione di interazione e, per gli enigmi, il relativo
     * {@link Puzzle}.
     */
    private static final class ElementoScena {

        private final TipoElemento tipo;
        private final String etichettaAzione;
        private final Circle forma;
        private final Label etichettaNodo;
        private double x;
        private double y;
        private Runnable azione;
        private Puzzle puzzle;

        ElementoScena(TipoElemento tipo, String nome, String etichettaAzione, Color colore) {
            this.tipo = tipo;
            this.etichettaAzione = etichettaAzione;
            this.forma = new Circle(RAGGIO_ELEMENTO, colore);
            this.forma.setStroke(Color.WHITE);
            this.forma.setStrokeWidth(0);
            this.etichettaNodo = new Label(nome);
            this.etichettaNodo.getStyleClass().add("hud-text");
            this.etichettaNodo.setTextAlignment(TextAlignment.CENTER);
            this.etichettaNodo.setPrefWidth(140);
            this.etichettaNodo.setAlignment(Pos.CENTER);
        }

        void posiziona(double x, double y) {
            this.x = x;
            this.y = y;
            forma.setCenterX(x);
            forma.setCenterY(y);
            etichettaNodo.setLayoutX(x - etichettaNodo.getPrefWidth() / 2);
            etichettaNodo.setLayoutY(y + RAGGIO_ELEMENTO + 2);
        }
    }
}
