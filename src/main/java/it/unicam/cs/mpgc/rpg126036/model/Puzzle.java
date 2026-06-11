package it.unicam.cs.mpgc.rpg126036.model;

import java.util.List;

/**
 * Contratto di un enigma del gioco. Un enigma presenta un testo, puo' fornire
 * suggerimenti in base alle statistiche del giocatore, valuta i tentativi di
 * soluzione e offre sempre un'opzione di sblocco garantito ("forza bruta") a
 * costo di energia, cosi' che il gioco non possa mai bloccarsi.
 *
 * <p>I metodi che modificano lo stato del giocatore (energia, XP) ne applicano
 * direttamente gli effetti e restituiscono un {@link PuzzleOutcome} descrittivo.</p>
 */
public interface Puzzle {

    /**
     * @return l'identificativo univoco dell'enigma
     */
    String getId();

    /**
     * @return il testo di presentazione dell'enigma
     */
    String getTesto();

    /**
     * @return {@code true} se l'enigma e' gia' stato risolto
     */
    boolean isRisolto();

    /**
     * Restituisce gli indizi sbloccati dalle statistiche del giocatore (ad es.
     * Investigazione, Intuizione o Carisma sopra una certa soglia).
     *
     * @param giocatore il giocatore che affronta l'enigma (non nullo)
     * @return la lista degli indizi disponibili, vuota se nessuno
     */
    List<String> suggerimentiPer(Player giocatore);

    /**
     * Valuta un tentativo di soluzione esplicito.
     *
     * @param giocatore il giocatore che affronta l'enigma (non nullo)
     * @param tentativo la soluzione proposta
     * @return l'esito del tentativo
     */
    PuzzleOutcome tenta(Player giocatore, String tentativo);

    /**
     * Sblocca l'enigma con la forza bruta: garantisce la risoluzione ma applica
     * una penalita' di energia. Serve a evitare situazioni di stallo.
     *
     * @param giocatore il giocatore che affronta l'enigma (non nullo)
     * @return l'esito dello sblocco forzato
     */
    PuzzleOutcome forzaBruta(Player giocatore);
}
