package it.unicam.cs.mpgc.rpg126036.view;

import it.unicam.cs.mpgc.rpg126036.model.Player;
import it.unicam.cs.mpgc.rpg126036.model.StatType;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Barra di stato (HUD) della schermata di esplorazione: mostra energia, XP e le
 * statistiche del giocatore. Costruisce il proprio nodo e si aggiorna sullo stato
 * corrente, senza conoscere il resto della scena.
 *
 * <p>Estratta dalla {@link ExplorationView} per isolare la presentazione dell'HUD
 * dalla logica di gioco: i suoi nodi sono usati solo qui, nella costruzione e
 * nell'aggiornamento.</p>
 */
final class HudEsplorazione {

    private final ProgressBar barraEnergia = new ProgressBar();
    private final Label valoreEnergia = new Label();
    private final Label valoreXp = new Label();
    private final HBox statistiche = new HBox(16);

    /**
     * Costruisce il nodo dell'HUD: a sinistra energia (barra + valore) e XP, a destra
     * le statistiche.
     *
     * @return il nodo da inserire nel layout della schermata
     */
    Node costruisci() {
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

    /**
     * Aggiorna l'HUD sullo stato corrente: livello e colore della barra di energia,
     * valori numerici di energia e XP, e l'elenco delle statistiche.
     *
     * @param energia l'energia corrente del giocatore
     * @param player  il giocatore (XP e statistiche)
     */
    void aggiorna(int energia, Player player) {
        barraEnergia.setProgress((double) energia / Player.ENERGIA_MASSIMA);
        barraEnergia.setStyle("-fx-accent: " + coloreEnergia(energia) + ";");
        valoreEnergia.setText(energia + " / " + Player.ENERGIA_MASSIMA);
        valoreXp.setText("XP: " + player.getXp() + " / " + Player.COSTO_XP_POTENZIAMENTO);

        statistiche.getChildren().clear();
        for (StatType tipo : StatType.values()) {
            Label stat = new Label(tipo.getIcona() + " " + player.getStatistica(tipo));
            stat.getStyleClass().add("hud-stat");
            statistiche.getChildren().add(stat);
        }
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
}
