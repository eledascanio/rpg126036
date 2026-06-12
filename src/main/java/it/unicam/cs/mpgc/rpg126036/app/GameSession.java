package it.unicam.cs.mpgc.rpg126036.app;

import it.unicam.cs.mpgc.rpg126036.achievement.AchievementManager;
import it.unicam.cs.mpgc.rpg126036.engine.GameEngine;
import it.unicam.cs.mpgc.rpg126036.engine.GameState;
import it.unicam.cs.mpgc.rpg126036.persistence.Campaign;

import java.util.Objects;

/**
 * Sessione di gioco assemblata: tiene insieme i componenti di una partita avviata
 * (motore, stato, gestore dei traguardi e campagna) e li espone a chi pilota il
 * gioco (la futura interfaccia grafica e i suoi controller).
 *
 * <p>E' un semplice contenitore immutabile di riferimenti: non aggiunge logica,
 * cosi' che la costruzione resti responsabilita' della {@link GameSessionFactory}
 * e l'evoluzione dello stato resti del {@link GameEngine}.</p>
 */
public class GameSession {

    private final GameEngine engine;
    private final GameState stato;
    private final AchievementManager achievementManager;
    private final Campaign campaign;

    /**
     * @param engine             il motore della partita (non nullo)
     * @param stato              lo stato della partita (non nullo)
     * @param achievementManager il gestore dei traguardi (non nullo)
     * @param campaign           la campagna in corso (non nulla)
     */
    public GameSession(GameEngine engine, GameState stato,
                       AchievementManager achievementManager, Campaign campaign) {
        this.engine = Objects.requireNonNull(engine, "Il motore non puo' essere nullo.");
        this.stato = Objects.requireNonNull(stato, "Lo stato non puo' essere nullo.");
        this.achievementManager = Objects.requireNonNull(achievementManager, "Il gestore dei traguardi non puo' essere nullo.");
        this.campaign = Objects.requireNonNull(campaign, "La campagna non puo' essere nulla.");
    }

    public GameEngine getEngine() {
        return engine;
    }

    public GameState getStato() {
        return stato;
    }

    public AchievementManager getAchievementManager() {
        return achievementManager;
    }

    public Campaign getCampaign() {
        return campaign;
    }
}
