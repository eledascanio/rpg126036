package it.unicam.cs.mpgc.rpg126036.view;

/**
 * Ruolo dell'effetto torcia (Aula B al buio): il velo nero col foro di luce
 * attorno al giocatore.
 *
 * <p>Uno dei ruoli in cui è segregata la {@link RegiaEsplorazione} (ISP). Vi
 * dipende il solo componente {@link AulaB}, l'unica trama che si svolge al buio.</p>
 */
public interface ServiziTorcia {

    /** Aggiunge il velo della torcia sopra lo scenario (Aula B al buio). */
    void aggiungiTorcia();

    /** Ricalcola il foro di luce della torcia centrandolo sul giocatore. */
    void aggiornaTorcia();
}
