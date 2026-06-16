package it.unicam.cs.mpgc.rpg126036.view;

import it.unicam.cs.mpgc.rpg126036.model.CharacterClass;
import it.unicam.cs.mpgc.rpg126036.persistence.PersistenceException;
import it.unicam.cs.mpgc.rpg126036.persistence.SaveRepository;
import it.unicam.cs.mpgc.rpg126036.persistence.SaveSlot;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

import java.io.InputStream;
import java.util.List;
import java.util.Objects;

/**
 * Schermata "Carica partita": elenca i (massimo {@value SaveRepository#MAX_SLOTS})
 * slot di salvataggio presenti nella cartella dei salvataggi del progetto, ciascuno
 * con il nome del personaggio e il tipo di classe. Selezionando uno slot la partita
 * viene ripristinata e si riprende direttamente dall'esplorazione; ogni slot può
 * anche essere eliminato. Gli slot liberi sono mostrati come "vuoti".
 *
 * <p>I salvataggi vengono creati automaticamente alla fine di ogni capitolo
 * ({@code AutoSaveListener}); qui se ne effettua solo il caricamento.</p>
 */
public class LoadGameView {

    /** Risorsa dell'immagine di sfondo, condivisa con la Home. */
    private static final String SFONDO = "/images/home-background.jpg";

    private final AppContext context;
    private final StackPane root;
    // Contenitore delle righe-slot, ricostruito dopo un'eliminazione.
    private final VBox listaSlot = new VBox(12);
    // Overlay di conferma eliminazione, uno alla volta.
    private Node overlayCorrente;

    public LoadGameView(AppContext context) {
        this.context = Objects.requireNonNull(context, "Il contesto non puo' essere nullo.");
        root = new StackPane();
        root.getStyleClass().addAll("screen-root", "pixel-font");
        root.getChildren().addAll(livelloSfondo(), livelloVelo(), livelloContenuto());
        popolaSlot();
    }

    // ----------------------------------------------------------------------
    // Strati di sfondo (coerenti con la Home)
    // ----------------------------------------------------------------------

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

    private Region livelloVelo() {
        Region velo = new Region();
        velo.getStyleClass().add("background-veil");
        return velo;
    }

    // ----------------------------------------------------------------------
    // Contenuto
    // ----------------------------------------------------------------------

    private Node livelloContenuto() {
        Label titolo = new Label("Carica partita");
        // Stesso stile e dimensione del titolo "Nuova partita" (creazione personaggio).
        titolo.getStyleClass().add("title");
        titolo.setStyle("-fx-font-size: 40px;");

        listaSlot.setAlignment(Pos.CENTER);

        Button indietro = new Button("Indietro");
        indietro.getStyleClass().add("game-button");
        indietro.setOnAction(e -> context.navigator().mostra(new HomeView(context).getRoot()));

        VBox centro = new VBox(28, titolo, listaSlot, indietro);
        centro.setAlignment(Pos.CENTER);
        centro.setPadding(new Insets(32));
        return centro;
    }

    /** Ricostruisce l'elenco degli slot: quelli salvati e i rimanenti come vuoti. */
    private void popolaSlot() {
        listaSlot.getChildren().clear();
        List<SaveSlot> slot = context.sessionFactory().slotSalvati();
        for (SaveSlot s : slot) {
            listaSlot.getChildren().add(slotOccupato(s));
        }
        for (int i = slot.size(); i < SaveRepository.MAX_SLOTS; i++) {
            listaSlot.getChildren().add(slotVuoto());
        }
    }

