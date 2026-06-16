package it.unicam.cs.mpgc.rpg126036.view;

import javafx.geometry.Rectangle2D;

import java.util.List;
import java.util.Objects;

/**
 * Rilevamento delle collisioni del personaggio con i muri della scena. Incapsula
 * la geometria del movimento, separandola dal disegno e dal ciclo di gioco della
 * {@link ExplorationView}.
 *
 * <p>Condivide il riferimento alla lista dei muri con la scena, che la ripopola a
 * ogni cambio di ambiente: cosi' il rilevamento resta sempre allineato senza
 * doverla risincronizzare. La logica e' pura (nessuna dipendenza dal toolkit
 * grafico) ed e' percio' verificabile con unit test.</p>
 */
final class MappaCollisioni {

    private final List<Rectangle2D> muri;
    private final double latoPersonaggio;

    /**
     * @param muri            la lista dei muri della scena (condivisa, non nulla)
     * @param latoPersonaggio il lato del riquadro di collisione del personaggio
     */
    MappaCollisioni(List<Rectangle2D> muri, double latoPersonaggio) {
        this.muri = Objects.requireNonNull(muri, "La lista dei muri non puo' essere nulla.");
        this.latoPersonaggio = latoPersonaggio;
    }

    /**
     * @param x ascissa dell'angolo in alto a sinistra del personaggio
     * @param y ordinata dell'angolo in alto a sinistra del personaggio
     * @return {@code true} se il personaggio, posizionato in (x, y), si
     *         sovrapporrebbe a un muro della scena
     */
    boolean collide(double x, double y) {
        for (Rectangle2D muro : muri) {
            if (muro.intersects(x, y, latoPersonaggio, latoPersonaggio)) {
                return true;
            }
        }
        return false;
    }
}
