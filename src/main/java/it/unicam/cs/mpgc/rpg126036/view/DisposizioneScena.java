package it.unicam.cs.mpgc.rpg126036.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Colloca gli elementi interattivi di una scena sulla mappa. Gli elementi a
 * posizione fissa (le porte degli edifici) sono saltati: vengono piazzati altrove.
 * Gli altri si dispongono sugli slot dell'ambiente, se definiti (sul pavimento,
 * lontano dai muri); in mancanza di slot si ripiega sulla disposizione automatica
 * a tre fasce orizzontali.
 *
 * <p>Estratta dalla {@link ExplorationView} per isolare la geometria del layout
 * della scena dal resto della schermata. Le coordinate sono calcolate in pixel a
 * partire dalle dimensioni della mappa.</p>
 */
final class DisposizioneScena {

    private final double larghezzaMappa;
    private final double altezzaMappa;

    /**
     * @param dimensione le dimensioni della mappa in pixel (non nulle)
     */
    DisposizioneScena(DimensioneMappa dimensione) {
        Objects.requireNonNull(dimensione, "Le dimensioni della mappa non possono essere nulle.");
        this.larghezzaMappa = dimensione.larghezza();
        this.altezzaMappa = dimensione.altezza();
    }

    /**
     * Dispone gli elementi non a posizione fissa: sugli slot dell'ambiente se
     * presenti, altrimenti a fasce.
     *
     * @param elementi gli elementi della scena (quelli a posizione fissa sono ignorati)
     * @param ambiente l'ambiente della scena, o {@code null} se la scena non ne ha uno
     */
    void disponi(List<ElementoScena> elementi, SceneEnvironment.Ambiente ambiente) {
        List<ElementoScena> daDisporre = new ArrayList<>();
        for (ElementoScena elemento : elementi) {
            if (!elemento.posizioneFissa) {
                daDisporre.add(elemento);
            }
        }
        if (ambiente != null && !ambiente.slot().isEmpty()) {
            disponiSuSlot(daDisporre, ambiente.slot());
        } else {
            disponiAFasce(daDisporre);
        }
    }

    private void disponiSuSlot(List<ElementoScena> daDisporre, List<SceneEnvironment.Punto> slot) {
        for (int i = 0; i < daDisporre.size(); i++) {
            if (i < slot.size()) {
                daDisporre.get(i).posiziona(slot.get(i).x() * larghezzaMappa, slot.get(i).y() * altezzaMappa);
            } else {
                // Piu' elementi che slot: fallback in basso, distribuiti in larghezza.
                int extra = i - slot.size();
                double x = larghezzaMappa * (extra + 1.0) / (daDisporre.size() - slot.size() + 1);
                daDisporre.get(i).posiziona(x, altezzaMappa - 48);
            }
        }
    }

    /**
     * Dispone gli elementi su tre fasce orizzontali (NPC in alto, oggetti ed enigmi
     * al centro, uscite in basso), distribuendoli uniformemente in larghezza.
     */
    private void disponiAFasce(List<ElementoScena> daDisporre) {
        List<ElementoScena> npc = new ArrayList<>();
        List<ElementoScena> centro = new ArrayList<>();
        List<ElementoScena> uscite = new ArrayList<>();
        for (ElementoScena elemento : daDisporre) {
            switch (elemento.tipo) {
                case NPC -> npc.add(elemento);
                case OGGETTO, ENIGMA -> centro.add(elemento);
                case USCITA -> uscite.add(elemento);
                case PORTA -> { /* le porte hanno posizione fissa, non a fasce */ }
            }
        }
        disponiFascia(npc, 64);
        disponiFascia(centro, altezzaMappa / 2);
        disponiFascia(uscite, altezzaMappa - 64);
    }

    private void disponiFascia(List<ElementoScena> fascia, double y) {
        for (int i = 0; i < fascia.size(); i++) {
            double x = larghezzaMappa * (i + 1) / (fascia.size() + 1);
            fascia.get(i).posiziona(x, y);
        }
    }
}