    /** Riga di uno slot salvato: nome e classe, con i pulsanti "Carica" ed "Elimina". */
    private Node slotOccupato(SaveSlot slot) {
        Label nome = new Label(slot.nomePersonaggio());
        nome.getStyleClass().add("slot-name");
        Label dettaglio = new Label(nomeClasse(slot.classe()) + "  ·  " + etichettaCapitolo(slot.idCapitolo()));
        dettaglio.getStyleClass().add("slot-detail");
        VBox info = new VBox(2, nome, dettaglio);
        info.setAlignment(Pos.CENTER_LEFT);

        Button carica = new Button("Carica");
        carica.getStyleClass().add("game-button");
        carica.setOnAction(e -> caricaPartita(slot.nomePersonaggio()));

        Button elimina = new Button("Elimina");
        elimina.getStyleClass().add("game-button");
        elimina.setOnAction(e -> chiediConfermaEliminazione(slot.nomePersonaggio()));

        Region spazio = new Region();
        HBox.setHgrow(spazio, Priority.ALWAYS);
        HBox riga = new HBox(12, info, spazio, carica, elimina);
        riga.setAlignment(Pos.CENTER_LEFT);
        riga.getStyleClass().add("slot-card");
        riga.setMaxWidth(560);
        return riga;
    }

    /** Riga di uno slot libero. */
    private Node slotVuoto() {
        Label etichetta = new Label("Slot vuoto");
        etichetta.getStyleClass().add("slot-empty");
        HBox riga = new HBox(etichetta);
        riga.setAlignment(Pos.CENTER);
        riga.getStyleClass().addAll("slot-card", "slot-card-empty");
        riga.setMaxWidth(560);
        riga.setMinHeight(64);
        return riga;
    }

    // ----------------------------------------------------------------------
    // Azioni
    // ----------------------------------------------------------------------

    /** Carica lo slot e riprende l'esplorazione; segnala un eventuale salvataggio corrotto. */
    private void caricaPartita(String nomePersonaggio) {
        try {
            context.sessionFactory().caricaPartita(nomePersonaggio).ifPresentOrElse(
                    sessione -> context.navigator().mostra(new ExplorationView(context, sessione).getRoot()),
                    () -> mostraConferma("Salvataggio non trovato.", null));
        } catch (PersistenceException e) {
            mostraConferma("Salvataggio illeggibile o corrotto.", null);
        }
    }

    private void chiediConfermaEliminazione(String nomePersonaggio) {
        mostraConferma("Eliminare il salvataggio di " + nomePersonaggio + "?", () -> {
            context.sessionFactory().eliminaPartita(nomePersonaggio);
            popolaSlot();
        });
    }

    /**
     * Mostra un piccolo overlay di messaggio. Se {@code allaConferma} è non nullo
     * compaiono i pulsanti "Sì"/"Annulla" (conferma); altrimenti solo "Chiudi".
     */
    private void mostraConferma(String messaggio, Runnable allaConferma) {
        chiudiOverlay();
        Label testo = new Label(messaggio);
        testo.getStyleClass().add("overlay-subtitle");
        testo.setWrapText(true);
        testo.setMaxWidth(420);

        HBox pulsanti = new HBox(12);
        pulsanti.setAlignment(Pos.CENTER);
        if (allaConferma != null) {
            Button si = new Button("Sì");
            si.getStyleClass().add("game-button");
            si.setOnAction(e -> {
                chiudiOverlay();
                allaConferma.run();
            });
            Button annulla = new Button("Annulla");
            annulla.getStyleClass().add("game-button");
            annulla.setOnAction(e -> chiudiOverlay());
            pulsanti.getChildren().addAll(si, annulla);
        } else {
            Button chiudi = new Button("Chiudi");
            chiudi.getStyleClass().add("game-button");
            chiudi.setOnAction(e -> chiudiOverlay());
            pulsanti.getChildren().add(chiudi);
        }

        VBox pannello = new VBox(20, testo, pulsanti);
        pannello.setAlignment(Pos.CENTER);
        pannello.setMaxWidth(480);
        pannello.getStyleClass().add("pixel-font");

        StackPane velo = new StackPane(pannello);
        velo.getStyleClass().add("overlay-veil");
        overlayCorrente = velo;
        root.getChildren().add(velo);
    }

    private void chiudiOverlay() {
        if (overlayCorrente != null) {
            root.getChildren().remove(overlayCorrente);
            overlayCorrente = null;
        }
    }

    // ----------------------------------------------------------------------
    // Utilità di formattazione
    // ----------------------------------------------------------------------

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
