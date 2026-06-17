package it.unicam.cs.mpgc.rpg126036.view;

import javafx.animation.AnimationTimer;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BooleanSupplier;

/**
 * Avatar giocante in esplorazione: incapsula lo sprite e il suo nodo, la posizione
 * logica sulla mappa, la direzione, i tasti premuti, i muri di collisione e il
 * ciclo di gioco (movimento con scivolamento lungo i muri e rilevamento
 * dell'elemento piu' vicino).
 *
 * <p>Estratto dalla {@link ExplorationView} per isolare il ciclo di gioco e
 * l'input dalla schermata. Comunica con essa solo tramite quanto le serve davvero:
 * la lista degli elementi su cui calcolare la prossimita', l'etichetta del
 * suggerimento da aggiornare e tre azioni di confine — se il movimento sia
 * consentito ({@code puoMuoversi}), cosa fare a ogni passo ({@code alMovimento},
 * tipicamente l'aggiornamento della torcia) e come reagire ai tasti E ed ESC
 * ({@code interagisci}, {@code gestisciEscape}), che toccano overlay, pausa e
 * dialoghi e restano percio' nella schermata.</p>
 */
final class Personaggio {

    /** Lato del riquadro di collisione del personaggio (i "piedi"). */
    static final double LATO = 28;

    // Altezza dello sprite a schermo: piu' alto del riquadro di collisione, che
    // resta ai piedi del personaggio (stile GdR 2D dall'alto).
    private static final double ALTEZZA_SPRITE = 60;
    private static final double VELOCITA = 3.0;
    private static final double RAGGIO_INTERAZIONE = 52;

    private final CharacterSprite sprite;
    private final ImageView nodo = new ImageView();
    private final double larghezzaMappa;
    private final double altezzaMappa;

    private final List<Rectangle2D> muri = new ArrayList<>();
    // Rilevamento collisioni col personaggio: condivide la lista dei muri qui sopra.
    private final MappaCollisioni collisioni = new MappaCollisioni(muri, LATO);
    private final Set<KeyCode> tastiPremuti = EnumSet.noneOf(KeyCode.class);
    private final AnimationTimer ciclo;

    // Collaboratori della schermata.
    private final List<ElementoScena> elementi;
    private final Label suggerimento;
    private final BooleanSupplier puoMuoversi;
    private final Runnable alMovimento;
    private final Runnable interagisci;
    private final Runnable gestisciEscape;

    private CharacterSprite.Direzione direzione = CharacterSprite.Direzione.GIU;
    private double posX;
    private double posY;
    private ElementoScena elementoVicino;

    /**
     * @param sprite         lo sprite della classe scelta dal giocatore
     * @param larghezzaMappa larghezza della mappa, per il limite di movimento
     * @param altezzaMappa   altezza della mappa, per il limite di movimento
     * @param elementi       gli elementi della scena (riferimento condiviso) su cui
     *                       calcolare la prossimita'
     * @param suggerimento   l'etichetta del suggerimento "Premi E" da aggiornare
     * @param puoMuoversi    se il personaggio puo' muoversi (nessun overlay, fuori pausa)
     * @param alMovimento    azione eseguita a ogni passo effettivo (es. la torcia)
     * @param interagisci    reazione al tasto E (interazione con l'elemento vicino)
     * @param gestisciEscape reazione al tasto ESC (pausa, chiusura overlay)
     */
    Personaggio(CharacterSprite sprite, double larghezzaMappa, double altezzaMappa,
                List<ElementoScena> elementi, Label suggerimento, BooleanSupplier puoMuoversi,
                Runnable alMovimento, Runnable interagisci, Runnable gestisciEscape) {
        this.sprite = Objects.requireNonNull(sprite, "Lo sprite non puo' essere nullo.");
        this.larghezzaMappa = larghezzaMappa;
        this.altezzaMappa = altezzaMappa;
        this.elementi = Objects.requireNonNull(elementi, "La lista degli elementi non puo' essere nulla.");
        this.suggerimento = Objects.requireNonNull(suggerimento, "Il suggerimento non puo' essere nullo.");
        this.puoMuoversi = Objects.requireNonNull(puoMuoversi, "Il predicato di movimento non puo' essere nullo.");
        this.alMovimento = Objects.requireNonNull(alMovimento, "L'azione di movimento non puo' essere nulla.");
        this.interagisci = Objects.requireNonNull(interagisci, "L'azione di interazione non puo' essere nulla.");
        this.gestisciEscape = Objects.requireNonNull(gestisciEscape, "L'azione di fuga non puo' essere nulla.");

        nodo.setImage(sprite.foglio());
        nodo.setFitHeight(ALTEZZA_SPRITE);
        nodo.setPreserveRatio(true);
        // Pixel art: bordi netti anche scalando.
        nodo.setSmooth(false);
        nodo.setViewport(sprite.viewport(direzione));

        posX = (larghezzaMappa - LATO) / 2;
        posY = (altezzaMappa - LATO) / 2;
        aggiornaSprite();

        this.ciclo = creaCicloDiGioco();
    }

    /** @return il nodo grafico dello sprite, da inserire nella mappa */
    ImageView nodo() {
        return nodo;
    }

    /** @return ascissa del centro del personaggio (per centrarvi la torcia) */
    double centroX() {
        return posX + LATO / 2.0;
    }

    /** @return ordinata del centro del personaggio */
    double centroY() {
        return posY + LATO / 2.0;
    }

    /** @return l'elemento attualmente piu' vicino, o {@code null} se nessuno è a portata */
    ElementoScena vicino() {
        return elementoVicino;
    }

