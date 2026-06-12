package it.unicam.cs.mpgc.rpg126036.view;

import it.unicam.cs.mpgc.rpg126036.app.GameSession;
import it.unicam.cs.mpgc.rpg126036.engine.GameEngine;
import it.unicam.cs.mpgc.rpg126036.engine.GameListener;
import it.unicam.cs.mpgc.rpg126036.engine.GameSummary;
import it.unicam.cs.mpgc.rpg126036.interaction.InteractionResult;
import it.unicam.cs.mpgc.rpg126036.model.Player;
import it.unicam.cs.mpgc.rpg126036.model.StatType;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Schermata di esplorazione: il giocatore si muove liberamente nell'ambientazione
 * (vista dall'alto, stile GdR 2D) con i tasti <b>WASD</b>. In alto è presente l'HUD
 * (energia, XP, statistiche).
 *
 * <p>Registrata come {@link GameListener} sul motore (Observer), mostra i pannelli
 * in sovrimpressione per l'upgrade di statistica, il game over e il completamento.
 * Il personaggio e la mappa sono per ora segnaposto: gli sprite e gli elementi
 * interattivi (NPC, oggetti, uscite) verranno aggiunti in seguito.</p>
 */
public class ExplorationView implements GameListener {

    private static final double MAPPA_LARGHEZZA = 860;
    private static final double MAPPA_ALTEZZA = 440;
    private static final double LATO_PERSONAGGIO = 28;
    private static final double VELOCITA = 3.0;

    private final AppContext context;
    private final GameEngine engine;
    private final StackPane root;

    // HUD.
    private final ProgressBar barraEnergia = new ProgressBar();
    private final Label valoreEnergia = new Label();
    private final Label valoreXp = new Label();
    private final HBox statistiche = new HBox(16);

    // Movimento.
    private final Rectangle personaggio = new Rectangle(LATO_PERSONAGGIO, LATO_PERSONAGGIO, Color.web("#e0483a"));
    private final Set<KeyCode> tastiPremuti = EnumSet.noneOf(KeyCode.class);
    private final AnimationTimer ciclo;
    private double posX = (MAPPA_LARGHEZZA - LATO_PERSONAGGIO) / 2;
    private double posY = (MAPPA_ALTEZZA - LATO_PERSONAGGIO) / 2;

    public ExplorationView(AppContext context, GameSession session) {
        this.context = Objects.requireNonNull(context, "Il contesto non puo' essere nullo.");
        Objects.requireNonNull(session, "La sessione non puo' essere nulla.");
        this.engine = session.getEngine();

        root = new StackPane(costruisciLayout());
        root.getStyleClass().add("screen-root");

        this.ciclo = creaCicloDiGioco();
        collegaInputAllaScena();

        engine.addListener(this);
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
        Pane mappa = new Pane(personaggio);
        mappa.getStyleClass().add("map-pane");
        mappa.setPrefSize(MAPPA_LARGHEZZA, MAPPA_ALTEZZA);
        mappa.setMaxSize(MAPPA_LARGHEZZA, MAPPA_ALTEZZA);
        personaggio.setLayoutX(posX);
        personaggio.setLayoutY(posY);
        personaggio.setArcWidth(8);
        personaggio.setArcHeight(8);

        StackPane contenitore = new StackPane(mappa);
        contenitore.setPadding(new Insets(20));
        return contenitore;
    }

    private Node costruisciBarraInferiore() {
        Label aiuto = new Label("Muoviti con W A S D");
        aiuto.getStyleClass().add("hud-text");

        Button menu = new Button("Esci al menu");
        menu.getStyleClass().add("game-button");
        menu.setOnAction(e -> vaiAlMenu());

        Region spazio = new Region();
        HBox.setHgrow(spazio, Priority.ALWAYS);
        HBox barra = new HBox(aiuto, spazio, menu);
        barra.setAlignment(Pos.CENTER);
        barra.setPadding(new Insets(16, 24, 24, 24));
        return barra;
    }

    // ----------------------------------------------------------------------
    // Movimento
    // ----------------------------------------------------------------------

    private AnimationTimer creaCicloDiGioco() {
        return new AnimationTimer() {
            @Override
            public void handle(long now) {
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
                if (dx != 0 || dy != 0) {
                    posX = clamp(posX + dx, MAPPA_LARGHEZZA - LATO_PERSONAGGIO);
                    posY = clamp(posY + dy, MAPPA_ALTEZZA - LATO_PERSONAGGIO);
                    personaggio.setLayoutX(posX);
                    personaggio.setLayoutY(posY);
                }
            }
        };
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

    private void abilitaInput(Scene scena) {
        scena.setOnKeyPressed(e -> tastiPremuti.add(e.getCode()));
        scena.setOnKeyReleased(e -> tastiPremuti.remove(e.getCode()));
    }

    // ----------------------------------------------------------------------
    // HUD e reazione agli eventi del motore
    // ----------------------------------------------------------------------

    private void aggiornaHud() {
        Player player = engine.getStato().getPlayer();
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
    public void onInteractionExecuted(InteractionResult risultato) {
        aggiornaHud();
    }

    @Override
    public void onUpgradeStatisticaDisponibile() {
        mostraSceltaUpgrade("Upgrade statistica disponibile",
                "Scegli la statistica da potenziare:", engine::applicaUpgrade);
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
    // Pannelli in sovrimpressione
    // ----------------------------------------------------------------------

    private void mostraSceltaUpgrade(String titolo, String sottotitolo, Consumer<StatType> azione) {
        Label etichettaTitolo = new Label(titolo);
        etichettaTitolo.getStyleClass().add("scene-title");
        Label etichettaSub = new Label(sottotitolo);
        etichettaSub.getStyleClass().add("overlay-subtitle");

        VBox opzioni = new VBox(12);
        opzioni.setAlignment(Pos.CENTER);
        VBox pannello = new VBox(20, etichettaTitolo, etichettaSub, opzioni);
        pannello.setAlignment(Pos.CENTER);
        StackPane overlay = velo(pannello);

        Player player = engine.getStato().getPlayer();
        for (StatType tipo : StatType.values()) {
            Button scelta = new Button(icona(tipo) + " " + tipo.getNomeVisualizzato()
                    + " (attuale: " + player.getStatistica(tipo) + ")");
            scelta.getStyleClass().add("game-button");
            scelta.setMaxWidth(360);
            scelta.setOnAction(e -> {
                root.getChildren().remove(overlay);
                azione.accept(tipo);
                aggiornaHud();
            });
            opzioni.getChildren().add(scelta);
        }
        root.getChildren().add(overlay);
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
        root.getChildren().add(velo(pannello));
    }

    private StackPane velo(Node contenuto) {
        StackPane overlay = new StackPane(contenuto);
        overlay.getStyleClass().add("overlay-veil");
        return overlay;
    }

    // ----------------------------------------------------------------------
    // Utilità
    // ----------------------------------------------------------------------

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
}
