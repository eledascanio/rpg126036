package it.unicam.cs.mpgc.rpg126036.view;

/**
 * Facciata dei servizi di scena che la schermata di esplorazione mette a
 * disposizione dei componenti di trama estratti ({@link AulaB}, {@link Porte},
 * {@link DialoghiNpc}, {@link PcVittima}, {@link Pensieri}).
 *
 * <p>Aggrega i ruoli coesi in cui i servizi sono stati segregati (ISP):
 * {@link ServiziMappa}, {@link ServiziTorcia}, {@link ServiziDialoghi},
 * {@link ServiziOverlay} e {@link ServiziProgressione}. {@link ExplorationView}
 * li implementa tutti, mentre ogni collaboratore espone nei propri campi solo i
 * ruoli che usa: chi mostra solo dialoghi non dipende dalla torcia o dalla
 * progressione, e così via.</p>
 *
 * <p>Astrazione introdotta anche per invertire la dipendenza (DIP): i componenti
 * di trama dipendono da questi contratti e non dalla classe concreta
 * {@link ExplorationView}, che ne è l'unica implementazione.</p>
 */
public interface RegiaEsplorazione extends ServiziMappa, ServiziTorcia,
        ServiziDialoghi, ServiziOverlay, ServiziProgressione {

    /** Energia consumata a ogni dialogo con un NPC (regola di gioco), e penalità affini. */
    int COSTO_ENERGIA_DIALOGO = 10;
}