    /**
     * Collega la cattura dei tasti alla scena quando la vista vi viene inserita e
     * la scollega (fermando il ciclo) quando viene rimossa, evitando dispersioni.
     */
    void collegaA(StackPane radice) {
        radice.sceneProperty().addListener((obs, vecchia, nuova) -> {
            if (nuova != null) {
                abilitaInput(nuova);
                ciclo.start();
            } else {
                if (vecchia != null) {
                    vecchia.setOnKeyPressed(null);
                    vecchia.setOnKeyReleased(null);
                }
                ciclo.stop();
            }
        });
    }

    /** Posiziona il personaggio nell'angolo in alto a sinistra indicato e ne allinea lo sprite. */
    void posiziona(double x, double y) {
        posX = x;
        posY = y;
        aggiornaSprite();
    }

    /**
     * Allinea lo sprite alla posizione logica: centrato in orizzontale sul riquadro
     * di collisione e appoggiato col suo bordo inferiore alla base del riquadro,
     * così che i "piedi" coincidano col punto di collisione.
     */
    void aggiornaSprite() {
        double larghezzaSprite = sprite.larghezzaPer(ALTEZZA_SPRITE);
        nodo.setLayoutX(posX + (LATO - larghezzaSprite) / 2);
        nodo.setLayoutY(posY + LATO - ALTEZZA_SPRITE);
    }

    /** Azzera i muri di collisione (alla ricostruzione della scena). */
    void azzeraMuri() {
        muri.clear();
    }

    /** Aggiunge un muro di collisione (in pixel) alla scena corrente. */
    void aggiungiMuro(Rectangle2D muro) {
        muri.add(muro);
    }

    /** Dimentica i tasti premuti (alla comparsa di un overlay, per non trascinare il movimento). */
    void azzeraTasti() {
        tastiPremuti.clear();
    }

    /** Ferma il ciclo di gioco (all'uscita dalla schermata). */
    void fermaCiclo() {
        ciclo.stop();
    }

    /** Se l'elemento indicato è quello vicino, lo dimentica e cancella il suggerimento. */
    void dimenticaSeVicino(ElementoScena elemento) {
        if (elementoVicino == elemento) {
            elementoVicino = null;
            suggerimento.setText("");
        }
    }

    private AnimationTimer creaCicloDiGioco() {
        return new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!puoMuoversi.getAsBoolean()) {
                    return;
                }
                double dx = 0;
                double dy = 0;
                if (tastiPremuti.contains(KeyCode.W)) {
                    dy -= VELOCITA;
                }
                if (tastiPremuti.contains(KeyCode.S)) {
                    dy += VELOCITA;
                }
                if (tastiPremuti.contains(KeyCode.A)) {
                    dx -= VELOCITA;
                }
                if (tastiPremuti.contains(KeyCode.D)) {
                    dx += VELOCITA;
                }
                // Spostamento separato sui due assi: cosi' il personaggio scivola
                // lungo un muro invece di bloccarsi del tutto contro uno spigolo.
                if (dx != 0) {
                    double nuovaX = clamp(posX + dx, larghezzaMappa - LATO);
                    if (!collisioni.collide(nuovaX, posY)) {
                        posX = nuovaX;
                    }
                }
                if (dy != 0) {
                    double nuovaY = clamp(posY + dy, altezzaMappa - LATO);
                    if (!collisioni.collide(posX, nuovaY)) {
                        posY = nuovaY;
                    }
                }
                if (dx != 0 || dy != 0) {
                    aggiornaDirezione(dx, dy);
                    aggiornaSprite();
                    alMovimento.run();
                }
                aggiornaElementoVicino();
            }
        };
    }

    private void abilitaInput(javafx.scene.Scene scena) {
        scena.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case E -> interagisci.run();
                case ESCAPE -> gestisciEscape.run();
                default -> tastiPremuti.add(e.getCode());
            }
        });
        scena.setOnKeyReleased(e -> tastiPremuti.remove(e.getCode()));
    }

    /**
     * Aggiorna la direzione dello sprite in base allo spostamento, dando priorità
     * all'asse orizzontale, e cambia la cella mostrata solo se la direzione è
     * effettivamente cambiata.
     */
    private void aggiornaDirezione(double dx, double dy) {
        CharacterSprite.Direzione nuova;
        if (dx > 0) {
            nuova = CharacterSprite.Direzione.DESTRA;
        } else if (dx < 0) {
            nuova = CharacterSprite.Direzione.SINISTRA;
        } else if (dy < 0) {
            nuova = CharacterSprite.Direzione.SU;
        } else {
            nuova = CharacterSprite.Direzione.GIU;
        }
        if (nuova != direzione) {
            direzione = nuova;
            nodo.setViewport(sprite.viewport(direzione));
        }
    }

    /**
     * Individua l'elemento piu' vicino al personaggio entro il raggio di
     * interazione e aggiorna il suggerimento a schermo.
     */
    private void aggiornaElementoVicino() {
        double centroX = centroX();
        double centroY = centroY();
        ElementoScena piuVicino = null;
        double minDistanza = RAGGIO_INTERAZIONE;
        for (ElementoScena e : elementi) {
            double distanza = Math.hypot(centroX - e.x, centroY - e.y);
            if (distanza <= minDistanza) {
                minDistanza = distanza;
                piuVicino = e;
            }
        }
        if (piuVicino != elementoVicino) {
            evidenzia(elementoVicino, false);
            evidenzia(piuVicino, true);
            elementoVicino = piuVicino;
        }
        suggerimento.setText(piuVicino == null ? "" : "▲ Premi E — " + piuVicino.etichettaAzione);
    }

    private void evidenzia(ElementoScena e, boolean attivo) {
        if (e != null) {
            e.evidenzia(attivo);
        }
    }

    private double clamp(double valore, double massimo) {
        return Math.max(0, Math.min(valore, massimo));
    }
}
