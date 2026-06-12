package it.unicam.cs.mpgc.rpg126036.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Descrive l'ambiente grafico di una scena esplorabile: l'immagine di sfondo, le
 * zone non calpestabili (muri/ostacoli) e il punto di comparsa del personaggio.
 *
 * <p>Le coordinate sono espresse come frazioni (0..1) della larghezza e altezza
 * della mappa, cosi' da restare valide a qualsiasi dimensione di rendering: la
 * vista le converte in pixel quando costruisce la scena. Le scene prive di
 * ambiente registrato usano la mappa astratta di default, senza ostacoli.</p>
 */
public final class SceneEnvironment {

    /**
     * Zona rettangolare non calpestabile, in frazioni delle dimensioni della mappa.
     *
     * @param x ascissa dell'angolo in alto a sinistra (0..1)
     * @param y ordinata dell'angolo in alto a sinistra (0..1)
     * @param w larghezza (0..1)
     * @param h altezza (0..1)
     */
    public record Muro(double x, double y, double w, double h) {
    }

    /**
     * Posizione (in frazioni delle dimensioni della mappa) su cui collocare un
     * elemento interattivo, scelta su una zona calpestabile.
     *
     * @param x ascissa (0..1)
     * @param y ordinata (0..1)
     */
    public record Punto(double x, double y) {
    }

    /**
     * Ambiente di una scena: sfondo (risorsa sul classpath, può essere {@code null}),
     * muri, posizioni degli elementi e punto di comparsa del personaggio.
     *
     * <p>Gli {@code slot} fissano dove collocare gli elementi della scena (NPC,
     * oggetti, enigmi, uscite) nell'ordine in cui sono dichiarati, così da posarli
     * sul pavimento ed evitare che finiscano dentro un muro. Se vuoti, la vista
     * ripiega sulla disposizione automatica a fasce.</p>
     *
     * @param sfondo path classpath dell'immagine di sfondo, o {@code null}
     * @param muri   le zone non calpestabili
     * @param slot   le posizioni degli elementi, in ordine di dichiarazione
     * @param spawnX ascissa del punto di comparsa (0..1)
     * @param spawnY ordinata del punto di comparsa (0..1)
     */
    public record Ambiente(String sfondo, List<Muro> muri, List<Punto> slot, double spawnX, double spawnY) {

        public Ambiente {
            muri = List.copyOf(muri);
            slot = List.copyOf(slot);
        }
    }

    private final Map<String, Ambiente> perScena = new HashMap<>();

    public SceneEnvironment() {
        // Cortile del Polo (capitoli 1 e 3): piazza grigia a forma di croce
        // racchiusa dai due edifici (Polo A a sinistra, Polo B a destra),
        // dall'aiuola alberata in alto e dalla fila di alberi in basso.
        perScena.put("cortile", new Ambiente("/images/scene/cortile.jpg", List.of(
                new Muro(0.00, 0.00, 0.32, 0.55),  // edificio Polo A (sinistra) e facciata
                new Muro(0.32, 0.00, 0.38, 0.20),  // aiuola alberata superiore
                new Muro(0.70, 0.00, 0.30, 0.56),  // edificio Polo B (destra)
                new Muro(0.93, 0.38, 0.07, 0.46),  // alberi sul bordo destro
                new Muro(0.00, 0.73, 0.09, 0.11),  // stradina esterna nell'angolo in basso a sinistra
                new Muro(0.00, 0.84, 1.00, 0.16)   // fila di alberi inferiore
        ), List.of(
                // Slot sul pavimento della piazza, nell'ordine dei contenuti dichiarati
                // nell'XML: 4 NPC, poi la chiave, infine l'enigma (porta del Polo A).
                new Punto(0.40, 0.30),
                new Punto(0.60, 0.30),
                new Punto(0.38, 0.70),
                new Punto(0.62, 0.70),
                new Punto(0.52, 0.52),
                new Punto(0.37, 0.45)
        ), 0.50, 0.76));
    }

    /**
     * @param idScena id della scena
     * @return l'ambiente registrato per la scena, se presente
     */
    public Optional<Ambiente> di(String idScena) {
        return Optional.ofNullable(perScena.get(idScena));
    }
}
