package it.unicam.cs.mpgc.rpg126036.view;

import it.unicam.cs.mpgc.rpg126036.model.CharacterClass;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

/**
 * Sprite del personaggio giocante. Carica il foglio normalizzato della classe
 * scelta e ne espone le quattro direzioni, così da mostrarlo sia nelle card di
 * creazione (vista frontale) sia in esplorazione (direzione del movimento).
 *
 * <p>Il foglio è una griglia 2x2 di celle identiche prodotta dallo strumento
 * {@code private/tools/sprite_process.py}. Le tre classi condividono lo stesso
 * disegno e si distinguono solo per il colore del cappello; ogni classe ha
 * quindi il proprio file, con ripiego sul foglio dello studente finché gli altri
 * non sono disponibili.</p>
 *
 * <pre>
 *   cella (0,0) = giù     cella (1,0) = su
 *   cella (0,1) = destra  cella (1,1) = sinistra
 * </pre>
 *
 * <p>Le due celle inferiori sono speculari: la cella di sinistra del foglio
 * ritrae il personaggio rivolto a destra e viceversa.</p>
 */
public final class CharacterSprite {

    /** Le quattro direzioni in cui il personaggio può essere rivolto. */
    public enum Direzione {GIU, SU, SINISTRA, DESTRA}

    private static final Map<CharacterClass, String> RISORSE = new EnumMap<>(CharacterClass.class);

    static {
        RISORSE.put(CharacterClass.STUDENTE_MODELLO, "/images/sprite/studente.png");
        RISORSE.put(CharacterClass.RAPPRESENTANTE_STUDENTI, "/images/sprite/rappresentante.png");
        RISORSE.put(CharacterClass.ORGANIZZATORE_EVENTI, "/images/sprite/organizzatore.png");
    }

    /** Foglio usato quando quello specifico della classe non è ancora presente. */
    private static final String RIPIEGO = "/images/sprite/studente.png";

    private final Image foglio;
    private final double cellaLarghezza;
    private final double cellaAltezza;

    /**
     * Carica lo sprite della classe indicata.
     *
     * @param classe la classe del personaggio (non nulla)
     */
    public CharacterSprite(CharacterClass classe) {
        this.foglio = carica(classe);
        // Griglia 2x2: ogni cella è metà foglio per lato.
        this.cellaLarghezza = foglio.getWidth() / 2;
        this.cellaAltezza = foglio.getHeight() / 2;
    }

    private static Image carica(CharacterClass classe) {
        InputStream risorsa = CharacterSprite.class.getResourceAsStream(RISORSE.get(classe));
        if (risorsa == null) {
            risorsa = CharacterSprite.class.getResourceAsStream(RIPIEGO);
        }
        return new Image(risorsa);
    }

    /**
     * @return il foglio sprite completo (griglia 2x2 delle direzioni)
     */
    public Image foglio() {
        return foglio;
    }

    /**
     * @return il rapporto larghezza/altezza di una cella, per dimensionare il nodo
     */
    public double rapporto() {
        return cellaLarghezza / cellaAltezza;
    }

    /**
     * @param direzione la direzione richiesta
     * @return il riquadro (in pixel del foglio) della cella corrispondente
     */
    public Rectangle2D viewport(Direzione direzione) {
        // Le due celle laterali del foglio sono speculari rispetto alla direzione
        // di marcia: la cella di sinistra del foglio ritrae il personaggio rivolto
        // a destra e viceversa, quindi qui le colonne sono incrociate.
        int colonna = switch (direzione) {
            case GIU, DESTRA -> 0;
            case SU, SINISTRA -> 1;
        };
        int riga = switch (direzione) {
            case GIU, SU -> 0;
            case SINISTRA, DESTRA -> 1;
        };
        return new Rectangle2D(colonna * cellaLarghezza, riga * cellaAltezza, cellaLarghezza, cellaAltezza);
    }

    /**
     * Crea un nodo che mostra una sola direzione dello sprite, alto i pixel
     * indicati e proporzionato in larghezza.
     *
     * @param direzione la direzione da mostrare
     * @param altezza   l'altezza desiderata in pixel
     * @return l'ImageView pronto per l'inserimento nella scena
     */
    public ImageView nodo(Direzione direzione, double altezza) {
        ImageView vista = new ImageView(foglio);
        vista.setViewport(viewport(direzione));
        vista.setFitHeight(altezza);
        vista.setPreserveRatio(true);
        // Pixel art: niente interpolazione, bordi netti anche in scala.
        vista.setSmooth(false);
        return vista;
    }

    /**
     * @param altezza l'altezza in pixel
     * @return la larghezza corrispondente mantenendo le proporzioni della cella
     */
    public double larghezzaPer(double altezza) {
        return altezza * rapporto();
    }
}
