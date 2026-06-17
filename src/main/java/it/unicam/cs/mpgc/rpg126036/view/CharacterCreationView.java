package it.unicam.cs.mpgc.rpg126036.view;

import it.unicam.cs.mpgc.rpg126036.app.GameSession;
import it.unicam.cs.mpgc.rpg126036.model.CharacterClass;
import it.unicam.cs.mpgc.rpg126036.persistence.SaveRepository;
import it.unicam.cs.mpgc.rpg126036.persistence.SaveSlot;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

/**
 * Schermata di creazione del personaggio: il giocatore inserisce il nome e
 * sceglie il tipo di personaggio. Ogni opzione mostra la statistica su cui ha il
 * bonus rispetto agli altri; il layout della card e' predisposto per accogliere
 * in futuro l'immagine del personaggio sopra il nome.
 *
 * <p>Alla conferma crea una nuova partita tramite la
 * {@link it.unicam.cs.mpgc.rpg126036.app.GameSessionFactory} del contesto.</p>
 */
public class CharacterCreationView {

    /** Risorsa dell'immagine di sfondo, condivisa con la Home per continuita' visiva. */
    private static final String SFONDO = "/images/home-background.jpg";

    private final AppContext context;
    private final StackPane root;
    // Overlay di scelta dello slot da sovrascrivere (quando i salvataggi sono pieni).
    private Node overlayCorrente;

    public CharacterCreationView(AppContext context) {
        this.context = Objects.requireNonNull(context, "Il contesto non puo' essere nullo.");

        Label titolo = new Label("Nuova partita");
        // Stesso stile del titolo della Home (colore, grassetto, ombra), leggermente piu' grande.
        titolo.getStyleClass().add("title");
        titolo.setStyle("-fx-font-size: 40px;");

        Label etichettaNome = new Label("Il tuo nome:");
        etichettaNome.getStyleClass().add("body-text");
        etichettaNome.setStyle("-fx-font-size: 22px;");
        TextField campoNome = new TextField();
        campoNome.setPromptText("Inserisci il nome del personaggio");
        campoNome.setMaxWidth(320);

        Label etichettaClasse = new Label("Scegli il tipo di personaggio:");
        etichettaClasse.getStyleClass().add("body-text");
        etichettaClasse.setStyle("-fx-font-size: 22px;");
        ToggleGroup gruppoClassi = new ToggleGroup();
        HBox carte = new HBox(16);
        carte.setAlignment(Pos.CENTER);
        for (CharacterClass classe : CharacterClass.values()) {
            carte.getChildren().add(cardClasse(classe, gruppoClassi));
        }

        Button inizia = new Button("Inizia");
        inizia.getStyleClass().add("menu-button");
        inizia.setStyle("-fx-alignment: center;");
        inizia.setPrefWidth(160);
        // Abilitato solo con un nome inserito e una classe selezionata.
        inizia.disableProperty().bind(
                campoNome.textProperty().isEmpty().or(gruppoClassi.selectedToggleProperty().isNull()));
        inizia.setOnAction(e -> avviaPartita(campoNome.getText().trim(),
                (CharacterClass) gruppoClassi.getSelectedToggle().getUserData()));

        Button indietro = new Button("Indietro");
        indietro.getStyleClass().add("menu-button");
        indietro.setStyle("-fx-alignment: center;");
        indietro.setPrefWidth(160);
        indietro.setOnAction(e -> context.navigator().mostra(new HomeView(context).getRoot()));

        HBox azioni = new HBox(16, indietro, inizia);
        azioni.setAlignment(Pos.CENTER);

        VBox contenuto = new VBox(20, titolo, etichettaNome, campoNome, etichettaClasse, carte, azioni);
        contenuto.setAlignment(Pos.CENTER);
        contenuto.setPadding(new Insets(32));

        root = new StackPane(livelloSfondo(), livelloVelo(), livelloOverlay(), contenuto);
        // Font pixel art applicato all'intera schermata: eredita a titolo, etichette e pulsanti.
        root.getStyleClass().add("pixel-font");
    }

    /** Overlay scuro semitrasparente, esclusivo di questa schermata. */
    private Region livelloOverlay() {
        Region overlay = new Region();
        overlay.getStyleClass().add("creation-overlay");
        return overlay;
    }

