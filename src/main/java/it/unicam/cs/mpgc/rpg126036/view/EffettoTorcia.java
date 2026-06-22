package it.unicam.cs.mpgc.rpg126036.view;

import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;

/**
 * Effetto torcia dell'Aula B al buio: un velo nero esteso a tutta la mappa con un
 * foro di luce (gradiente radiale) trasparente solo attorno al giocatore, così il
 * resto dell'aula resta in ombra. Incapsula la geometria del cono di luce,
 * separandola dal disegno e dal ciclo di gioco della {@link ExplorationView}.
 */
final class EffettoTorcia {

    // Raggio del cono di luce attorno al giocatore (px).
    private static final double RAGGIO = 95;

    private final Rectangle velo;

    /**
     * @param dimensione le dimensioni della mappa coperta dal velo (non nulle)
     */
    EffettoTorcia(DimensioneMappa dimensione) {
        velo = new Rectangle(dimensione.larghezza(), dimensione.altezza());
        // Il velo non deve intercettare i clic sugli elementi sottostanti.
        velo.setMouseTransparent(true);
    }

    /**
     * @return il nodo del velo, da sovrapporre allo scenario e al personaggio
     */
    Rectangle nodo() {
        return velo;
    }

    /**
     * Ricalcola il foro di luce centrandolo sul punto indicato (di norma il centro
     * del personaggio): trasparente al centro, opaco verso i bordi.
     *
     * @param cx ascissa del centro del cono di luce
     * @param cy ordinata del centro del cono di luce
     */
    void centraSu(double cx, double cy) {
        RadialGradient luce = new RadialGradient(0, 0, cx, cy, RAGGIO, false,
                CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.color(0, 0, 0, 0.0)),
                new Stop(0.55, Color.color(0, 0, 0, 0.0)),
                new Stop(1.0, Color.color(0, 0, 0, 0.97)));
        velo.setFill(luce);
    }
}
