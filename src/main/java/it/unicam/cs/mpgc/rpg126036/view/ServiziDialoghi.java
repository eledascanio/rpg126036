package it.unicam.cs.mpgc.rpg126036.view;

import java.util.List;

/**
 * Ruolo dei dialoghi a sovrimpressione: il dialog box standard (con o senza
 * scelte) e la sua chiusura.
 *
 * <p>Uno dei ruoli in cui è segregata la {@link RegiaEsplorazione} (ISP). È il
 * ruolo più trasversale: vi dipende ogni componente di trama, poiché tutti
 * comunicano col giocatore attraverso il dialog box.</p>
 */
public interface ServiziDialoghi {

    /** Mostra un dialog box (nome + testo) con chiusura standard. */
    void mostraDialogo(String nome, String testo);

    /**
     * Mostra un dialog box completo: al termine del testo, se non ci sono opzioni
     * esegue {@code alTermine}, altrimenti presenta le scelte.
     */
    void mostraDialogo(String nome, String testo, Runnable alTermine, List<OpzioneDialogo> opzioni);

    /** Chiude l'overlay corrente, fermando l'eventuale dialogo in corso. */
    void chiudiOverlay();
}