    /** Immagine di sfondo ridimensionata a coprire l'intera area, identica alla Home. */
    private Region livelloSfondo() {
        Region sfondo = new Region();
        InputStream risorsa = getClass().getResourceAsStream(SFONDO);
        if (risorsa != null) {
            BackgroundSize cover = new BackgroundSize(
                    BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, false, true);
            sfondo.setBackground(new Background(new BackgroundImage(new Image(risorsa),
                    BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER, cover)));
        }
        return sfondo;
    }

    /** Velo scuro sopra l'immagine, per il contrasto del testo. */
    private Region livelloVelo() {
        Region velo = new Region();
        velo.getStyleClass().add("background-veil");
        return velo;
    }

    /** Dimensioni uniformi delle card e dei loro blocchi interni (in pixel). */
    private static final double LARGHEZZA_CARD = 200;
    private static final double ALTEZZA_CARD = 250;
    private static final double ALTEZZA_SPRITE_CARD = 110;
    // Altezza riservata al nome: fissa e pari a due righe, così le card con nome
    // su una sola riga restano comunque alte uguale e tutto resta allineato.
    private static final double ALTEZZA_NOME = 60;

    /**
     * Crea una card selezionabile per una classe: sprite frontale, nome e
     * statistica caratterizzante. Tutti i blocchi hanno altezza fissa identica
     * tra le card, così sprite e testi risultano allineati e centrati.
     */
    private ToggleButton cardClasse(CharacterClass classe, ToggleGroup gruppo) {
        // Sprite frontale della classe: distingue le tre opzioni a colpo d'occhio.
        var sprite = new CharacterSprite(classe).nodo(CharacterSprite.Direzione.GIU, ALTEZZA_SPRITE_CARD);
        // Riquadro di altezza fissa con lo sprite appoggiato in basso: i "piedi"
        // dei tre personaggi cadono tutti sulla stessa linea.
        StackPane riquadroSprite = new StackPane(sprite);
        riquadroSprite.setPrefHeight(ALTEZZA_SPRITE_CARD);
        riquadroSprite.setMinHeight(ALTEZZA_SPRITE_CARD);
        riquadroSprite.setAlignment(Pos.BOTTOM_CENTER);

        Label nome = new Label(classe.getNomeVisualizzato());
        // Testo chiaro: la card ora ha sfondo scuro come i pulsanti del tema.
        nome.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #e5e2e1;");
        // Nomi lunghi (es. "Rappresentante degli studenti") vanno a capo, centrati.
        nome.setWrapText(true);
        nome.setTextAlignment(TextAlignment.CENTER);
        nome.setAlignment(Pos.CENTER);
        // Blocco nome ad altezza fissa: uniforma le card a una o due righe.
        StackPane riquadroNome = new StackPane(nome);
        riquadroNome.setPrefHeight(ALTEZZA_NOME);
        riquadroNome.setMinHeight(ALTEZZA_NOME);
        riquadroNome.setMaxWidth(LARGHEZZA_CARD - 24);
        riquadroNome.setAlignment(Pos.CENTER);

        Label bonus = new Label("+1 " + classe.statisticaPrincipale().getNomeVisualizzato());
        bonus.setStyle("-fx-font-size: 18px; -fx-text-fill: #c0392b;");

        VBox contenuto = new VBox(8, riquadroSprite, riquadroNome, bonus);
        // I blocchi hanno altezza fissa identica fra le card: basta centrarli,
        // così il contenuto resta verticalmente equilibrato nel bottone.
        contenuto.setAlignment(Pos.CENTER);

        ToggleButton card = new ToggleButton();
        card.setGraphic(contenuto);
        card.setToggleGroup(gruppo);
        card.setUserData(classe);
        card.setPrefSize(LARGHEZZA_CARD, ALTEZZA_CARD);
        card.setAlignment(Pos.CENTER);
        return card;
    }

    /**
     * Avvia una nuova partita con il nome e la classe scelti. Se i tre slot di
     * salvataggio sono pieni e il nome non corrisponde a un salvataggio esistente,
     * l'autosalvataggio di fine capitolo non avrebbe spazio (vedi
     * {@link it.unicam.cs.mpgc.rpg126036.app.GameSessionFactory#slotPieni()}): si
     * chiede prima quale slot sovrascrivere, poi si procede.
     */
    private void avviaPartita(String nome, CharacterClass classe) {
        if (context.sessionFactory().slotPieni() && !context.sessionFactory().esisteSlot(nome)) {
            chiediSlotDaLiberare(nome, classe);
        } else {
            creaEAvvia(nome, classe);
        }
    }

