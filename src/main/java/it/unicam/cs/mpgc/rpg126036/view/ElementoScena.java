package it.unicam.cs.mpgc.rpg126036.view;

import it.unicam.cs.mpgc.rpg126036.model.Puzzle;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.TextAlignment;

/**
 * Elemento interattivo posizionato sulla mappa di esplorazione: un nodo visivo
 * (cerchio colorato con etichetta, oppure sprite immagine senza etichetta),
 * un'azione di interazione e, per gli enigmi, il relativo {@link Puzzle}.
 *
 * <p>I campi e i metodi hanno visibilita' di package: la {@link ExplorationView}
 * vi accede direttamente nel comporre e aggiornare la scena.</p>
 */
final class ElementoScena {

    /** Raggio del cerchio segnaposto e riferimento per posizionare l'etichetta. */
    static final double RAGGIO_ELEMENTO = 16;

    final TipoElemento tipo;
    final String etichettaAzione;
    final Node nodo;
    // Nome sotto l'elemento: presente per i cerchi, assente per gli sprite.
    final Label etichettaNodo;
    double x;
    double y;
    Runnable azione;
    Puzzle puzzle;
    // Elementi a posizione fissa (le porte degli edifici) sono esclusi dalla
    // disposizione automatica su slot/fasce e collocati esplicitamente.
    boolean posizioneFissa;

    /** Elemento a cerchio colorato con il nome sotto (segnaposto generico). */
    ElementoScena(TipoElemento tipo, String nome, String etichettaAzione, Color colore) {
        this.tipo = tipo;
        this.etichettaAzione = etichettaAzione;
        Circle cerchio = new Circle(RAGGIO_ELEMENTO, colore);
        cerchio.setStroke(Color.WHITE);
        cerchio.setStrokeWidth(0);
        this.nodo = cerchio;
        this.etichettaNodo = new Label(nome);
        this.etichettaNodo.getStyleClass().add("hud-text");
        this.etichettaNodo.setTextAlignment(TextAlignment.CENTER);
        this.etichettaNodo.setPrefWidth(140);
        this.etichettaNodo.setAlignment(Pos.CENTER);
    }

    /** Elemento a sprite immagine, senza nome sotto. */
    ElementoScena(TipoElemento tipo, String etichettaAzione, ImageView immagine) {
        this.tipo = tipo;
        this.etichettaAzione = etichettaAzione;
        this.nodo = immagine;
        this.etichettaNodo = null;
    }

    /** Elemento con un cerchio già configurato (es. il luccichio dell'Aula B), senza nome sotto. */
    ElementoScena(TipoElemento tipo, String etichettaAzione, Circle cerchio) {
        this.tipo = tipo;
        this.etichettaAzione = etichettaAzione;
        this.nodo = cerchio;
        this.etichettaNodo = null;
    }

    void posiziona(double x, double y) {
        this.x = x;
        this.y = y;
        if (nodo instanceof Circle cerchio) {
            cerchio.setCenterX(x);
            cerchio.setCenterY(y);
        } else {
            // Immagine: centrata sul punto (x, y).
            nodo.setLayoutX(x - nodo.getBoundsInLocal().getWidth() / 2);
            nodo.setLayoutY(y - nodo.getBoundsInLocal().getHeight() / 2);
        }
        if (etichettaNodo != null) {
            etichettaNodo.setLayoutX(x - etichettaNodo.getPrefWidth() / 2);
            etichettaNodo.setLayoutY(y + RAGGIO_ELEMENTO + 2);
        }
    }

    /** Mostra o nasconde il nodo (e l'eventuale etichetta) dell'elemento. */
    void setVisibile(boolean visibile) {
        nodo.setVisible(visibile);
        if (etichettaNodo != null) {
            etichettaNodo.setVisible(visibile);
        }
    }

    /** Evidenzia l'elemento vicino: bordo per i cerchi, bagliore per gli sprite. */
    void evidenzia(boolean attivo) {
        if (nodo instanceof Circle cerchio) {
            cerchio.setStrokeWidth(attivo ? 3 : 0);
        } else {
            nodo.setEffect(attivo ? new Glow(0.9) : null);
        }
    }
}
