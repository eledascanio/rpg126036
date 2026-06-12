package it.unicam.cs.mpgc.rpg126036.app;

import it.unicam.cs.mpgc.rpg126036.achievement.AchievementManager;
import it.unicam.cs.mpgc.rpg126036.engine.GameEngine;
import it.unicam.cs.mpgc.rpg126036.engine.GameState;
import it.unicam.cs.mpgc.rpg126036.model.CharacterClass;
import it.unicam.cs.mpgc.rpg126036.model.Player;
import it.unicam.cs.mpgc.rpg126036.persistence.AutoSaveListener;
import it.unicam.cs.mpgc.rpg126036.persistence.Campaign;
import it.unicam.cs.mpgc.rpg126036.persistence.GameOverCleanupListener;
import it.unicam.cs.mpgc.rpg126036.persistence.SaveRepository;

import java.util.Objects;

/**
 * Costruisce una {@link GameSession} pronta all'uso, assemblando i componenti del
 * gioco e registrando sul motore i listener standard di una partita:
 * <ul>
 *     <li>{@link AchievementManager} per i traguardi;</li>
 *     <li>{@link AutoSaveListener} per il salvataggio automatico a fine capitolo;</li>
 *     <li>{@link GameOverCleanupListener} per liberare lo slot a fine partita.</li>
 * </ul>
 *
 * <p>Concentra qui il cablaggio (composition root della partita): aggiungere un
 * nuovo osservatore di sistema richiede solo registrarlo in questo punto.</p>
 */
public class GameSessionFactory {

    private final SaveRepository repository;
    private final Campaign campaign;

    /**
     * @param repository il repository dei salvataggi (non nullo)
     * @param campaign   la campagna da giocare (non nulla)
     */
    public GameSessionFactory(SaveRepository repository, Campaign campaign) {
        this.repository = Objects.requireNonNull(repository, "Il repository non puo' essere nullo.");
        this.campaign = Objects.requireNonNull(campaign, "La campagna non puo' essere nulla.");
    }

    /**
     * Avvia una nuova partita creando il personaggio e assemblando la sessione dal
     * primo capitolo della campagna.
     *
     * @param nomePersonaggio nome del personaggio (non nullo)
     * @param classe          classe scelta (non nulla)
     * @return la sessione di gioco pronta
     */
    public GameSession nuovaPartita(String nomePersonaggio, CharacterClass classe) {
        Player player = new Player(nomePersonaggio, classe);
        return assembla(new GameState(player));
    }

    /**
     * Assembla la sessione su uno stato gia' esistente, registrando i listener.
     * Il motore parte dal primo capitolo della campagna.
     *
     * @param stato lo stato della partita (non nullo)
     * @return la sessione di gioco pronta
     */
    private GameSession assembla(GameState stato) {
        GameEngine engine = new GameEngine(stato, campaign.getCapitoli());
        AchievementManager achievementManager = new AchievementManager(stato);
        engine.addListener(achievementManager);
        engine.addListener(new AutoSaveListener(repository, stato));
        engine.addListener(new GameOverCleanupListener(repository, stato));
        return new GameSession(engine, stato, achievementManager, campaign);
    }
}