    /**
     * Crea la partita e apre la sequenza introduttiva (monologo, cartello del
     * capitolo, esplorazione).
     */
    private void creaEAvvia(String nome, CharacterClass classe) {
        GameSession sessione = context.sessionFactory().nuovaPartita(nome, classe);
        context.navigator().mostra(new IntroView(context, sessione).getRoot());
    }

    /**
     * Mostra l'overlay che, con gli slot pieni, chiede quale salvataggio
     * sovrascrivere: ogni slot esistente offre un pulsante che lo elimina e avvia
     * subito la nuova partita. "Annulla" chiude l'overlay senza modifiche.
     */
    private void chiediSlotDaLiberare(String nome, CharacterClass classe) {
        Label titolo = new Label("Slot di salvataggio pieni");
        titolo.getStyleClass().add("scene-title");
        titolo.setTextAlignment(TextAlignment.CENTER);
        Label sottotitolo = new Label("Hai gia' " + SaveRepository.MAX_SLOTS
                + " partite salvate. Scegli quale sovrascrivere per iniziare la nuova partita.");
        sottotitolo.getStyleClass().add("overlay-subtitle");
        sottotitolo.setWrapText(true);
        sottotitolo.setMaxWidth(460);
        sottotitolo.setTextAlignment(TextAlignment.CENTER);

        VBox lista = new VBox(12);
        lista.setAlignment(Pos.CENTER);
        List<SaveSlot> slot = context.sessionFactory().slotSalvati();
        for (SaveSlot s : slot) {
            lista.getChildren().add(rigaSlotDaLiberare(s, nome, classe));
        }

        Button annulla = new Button("Annulla");
        annulla.getStyleClass().add("game-button");
        annulla.setOnAction(e -> chiudiOverlay());

        VBox pannello = new VBox(20, titolo, sottotitolo, lista, annulla);
        pannello.setAlignment(Pos.CENTER);
        pannello.setMaxWidth(560);
        pannello.getStyleClass().add("pixel-font");

        StackPane velo = new StackPane(pannello);
        velo.getStyleClass().add("overlay-veil");
        mostraOverlay(velo);
    }

    /** Riga di uno slot sovrascrivibile: nome, classe e capitolo, col pulsante "Sovrascrivi". */
    private Node rigaSlotDaLiberare(SaveSlot slot, String nome, CharacterClass classe) {
        Label info = new Label(slot.nomePersonaggio() + "  ·  " + nomeClasse(slot.classe())
                + "  ·  " + etichettaCapitolo(slot.idCapitolo()));
        info.getStyleClass().add("slot-name");

        Button sovrascrivi = new Button("Sovrascrivi");
        sovrascrivi.getStyleClass().add("game-button");
        sovrascrivi.setOnAction(e -> {
            context.sessionFactory().eliminaPartita(slot.nomePersonaggio());
            chiudiOverlay();
            creaEAvvia(nome, classe);
        });

        Region spazio = new Region();
        HBox.setHgrow(spazio, Priority.ALWAYS);
        HBox riga = new HBox(12, info, spazio, sovrascrivi);
        riga.setAlignment(Pos.CENTER_LEFT);
        riga.getStyleClass().add("slot-card");
        riga.setMaxWidth(500);
        return riga;
    }

    private void mostraOverlay(Node overlay) {
        chiudiOverlay();
        overlayCorrente = overlay;
        root.getChildren().add(overlay);
    }

    private void chiudiOverlay() {
        if (overlayCorrente != null) {
            root.getChildren().remove(overlayCorrente);
            overlayCorrente = null;
        }
    }

    /** @return il nome leggibile della classe, o l'id grezzo se non riconosciuto */
    private String nomeClasse(String enumName) {
        try {
            return CharacterClass.valueOf(enumName).getNomeVisualizzato();
        } catch (IllegalArgumentException e) {
            return enumName;
        }
    }

    /** @return "Capitolo N" ricavato dall'id (es. "capitolo2" → "Capitolo 2") */
    private String etichettaCapitolo(String idCapitolo) {
        if (idCapitolo == null || idCapitolo.isBlank()) {
            return "—";
        }
        String numero = idCapitolo.replaceAll("\\D+", "");
        return numero.isBlank() ? idCapitolo : "Capitolo " + numero;
    }

    /**
     * @return il nodo radice della schermata
     */
    public Parent getRoot() {
        return root;
    }
}
