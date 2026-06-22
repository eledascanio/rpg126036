package it.unicam.cs.mpgc.rpg126036.view;

import it.unicam.cs.mpgc.rpg126036.model.StatType;
import it.unicam.cs.mpgc.rpg126036.model.Transition;

import java.util.function.Consumer;

/**
 * Ruolo della progressione di gioco: aggiornamento dell'HUD, potenziamenti delle
 * statistiche, annuncio dei traguardi, pensieri d'indagine e avanzamento tra le
 * scene.
 *
 * <p>Uno dei ruoli in cui è segregata la {@link RegiaEsplorazione} (ISP). Vi
 * dipendono i componenti che fanno evolvere lo stato di gioco a valle di
 * un'interazione (energia, XP, cambio scena).</p>
 */
public interface ServiziProgressione {

    /** Aggiorna l'HUD (energia, XP, statistiche) sullo stato corrente. */
    void aggiornaHud();

    /**
     * Se il giocatore ha raggiunto la soglia di XP, mostra la scelta del
     * potenziamento (ripetuta a catena); al termine — o subito — esegue {@code dopo}.
     */
    void mostraPotenziamentoSeDovuto(Runnable dopo);

    /** Mostra la schermata di scelta della statistica da potenziare. */
    void mostraSceltaUpgrade(String titolo, String sottotitolo, Consumer<StatType> azione);

    /** Annuncia col dialog box un eventuale traguardo appena sbloccato, poi esegue {@code dopo}. */
    void annunciaTraguardoSePresente(Runnable dopo);

    /** Mostra una sola volta il pensiero d'indagine verso il PC, se ne ricorrono le condizioni. */
    void forsePensieroIndagine();

    /** Segue una transizione verso un'altra scena (con il relativo cartello). */
    void usaUscita(Transition transizione);

    /** Avanza il motore alla scena indicata senza ricostruire la vista (scene di servizio). */
    void avanzaSenzaRicostruire(String idScena);
}
