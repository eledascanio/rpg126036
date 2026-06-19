package it.unicam.cs.mpgc.rpg126036.view;

import it.unicam.cs.mpgc.rpg126036.model.Player;
import it.unicam.cs.mpgc.rpg126036.model.PuzzleOutcome;
import it.unicam.cs.mpgc.rpg126036.model.StatType;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Factory dei pannelli in sovrimpressione della schermata di esplorazione: messaggi,
 * scene narrative, scelta del potenziamento, enigma a tastierino, schermate finali e
 * pausa. Ogni metodo si occupa esclusivamente di assemblare i nodi JavaFX (etichette,
 * pulsanti, contenitori) e i relativi stili, ricevendo dalla schermata i testi e le
 * azioni (callback) da eseguire all'interazione dell'utente.
 *
 * <p>Estratta dalla {@link ExplorationView} per separare la <i>costruzione</i> visiva
 * dei pannelli dall'<i>orchestrazione</i> del gioco (avanzamento del motore, stato,
 * navigazione), che resta nella schermata. I metodi restituiscono il contenuto del
 * pannello, che la schermata avvolge nel velo e mostra come overlay.</p>
 */
final class PannelliEsplorazione {

    private PannelliEsplorazione() {
    }

    /**
     * Pannello a messaggio: titolo, corpo e un pulsante "Chiudi" che esegue
     * {@code allaChiusura} (utile per concatenare un dialogo successivo).
     */
    static VBox messaggio(String titolo, String corpo, Runnable allaChiusura) {
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
        return pannello;
    }

    /**
     * Pannello di una scena narrativa: titolo, testo e un pulsante "Continua" che
     * esegue {@code continua} (conclusione del capitolo).
     */
    static VBox overlayNarrativo(String titolo, String corpo, Runnable continua) {
        Label etichettaTitolo = new Label(titolo);
        etichettaTitolo.getStyleClass().add("overlay-title");
        Label etichettaCorpo = new Label(corpo);
        etichettaCorpo.getStyleClass().add("overlay-subtitle");
        etichettaCorpo.setWrapText(true);
        etichettaCorpo.setMaxWidth(560);

        Button bottoneContinua = new Button("Continua");
        bottoneContinua.getStyleClass().add("game-button");
        bottoneContinua.setOnAction(e -> continua.run());

        VBox pannello = new VBox(24, etichettaTitolo, etichettaCorpo, bottoneContinua);
        pannello.setAlignment(Pos.CENTER);
        return pannello;
    }

    /**
     * Pannello di scelta del potenziamento: titolo, sottotitolo e un pulsante per
     * ciascuna statistica (col valore corrente). Alla pressione esegue {@code scelta}
     * con il tipo selezionato.
     */
    static VBox sceltaUpgrade(String titolo, String sottotitolo, Player player, Consumer<StatType> scelta) {
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

        for (StatType tipo : StatType.values()) {
            Button bottone = new Button(tipo.getIcona() + " " + tipo.getNomeVisualizzato()
                    + " (attuale: " + player.getStatistica(tipo) + ")");
            bottone.getStyleClass().add("game-button");
            bottone.setMaxWidth(360);
            bottone.setOnAction(e -> scelta.accept(tipo));
            opzioni.getChildren().add(bottone);
        }
        return pannello;
    }

    /**
     * Pannello dell'enigma a combinazione numerica (porta del laboratorio): tastierino
     * a quattro cifre, pulsante di forza bruta e, se presenti, il pensiero-indizio del
     * giocatore come dialog box in basso. La valutazione dei tentativi è delegata alle
     * callback {@code tenta}/{@code forzaBruta}; quando l'esito è risolto si esegue
     * {@code onRisolto}. Restituisce il velo completo, pronto per essere mostrato.
     *
     * @param testoEnigma   testo di presentazione dell'enigma
     * @param pensieri      indizi sbloccati dalle statistiche (vuoto se nessuno)
     * @param nomeGiocatore nome del giocatore, per il box pensiero
     * @param tenta         valuta un tentativo esplicito (codice digitato)
     * @param forzaBruta    sblocca con la forza bruta (penalità di energia)
     * @param onRisolto     azione da eseguire quando l'enigma è risolto
     */
    static StackPane enigmaTastierino(String testoEnigma, List<String> pensieri, String nomeGiocatore,
                                      Function<String, PuzzleOutcome> tenta, Supplier<PuzzleOutcome> forzaBruta,
                                      Runnable onRisolto) {
        Label etichettaTitolo = new Label("Tastierino numerico");
        etichettaTitolo.getStyleClass().add("scene-title");
        Label etichettaTesto = new Label(testoEnigma);
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
            PuzzleOutcome outcome = tenta.apply(codice);
            esito.setText(outcome.messaggio());
            if (outcome.risolto()) {
                onRisolto.run();
                return true;
            }
            return false;
        });

        Button forza = new Button("Forza bruta (perdi energia)");
        forza.getStyleClass().add("game-button");
        forza.setOnAction(e -> {
            PuzzleOutcome outcome = forzaBruta.get();
            esito.setText(outcome.messaggio());
            if (outcome.risolto()) {
                onRisolto.run();
            }
        });

        VBox pannello = new VBox(16, etichettaTitolo, etichettaTesto, tastierino, forza, esito);
        pannello.setAlignment(Pos.CENTER);
        pannello.setMaxWidth(480);

        BorderPane contenuto = new BorderPane(pannello);
        if (!pensieri.isEmpty()) {
            Node boxPensiero = boxPensiero(nomeGiocatore, String.join("\n\n", pensieri));
            contenuto.setBottom(boxPensiero);
            BorderPane.setMargin(boxPensiero, new Insets(0, 16, 16, 16));
        }

        StackPane velo = new StackPane(contenuto);
        velo.getStyleClass().addAll("overlay-veil", "pixel-font");
        return velo;
    }

    /**
     * Costruisce un dialog box statico (nome + testo) nello stile delle finestre di
     * dialogo, usato per il pensiero del giocatore durante l'enigma.
     */
    static Node boxPensiero(String nome, String testo) {
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
     * Pannello di una schermata finale (game over o completamento): titolo, sottotitolo
     * e un pulsante che esegue {@code alMenu} (ritorno al menu principale).
     */
    static VBox overlayFinale(String titolo, String sottotitolo, Runnable alMenu) {
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
        menu.setOnAction(e -> alMenu.run());

        VBox pannello = new VBox(24, etichettaTitolo, etichettaSub, menu);
        pannello.setAlignment(Pos.CENTER);
        // Senza fillWidth ogni scritta è larga quanto il suo testo e il VBox la
        // centra come nodo: titolo e sottotitolo restano allineati al centro.
        pannello.setFillWidth(false);
        // Font pixel VT323, come nel resto del gioco.
        pannello.getStyleClass().add("pixel-font");
        return pannello;
    }

    /**
     * Pannello di pausa: la scritta "PAUSA" e un pulsante "Riprendi" che esegue
     * {@code riprendi}.
     */
    static VBox pausa(Runnable riprendi) {
        Label etichettaTitolo = new Label("PAUSA");
        etichettaTitolo.getStyleClass().add("overlay-title");

        Button bottoneRiprendi = new Button("Riprendi");
        bottoneRiprendi.getStyleClass().add("game-button");
        bottoneRiprendi.setOnAction(e -> riprendi.run());

        VBox pannello = new VBox(24, etichettaTitolo, bottoneRiprendi);
        pannello.setAlignment(Pos.CENTER);
        return pannello;
    }
}
