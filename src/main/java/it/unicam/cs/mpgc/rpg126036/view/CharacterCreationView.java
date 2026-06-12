package it.unicam.cs.mpgc.rpg126036.view;

import it.unicam.cs.mpgc.rpg126036.app.GameSession;
import it.unicam.cs.mpgc.rpg126036.model.CharacterClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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

    private final AppContext context;
    private final VBox root;

    public CharacterCreationView(AppContext context) {
        this.context = Objects.requireNonNull(context, "Il contesto non puo' essere nullo.");

        Label titolo = new Label("Nuova partita");
        titolo.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #c0392b;");

        Label etichettaNome = new Label("Il tuo nome:");
        etichettaNome.setStyle("-fx-text-fill: #ecf0f1;");
        TextField campoNome = new TextField();
        campoNome.setPromptText("Inserisci il nome del personaggio");
        campoNome.setMaxWidth(320);

        Label etichettaClasse = new Label("Scegli il tipo di personaggio:");
        etichettaClasse.setStyle("-fx-text-fill: #ecf0f1;");
        ToggleGroup gruppoClassi = new ToggleGroup();
        HBox carte = new HBox(16);
        carte.setAlignment(Pos.CENTER);
        for (CharacterClass classe : CharacterClass.values()) {
            carte.getChildren().add(cardClasse(classe, gruppoClassi));
        }

        Button inizia = new Button("Inizia");
        inizia.setPrefWidth(160);
        // Abilitato solo con un nome inserito e una classe selezionata.
        inizia.disableProperty().bind(
                campoNome.textProperty().isEmpty().or(gruppoClassi.selectedToggleProperty().isNull()));
        inizia.setOnAction(e -> avviaPartita(campoNome.getText().trim(),
                (CharacterClass) gruppoClassi.getSelectedToggle().getUserData()));

        Button indietro = new Button("Indietro");
        indietro.setPrefWidth(160);
        indietro.setOnAction(e -> context.navigator().mostra(new HomeView(context).getRoot()));

        HBox azioni = new HBox(16, indietro, inizia);
        azioni.setAlignment(Pos.CENTER);

        root = new VBox(20, titolo, etichettaNome, campoNome, etichettaClasse, carte, azioni);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(32));
        root.setStyle("-fx-background-color: #111111;");
    }

    /**
     * Crea una card selezionabile per una classe: nome e statistica caratterizzante.
     * Lo spazio sopra il nome e' previsto per l'immagine del personaggio.
     */
    private ToggleButton cardClasse(CharacterClass classe, ToggleGroup gruppo) {
        Label nome = new Label(classe.getNomeVisualizzato());
        nome.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ecf0f1;");
        Label bonus = new Label("+1 " + classe.statisticaPrincipale().getNomeVisualizzato());
        bonus.setStyle("-fx-text-fill: #c0392b;");

        VBox contenuto = new VBox(6, nome, bonus);
        contenuto.setAlignment(Pos.CENTER);

        ToggleButton card = new ToggleButton();
        card.setGraphic(contenuto);
        card.setToggleGroup(gruppo);
        card.setUserData(classe);
        card.setPrefSize(200, 120);
        return card;
    }

    /**
     * Avvia una nuova partita con il nome e la classe scelti e apre la sequenza
     * introduttiva (monologo, cartello del capitolo, esplorazione).
     */
    private void avviaPartita(String nome, CharacterClass classe) {
        GameSession sessione = context.sessionFactory().nuovaPartita(nome, classe);
        context.navigator().mostra(new IntroView(context, sessione).getRoot());
    }

    /**
     * @return il nodo radice della schermata
     */
    public Parent getRoot() {
        return root;
    }
}
